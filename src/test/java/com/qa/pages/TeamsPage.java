package com.qa.pages;

import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

public class TeamsPage {
  private final WebDriver driver;
  private final WebDriverWait wait;
  private final String baseUrl;

  public TeamsPage(WebDriver driver, Duration timeout, String baseUrl) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, timeout);
    this.baseUrl = baseUrl;
  }

  public void open() {
    UiSupport.open(driver, wait, baseUrl + "/teams", "Gestion des équipes");
    wait.until(ExpectedConditions.elementToBeClickable(UiSupport.button("Nouvelle équipe")));
  }

  public void create(String name) {
    wait.until(ExpectedConditions.elementToBeClickable(UiSupport.button("Nouvelle équipe")))
        .click();
    WebElement form = UiSupport.dialog(driver, wait);
    UiSupport.fill(form, "Nom", name);
    UiSupport.firstOption(form.findElement(UiSupport.label("Département")));
    UiSupport.fill(form, "Objectif", "QA Automation");
    UiSupport.submit(driver, wait);
    wait.until(d -> find(name) != null);
  }

  public void rename(String old, String name) {
    action(wait.until(d -> find(old)), "Modifier");
    UiSupport.fill(UiSupport.dialog(driver, wait), "Nom", name);
    UiSupport.submit(driver, wait);
    wait.until(d -> find(name) != null && find(old) == null);
  }

  public void delete(String name) {
    if (!name.startsWith("QA team "))
      throw new IllegalArgumentException("Only generated QA teams may be deleted");
    action(wait.until(d -> find(name)), "Supprimer");
    UiSupport.confirm(driver, wait);
    wait.until(
        d -> {
          UiSupport.failOnError(d);
          return find(name) == null;
        });
  }

  public boolean contains(String name) {
    return find(name) != null;
  }

  private WebElement find(String name) {
    return driver.findElements(By.cssSelector("main article.admin-card")).stream()
        .filter(
            c ->
                c.findElements(By.tagName("h2")).stream()
                    .anyMatch(h -> name.equals(h.getText().trim())))
        .findFirst()
        .orElse(null);
  }

  private void action(WebElement card, String text) {
    card.findElements(By.tagName("button")).stream()
        .filter(b -> text.equals(b.getText().trim()))
        .findFirst()
        .orElseThrow()
        .click();
  }
}
