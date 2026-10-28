package com.qa.pages;

import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

public class DepartmentsPage {
  private final WebDriver driver;
  private final WebDriverWait wait;
  private final String baseUrl;

  public DepartmentsPage(WebDriver driver, Duration timeout, String baseUrl) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, timeout);
    this.baseUrl = baseUrl;
  }

  public void open() {
    UiSupport.open(driver, wait, baseUrl + "/departments", "Organisation & Départements");
    wait.until(
        d -> {
          UiSupport.failOnError(d);
          return d.findElements(By.cssSelector(".reference-departments")).stream()
              .anyMatch(WebElement::isDisplayed);
        });
  }

  public void create(String name) {
    wait.until(ExpectedConditions.elementToBeClickable(UiSupport.button("Nouveau département")))
        .click();
    WebElement form = UiSupport.dialog(driver, wait);
    UiSupport.fill(form, "Nom", name);
    UiSupport.fill(
        form,
        "Code unique",
        "QA" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12));
    UiSupport.firstOption(form.findElement(UiSupport.label("Responsable")));
    UiSupport.fill(form, "Description", "Created by isolated Selenium QA");
    UiSupport.submit(driver, wait);
    wait.until(d -> find(name) != null);
  }

  public void rename(String old, String name) {
    WebElement card = wait.until(d -> find(old));
    action(card, "Modifier");
    UiSupport.fill(UiSupport.dialog(driver, wait), "Nom", name);
    UiSupport.submit(driver, wait);
    wait.until(d -> find(name) != null && find(old) == null);
  }

  public boolean contains(String name) {
    return find(name) != null;
  }

  public void deleteIfPresent(String name) {
    if (!name.matches("Q[A-F0-9]{3} QA department.*"))
      throw new IllegalArgumentException("Only this test's generated departments may be removed");
    WebElement card = find(name);
    if (card == null) return;
    action(card, "Supprimer");
    UiSupport.confirm(driver, wait);
    wait.until(
        d -> {
          UiSupport.failOnError(d);
          return find(name) == null;
        });
  }

  private WebElement find(String name) {
    return driver.findElements(By.cssSelector("article.reference-department")).stream()
        .filter(
            c ->
                c.findElements(By.tagName("h2")).stream()
                    .anyMatch(h -> name.equals(h.getText().trim())))
        .findFirst()
        .orElse(null);
  }

  private void action(WebElement card, String text) {
    card.findElement(By.cssSelector("details summary")).click();
    wait.until(
            d ->
                card.findElements(By.tagName("button")).stream()
                    .filter(b -> b.isDisplayed() && text.equals(b.getText().trim()))
                    .findFirst()
                    .orElse(null))
        .click();
  }
}
