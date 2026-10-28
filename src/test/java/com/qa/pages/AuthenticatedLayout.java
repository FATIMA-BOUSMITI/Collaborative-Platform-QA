package com.qa.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AuthenticatedLayout {

  private static final By LOGOUT_BUTTON =
      By.xpath("//button[contains(normalize-space(.), 'Déconnexion')]");

  private final WebDriver driver;
  private final WebDriverWait wait;

  public AuthenticatedLayout(WebDriver driver, Duration timeout) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, timeout);
  }

  public void logOut() {
    wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON)).click();
  }
}
