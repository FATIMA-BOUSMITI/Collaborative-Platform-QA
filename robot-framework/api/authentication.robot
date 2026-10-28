*** Settings ***
Documentation    Read-only JWT authentication checks against a dedicated QA account.
Resource         ../resources/auth.resource
Library          Collections
Library          RequestsLibrary
Suite Setup      Require API configuration
Test Tags        api    auth    non-destructive

*** Test Cases ***
Valid QA login returns a token pair
    Create API session
    ${response}=    Authenticate QA account
    Should Be Equal As Integers    ${response.status_code}    200
    ${body}=    Evaluate    $response.json()
    Dictionary Should Contain Key    ${body}    accessToken
    Dictionary Should Contain Key    ${body}    refreshToken
    Should Not Be Empty    ${body}[accessToken]
    Should Not Be Empty    ${body}[refreshToken]

Invalid credentials do not authenticate
    Create API session
    ${payload}=    Create Dictionary    email=qa.invalid@example.invalid    password=not-a-real-password
    ${response}=    POST On Session    api    /api/auth/login    json=${payload}    expected_status=any
    Should Not Be Equal As Integers    ${response.status_code}    200
