package com.qa.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

final class UiSupport {
  private UiSupport() {}

  static String literal(String value) {
    if (!value.contains("'")) return "'" + value + "'";
    return "concat('" + value.replace("'", "',\"'\",'") + "')";
  }

  static By button(String text) {
    return By.xpath("//button[normalize-space(.)=" + literal(text) + "]");
  }

  static By label(String text) {
    return By.xpath(
        ".//label[normalize-space(text())="
            + literal(text)
            + "]/*[self::input or self::select or self::textarea]");
  }

  static WebElement dialog(WebDriver d, WebDriverWait w) {
    return w.until(
        ExpectedConditions.visibilityOfElementLocated(By.cssSelector("dialog.admin-modal[open]")));
  }

  static void fill(WebElement scope, String label, String value) {
    WebElement input = scope.findElement(label(label));
    input.clear();
    if (!value.isEmpty()) input.sendKeys(value);
  }

  static void firstOption(WebElement select) {
    Select s = new Select(select);
    s.getOptions().stream()
        .filter(o -> !o.getDomProperty("value").isBlank() && o.isEnabled())
        .findFirst()
        .ifPresentOrElse(
            o -> s.selectByValue(o.getDomProperty("value")),
            () -> {
              throw new IllegalStateException(
                  "QA fixture missing: no selectable option for " + select.getText());
            });
  }

  static void failOnError(WebDriver d) {
    d.findElements(By.cssSelector(".admin-feedback.error[role='alert']")).stream()
        .filter(WebElement::isDisplayed)
        .findFirst()
        .ifPresent(
            e -> {
              throw new IllegalStateException("Application rejected the operation: " + e.getText());
            });
  }

  static void submit(WebDriver d, WebDriverWait w) {
    WebElement modal = dialog(d, w);
    modal.findElement(By.cssSelector("form .primary-button,form .collab-primary")).click();
    w.until(
        x -> {
          failOnError(x);
          return x.findElements(By.cssSelector("dialog[open]")).isEmpty();
        });
  }

  static void open(WebDriver d, WebDriverWait w, String url, String title) {
    d.get(url);
    w.withMessage("Expected page " + title + " at " + url)
        .until(
            x -> {
              failOnError(x);
              return x.findElements(By.cssSelector("main h1")).stream()
                  .anyMatch(h -> h.isDisplayed() && title.equals(h.getText().trim()));
            });
  }

  static void confirm(WebDriver d, WebDriverWait w) {
    w.until(ExpectedConditions.alertIsPresent()).accept();
  }
}
