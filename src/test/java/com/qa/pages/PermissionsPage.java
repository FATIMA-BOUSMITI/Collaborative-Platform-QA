package com.qa.pages;

import java.time.Duration;
import java.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

public class PermissionsPage {
  private final WebDriver driver;
  private final WebDriverWait wait;
  private final String baseUrl;
  private final Map<String, Integer> roleColumns = new HashMap<>();
  private final Map<String, String> permissionNames = new HashMap<>();

  public PermissionsPage(WebDriver driver, Duration timeout, String baseUrl) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, timeout);
    this.baseUrl = baseUrl;
  }

  public void open() {
    UiSupport.open(driver, wait, baseUrl + "/permissions", "Permissions");
    wait.until(
        d -> {
          UiSupport.failOnError(d);
          return !d.findElements(
                  By.cssSelector(
                      "[data-testid='permissions-page'] tbody tr input[type='checkbox']"))
              .isEmpty();
        });
  }

  public String selectEditableRole(String role) {
    List<WebElement> headers =
        driver.findElements(By.cssSelector("[data-testid='permissions-page'] thead th"));
    for (int i = 1; i < headers.size(); i++)
      if (role.equals(headers.get(i).getText().trim())) {
        for (WebElement row :
            driver.findElements(By.cssSelector("[data-testid='permissions-page'] tbody tr"))) {
          WebElement box =
              row.findElements(By.tagName("td")).get(i - 1).findElement(By.tagName("input"));
          if (box.isEnabled()) {
            roleColumns.put(role, i - 1);
            permissionNames.put(
                role,
                row.findElement(By.cssSelector("th[scope='row']")).getDomProperty("textContent"));
            return role;
          }
        }
      }
    throw new IllegalStateException("No editable permission found for dedicated QA role " + role);
  }

  public void discardUnsavedChanges() {
    driver.findElements(UiSupport.button("Annuler les modifications")).stream()
        .filter(WebElement::isEnabled)
        .findFirst()
        .ifPresent(WebElement::click);
  }

  public String firstNonAdminRoleId() {
    List<WebElement> headers =
        driver.findElements(By.cssSelector("[data-testid='permissions-page'] thead th"));
    for (int i = 1; i < headers.size(); i++) {
      String role = headers.get(i).getText().trim();
      if (role.toUpperCase(Locale.ROOT).contains("ADMIN")) continue;
      for (WebElement row :
          driver.findElements(By.cssSelector("[data-testid='permissions-page'] tbody tr"))) {
        WebElement box =
            row.findElements(By.tagName("td")).get(i - 1).findElement(By.tagName("input"));
        if (box.isEnabled()) {
          roleColumns.put(role, i - 1);
          String permission =
              row.findElement(By.cssSelector("th[scope='row']")).getDomProperty("textContent");
          permissionNames.put(role, permission);
          return role;
        }
      }
    }
    throw new IllegalStateException(
        "QA fixture missing: no editable permission exists for a non-admin role");
  }

  public String firstPermissionId() {
    if (permissionNames.isEmpty()) throw new IllegalStateException("Select an editable role first");
    return permissionNames.values().iterator().next();
  }

  public boolean isPermissionChecked(String role, String permission) {
    return checkbox(role, permission).isSelected();
  }

  public void togglePermission(String role, String permission) {
    wait.until(ExpectedConditions.elementToBeClickable(checkbox(role, permission))).click();
  }

  public void save() {
    wait.until(
            ExpectedConditions.elementToBeClickable(
                UiSupport.button("Enregistrer les modifications")))
        .click();
    wait.until(
        d -> {
          UiSupport.failOnError(d);
          return d.findElements(By.cssSelector(".admin-feedback.success")).stream()
              .anyMatch(e -> e.isDisplayed() && e.getText().contains("enregistrées"));
        });
  }

  private WebElement checkbox(String role, String permission) {
    if (!roleColumns.containsKey(role)) throw new IllegalArgumentException("Unknown selected role");
    List<WebElement> headers =
        driver.findElements(By.cssSelector("[data-testid='permissions-page'] thead th"));
    int index =
        java.util.stream.IntStream.range(1, headers.size())
                .filter(i -> role.equals(headers.get(i).getText().trim()))
                .findFirst()
                .orElseThrow()
            - 1;
    return wait.until(
        d ->
            d.findElements(By.cssSelector("[data-testid='permissions-page'] tbody tr")).stream()
                .filter(
                    r ->
                        permission.equals(
                            r.findElement(By.cssSelector("th[scope='row']"))
                                .getDomProperty("textContent")))
                .map(
                    r ->
                        r.findElements(By.tagName("td"))
                            .get(index)
                            .findElement(By.tagName("input")))
                .findFirst()
                .orElse(null));
  }
}
