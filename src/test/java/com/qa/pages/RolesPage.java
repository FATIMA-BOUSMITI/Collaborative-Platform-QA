package com.qa.pages;

import java.time.Duration;
import java.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

public class RolesPage {
  private final WebDriver driver;
  private final WebDriverWait wait;
  private final String baseUrl;
  private String targetEmail;
  private static final By SEARCH = By.cssSelector("input[placeholder='Nom ou email']");

  public RolesPage(WebDriver d, Duration timeout, String baseUrl) {
    driver = d;
    wait = new WebDriverWait(d, timeout);
    this.baseUrl = baseUrl;
  }

  public void open() {
    UiSupport.open(driver, wait, baseUrl + "/roles", "Rôles et accès");
    wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH));
  }

  public void createTestRole(String name) {
    if (!name.startsWith("QA_SELENIUM_"))
      throw new IllegalArgumentException("Only QA roles may be created by this helper");
    wait.until(ExpectedConditions.elementToBeClickable(UiSupport.button("Créer un rôle"))).click();
    WebElement modal = UiSupport.dialog(driver, wait);
    UiSupport.fill(modal, "Nom", name);
    UiSupport.fill(modal, "Description", "Isolated Selenium permission fixture");
    UiSupport.submit(driver, wait);
    wait.until(d -> roleCard(name) != null);
  }

  public void deleteTestRoleIfPresent(String name) {
    if (!name.startsWith("QA_SELENIUM_"))
      throw new IllegalArgumentException("Only tracked QA roles may be deleted");
    WebElement card = roleCard(name);
    if (card == null) return;
    card.findElements(By.tagName("button")).stream()
        .filter(b -> "Supprimer".equals(b.getText().trim()))
        .findFirst()
        .orElseThrow()
        .click();
    UiSupport.confirm(driver, wait);
    wait.until(
        d -> {
          UiSupport.failOnError(d);
          return roleCard(name) == null;
        });
  }

  private WebElement roleCard(String name) {
    return driver.findElements(By.cssSelector("main article.admin-card")).stream()
        .filter(
            c ->
                c.findElements(By.tagName("h2")).stream()
                    .anyMatch(h -> name.equals(h.getText().trim())))
        .findFirst()
        .orElse(null);
  }

  public List<String> getColumnHeaders() {
    return driver.findElements(By.cssSelector(".admin-table-wrap thead th")).stream()
        .map(h -> h.getDomProperty("textContent").trim())
        .toList();
  }

  public void searchFor(String value) {
    WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH));
    input.clear();
    input.sendKeys(value);
  }

  public List<String> getVisibleEmails() {
    return driver.findElements(By.cssSelector(".admin-table-wrap tbody tr")).stream()
        .map(r -> r.findElements(By.tagName("td")).get(1).getText().trim())
        .toList();
  }

  public String getRoleForEmail(String email) {
    return new Select(wait.until(d -> find(email)).findElement(By.tagName("select")))
        .getFirstSelectedOption()
        .getText()
        .trim();
  }

  public void openRoleEditorFor(String email) {
    if (!email.matches("qa\\.selenium\\.[0-9a-f]{32}@example\\.invalid"))
      throw new IllegalArgumentException("Only generated QA users may have their role changed");
    targetEmail = email;
    wait.until(d -> find(email)).findElement(By.tagName("select")).click();
  }

  public List<String> getNonAdminRoleOptions() {
    return new Select(wait.until(d -> find(targetEmail)).findElement(By.tagName("select")))
        .getOptions().stream()
            .filter(
                o ->
                    !o.getDomProperty("value").isBlank()
                        && !o.getText().toUpperCase(Locale.ROOT).contains("ADMIN"))
            .map(o -> o.getText().trim())
            .toList();
  }

  public void selectRoleAndSave(String role) {
    WebElement select = wait.until(d -> find(targetEmail)).findElement(By.tagName("select"));
    new Select(select).selectByVisibleText(role);
    UiSupport.confirm(driver, wait);
    wait.until(
        d -> {
          UiSupport.failOnError(d);
          return d.findElements(By.cssSelector(".admin-feedback.success")).stream()
              .anyMatch(e -> e.isDisplayed() && e.getText().contains("Rôle attribué"));
        });
    wait.until(d -> role.equals(getRoleForEmail(targetEmail)));
  }

  private WebElement find(String email) {
    return driver.findElements(By.cssSelector(".admin-table-wrap tbody tr")).stream()
        .filter(
            r -> {
              List<WebElement> cells = r.findElements(By.tagName("td"));
              return cells.size() >= 3 && email.equals(cells.get(1).getText().trim());
            })
        .findFirst()
        .orElse(null);
  }
}
