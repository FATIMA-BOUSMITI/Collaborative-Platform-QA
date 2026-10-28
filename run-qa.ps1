[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet("check", "selenium", "robot", "k6", "all", "setup", "cleanup")]
    [string]$Target = "all"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$EnvFile = Join-Path $ProjectRoot ".env"

function Import-LocalEnv {
    if (-not (Test-Path -LiteralPath $EnvFile)) {
        throw "Missing .env. Copy .env.example to .env and configure dedicated QA credentials once."
    }
    foreach ($line in Get-Content -LiteralPath $EnvFile) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith("#") -or -not $trimmed.Contains("=")) { continue }
        $name, $value = $trimmed.Split("=", 2)
        $name = $name.Trim()
        $value = $value.Trim().Trim('"').Trim("'")
        if (-not [Environment]::GetEnvironmentVariable($name, "Process")) {
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
}

function Require-Command([string]$Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required tool '$Name' was not found on PATH. Run .\run-qa.ps1 setup or install it first."
    }
}

function Get-Python {
    $python = Get-Command python -ErrorAction SilentlyContinue
    if ($python) { return $python.Source }
    $candidate = Join-Path $env:LOCALAPPDATA "Programs\Python\Python313\python.exe"
    if (Test-Path -LiteralPath $candidate) { return $candidate }
    throw "Python 3 was not found. Install Python before running Robot Framework."
}

function Require-Setting([string]$Name) {
    $value = [Environment]::GetEnvironmentVariable($Name, "Process")
    if ([string]::IsNullOrWhiteSpace($value)) { throw "Required QA setting is empty: $Name" }
    return $value
}

function Test-Service([string]$Url, [string]$Name) {
    $uri = [Uri]$Url
    $port = if ($uri.IsDefaultPort) { if ($uri.Scheme -eq "https") { 443 } else { 80 } } else { $uri.Port }
    if (-not (Test-NetConnection -ComputerName $uri.Host -Port $port -InformationLevel Quiet)) {
        throw "$Name is unreachable at $Url. Start the dedicated QA environment first."
    }
}

function Invoke-ExternalCommand([string]$Path, [string[]]$Arguments) {
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $Path
    $startInfo.Arguments = [string]::Join(" ", $Arguments)
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.CreateNoWindow = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    if (-not $process.Start()) { throw "Unable to start $Path" }
    $standardOutput = $process.StandardOutput.ReadToEnd()
    $standardError = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    return [PSCustomObject]@{
        ExitCode = $process.ExitCode
        Output = "$standardOutput$standardError".Trim()
    }
}

function Test-Java17 {
    $java = Get-Command "java" -ErrorAction SilentlyContinue
    if (-not $java) {
        Write-Host "[FAIL] Java was not found on PATH. Solution: install a JDK 17 and restart the terminal."
        return $false
    }

    $result = Invoke-ExternalCommand $java.Source @("-version")
    $version = ($result.Output -split "`r?`n" | Select-Object -First 1)
    if ($result.ExitCode -ne 0 -or $version -notmatch 'version\s+"17(?:\.|"|\s)') {
        Write-Host "[FAIL] Java 17 is required; detected: $version. Solution: configure JDK 17 in PATH/JAVA_HOME."
        return $false
    }

    Write-Host "[OK] Java 17 detected: $version"
    return $true
}

function Test-Tool([string]$Name, [string[]]$Arguments, [string]$Solution) {
    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if (-not $command) {
        Write-Host "[FAIL] $Name was not found. Solution: $Solution"
        return $false
    }

    try {
        $result = Invoke-ExternalCommand $command.Source $Arguments
        $version = ($result.Output -split "`r?`n" | Select-Object -First 1)
        if ($result.ExitCode -ne 0) { throw "command returned exit code $($result.ExitCode)" }
        Write-Host "[OK] $Name available: $version"
        return $true
    } catch {
        Write-Host "[FAIL] $Name cannot run. Solution: $Solution Details: $($_.Exception.Message)"
        return $false
    }
}

function Test-RobotFramework {
    try {
        $python = Get-Python
        $result = Invoke-ExternalCommand $python @("-m", "robot", "--version")
        $version = ($result.Output -split "`r?`n" | Select-Object -First 1)
        if ($result.ExitCode -notin @(0, 251) -or $version -notmatch '^Robot Framework\s') {
            throw "Robot Framework returned exit code $($result.ExitCode)"
        }
        Write-Host "[OK] Robot Framework available: $version"
        return $true
    } catch {
        Write-Host "[FAIL] Robot Framework is unavailable. Solution: run 'python -m pip install -r robot-framework/requirements.txt'. Details: $($_.Exception.Message)"
        return $false
    }
}

function Test-PythonRuntime {
    try {
        $python = Get-Python
        $result = Invoke-ExternalCommand $python @("--version")
        $version = ($result.Output -split "`r?`n" | Select-Object -First 1)
        if ($result.ExitCode -ne 0) { throw "Python returned exit code $($result.ExitCode)" }
        Write-Host "[OK] Python available: $version"
        return $true
    } catch {
        Write-Host "[FAIL] Python 3 is unavailable. Solution: install Python 3 and add it to PATH. Details: $($_.Exception.Message)"
        return $false
    }
}

function Test-K6Support {
    $k6 = Get-Command "k6" -ErrorAction SilentlyContinue
    if ($k6) {
        return Test-Tool "k6" @("version") "install k6 or use Docker Desktop for the existing k6 command"
    }

    $docker = Get-Command "docker" -ErrorAction SilentlyContinue
    if (-not $docker) {
        Write-Host "[FAIL] k6 support is unavailable. Solution: install k6 or Docker Desktop (the current k6 command uses Docker)."
        return $false
    }

    try {
        $result = Invoke-ExternalCommand $docker.Source @("version", "--format={{.Server.Version}}")
        $version = ($result.Output -split "`r?`n" | Select-Object -First 1)
        if ($result.ExitCode -ne 0 -or [string]::IsNullOrWhiteSpace($version)) { throw "Docker engine is not running" }
        Write-Host "[OK] k6 support available through Docker engine $version"
        return $true
    } catch {
        Write-Host "[FAIL] k6 requires a running Docker engine. Solution: start Docker Desktop or install native k6. Details: $($_.Exception.Message)"
        return $false
    }
}

function Test-HttpEndpoint([string]$Url, [string]$Name, [bool]$AllowAuthenticationResponse) {
    try {
        $uri = [Uri]$Url
        if ($uri.Scheme -notin @("http", "https") -or -not $uri.Host) { throw "URL must be an absolute HTTP or HTTPS URL" }
    } catch {
        Write-Host "[FAIL] $Name URL is invalid: '$Url'. Solution: correct the matching URL in .env."
        return $false
    }

    try {
        $response = Invoke-WebRequest -Uri $uri -Method Get -TimeoutSec 10 -UseBasicParsing -ErrorAction Stop
        Write-Host "[OK] $Name responded with HTTP $($response.StatusCode): $uri"
        return $true
    } catch {
        $statusCode = $null
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }

        if ($AllowAuthenticationResponse -and $statusCode -in 401, 403) {
            Write-Host "[OK] $Name responded with HTTP $statusCode (authentication is expected): $uri"
            return $true
        }
        if ($statusCode -ge 400 -and $statusCode -lt 500) {
            Write-Warning "$Name is reachable and responded with HTTP ${statusCode}: $uri. Check the configured route if that response is unexpected."
            return $true
        }

        Write-Host "[FAIL] $Name did not respond successfully at $uri. Solution: start the service and verify its URL/port in .env. Details: $($_.Exception.Message)"
        return $false
    }
}

function Invoke-QACheck {
    $failures = 0
    Write-Host "QA readiness check (no tests or data changes will be performed)."

    try {
        Import-LocalEnv
        Write-Host "[OK] .env loaded from $EnvFile"
    } catch {
        Write-Host "[FAIL] Unable to load .env. Solution: copy .env.example to .env and complete the required values. Details: $($_.Exception.Message)"
        return 1
    }

    $requiredSettings = @(
        "QA_BASE_URL", "QA_API_BASE_URL",
        "QA_VALID_EMAIL", "QA_VALID_PASSWORD", "QA_VALID_LANDING_PATH",
        "QA_INVALID_EMAIL", "QA_INVALID_PASSWORD",
        "QA_ADMIN_EMAIL", "QA_ADMIN_PASSWORD",
        "QA_NON_ADMIN_EMAIL", "QA_NON_ADMIN_PASSWORD"
    )
    $missingSettings = @($requiredSettings | Where-Object {
        [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($_, "Process"))
    })
    if ($missingSettings.Count -gt 0) {
        $failures++
        Write-Host "[FAIL] Required QA settings are missing: $($missingSettings -join ', '). Solution: set them in .env. Values remain hidden."
    } else {
        Write-Host "[OK] Required QA settings are present ($($requiredSettings.Count) keys; sensitive values hidden)."
    }

    if (-not (Test-Java17)) { $failures++ }
    if (-not (Test-Tool "mvn" @("-version") "install Apache Maven and add it to PATH")) { $failures++ }
    if (-not (Test-PythonRuntime)) { $failures++ }
    if (-not (Test-RobotFramework)) { $failures++ }
    if (-not (Test-K6Support)) { $failures++ }

    $frontendUrl = [Environment]::GetEnvironmentVariable("QA_BASE_URL", "Process")
    $apiUrl = [Environment]::GetEnvironmentVariable("QA_API_BASE_URL", "Process")
    if ([string]::IsNullOrWhiteSpace($frontendUrl) -or -not (Test-HttpEndpoint $frontendUrl "Frontend" $false)) { $failures++ }
    if ([string]::IsNullOrWhiteSpace($apiUrl) -or -not (Test-HttpEndpoint $apiUrl "API gateway" $true)) { $failures++ }

    if ($failures -gt 0) {
        Write-Host "QA readiness check failed: $failures required check(s) need attention."
        return 1
    }

    Write-Host "QA readiness check passed."
    return 0
}

function Assert-IsolatedQaData {
    if ([Environment]::GetEnvironmentVariable("QA_TEST_DATA_ISOLATED", "Process") -ne "true") {
        throw "Fixture creation is blocked. Set QA_TEST_DATA_ISOLATED=true only after verifying an isolated QA database."
    }
    throw "No automatic fixture bootstrap is configured: the current endpoint contracts do not safely support creating the first QA admin/roles. Follow config/fixtures.local.example.json for the one-time administrator setup."
}

function Invoke-Selenium {
    Require-Command "mvn"
    Test-Service (Require-Setting "QA_BASE_URL") "Frontend"
    Push-Location $ProjectRoot
    try { & mvn test } finally { Pop-Location }
}

function Invoke-Robot {
    $python = Get-Python
    Test-Service (Require-Setting "QA_API_BASE_URL") "API gateway"
    Require-Setting "QA_VALID_EMAIL" | Out-Null
    Require-Setting "QA_VALID_PASSWORD" | Out-Null
    Push-Location $ProjectRoot
    try { & $python -m robot --outputdir reports/robot robot-framework/api } finally { Pop-Location }
}

function Invoke-K6Smoke {
    Require-Command "docker"
    $apiBaseUrl = Require-Setting "QA_API_BASE_URL"
    Test-Service $apiBaseUrl "API gateway"
    $email = Require-Setting "QA_VALID_EMAIL"
    $password = Require-Setting "QA_VALID_PASSWORD"
    if ([Environment]::GetEnvironmentVariable("K6_ALLOW_LOCAL", "Process") -ne "true") {
        throw "K6 smoke is blocked without K6_ALLOW_LOCAL=true. Load and stress are never included in 'all'."
    }
    $loginBody = @{ email = $email; password = $password } | ConvertTo-Json -Compress
    $login = Invoke-RestMethod -Method Post -Uri "$apiBaseUrl/api/auth/login" -ContentType "application/json" -Body $loginBody
    if (-not $login.accessToken -or -not $login.refreshToken) { throw "QA login did not return the required token pair." }
    $containerApiUrl = ([Uri]$apiBaseUrl).GetComponents([UriComponents]::SchemeAndServer, [UriFormat]::UriEscaped) -replace '://(localhost|127\.0\.0\.1)', '://host.docker.internal'
    try {
        Push-Location $ProjectRoot
        $env:K6_ACCESS_TOKEN = $login.accessToken
        & docker run --rm -e "K6_BASE_URL=$containerApiUrl" -e K6_ACCESS_TOKEN -e "K6_ALLOW_TEST_ENVIRONMENT=true" -v "${ProjectRoot}:/work" -w /work grafana/k6:latest run performance/k6/smoke/projects-read.js
        if ($LASTEXITCODE -ne 0) { throw "k6 failed with exit code $LASTEXITCODE." }
    } finally {
        try { Invoke-RestMethod -Method Post -Uri "$apiBaseUrl/api/auth/logout" -Headers @{ Authorization = "Bearer $($login.accessToken)" } -ContentType "application/json" -Body (@{ refreshToken = $login.refreshToken } | ConvertTo-Json -Compress) | Out-Null } catch { Write-Warning "The temporary k6 session could not be revoked." }
        Remove-Item Env:K6_ACCESS_TOKEN -ErrorAction SilentlyContinue
        Pop-Location -ErrorAction SilentlyContinue
    }
}

if ($Target -eq "check") {
    exit (Invoke-QACheck)
}

Import-LocalEnv
switch ($Target) {
    "setup" { Get-Python | ForEach-Object { & $_ -m robot --version }; Require-Command "mvn"; Require-Command "docker"; Write-Host "QA tooling is available." }
    "selenium" { Invoke-Selenium }
    "robot" { Invoke-Robot }
    "k6" { Invoke-K6Smoke }
    "all" { Invoke-Selenium; Invoke-Robot; Invoke-K6Smoke }
    "cleanup" { Assert-IsolatedQaData }
}
