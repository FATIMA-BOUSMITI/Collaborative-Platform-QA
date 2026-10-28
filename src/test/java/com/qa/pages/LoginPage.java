package com.qa.pages;

import java.net.URI;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {

  private static final By EMAIL_INPUT = By.cssSelector("input[type='email']");
  private static final By PASSWORD_INPUT = By.cssSelector("input[type='password']");
  private static final By SUBMIT_BUTTON = By.cssSelector("form.login-form button[type='submit']");
  private static final By ERROR_MESSAGE = By.cssSelector(".error-box");

  private final WebDriver driver;
  private final WebDriverWait wait;
  private final String baseUrl;

  public LoginPage(WebDriver driver, Duration timeout, String baseUrl) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, timeout);
    this.baseUrl = baseUrl;
  }

  public void open() {
    driver.get(baseUrl + "/");
    waitForLoginFormWithOneRefresh();
  }

  public void signIn(String email, String password) {
    WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT));
    emailInput.clear();
    emailInput.sendKeys(email);

    WebElement passwordInput =
        wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
    passwordInput.clear();
    passwordInput.sendKeys(password);
    wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
  }

  public void submitEmptyForm() {
    wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
  }

  public String waitForLandingPath() {
    return wait.until(
        d -> {
          for (WebElement error : d.findElements(ERROR_MESSAGE))
            if (error.isDisplayed() && !error.getText().isBlank())
              throw new IllegalStateException("Login rejected by application: " + error.getText());
          String path = applicationPath(URI.create(d.getCurrentUrl()));
          return java.util.Set.of("/dashboard", "/director", "/manager", "/employee").contains(path)
              ? path
              : null;
        });
  }

  private void waitForLoginFormWithOneRefresh() {
    try {
      wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT));
    } catch (TimeoutException firstAttemptFailure) {
      driver.navigate().refresh();
      wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT));
    }
  }

  public String waitForLoginError() {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE)).getText();
  }

  public boolean isLoginFormVisible() {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(SUBMIT_BUTTON)).isDisplayed();
  }

  private String normalizedPath(String path) {
    if (path == null || path.isBlank() || "/".equals(path)) {
      return "/";
    }
    return path.replaceFirst("/+$", "");
  }

  private String applicationPath(URI uri) {
    String fragment = uri.getFragment();
    if (fragment != null && fragment.startsWith("/")) {
      return normalizedPath(fragment);
    }
    return normalizedPath(uri.getPath());
  }
}
