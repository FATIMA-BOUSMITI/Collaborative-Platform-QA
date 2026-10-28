package com.qa.pages;

import java.time.Duration;
import java.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

public class KanbanPage {
  private final WebDriver driver;
  private final WebDriverWait wait;
  private final String baseUrl;
  private static final By PAGE = By.cssSelector("[data-testid='kanban-page']"),
      CARDS = By.cssSelector(".collab-task");

  public KanbanPage(WebDriver d, Duration timeout, String baseUrl) {
    driver = d;
    wait = new WebDriverWait(d, timeout);
    this.baseUrl = baseUrl;
  }

  public void open() {
    driver.get(baseUrl + "/projects");
    wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE));
    wait.until(
        d -> {
          UiSupport.failOnError(d);
          return d.findElements(By.cssSelector(".collab-toolbar label select option")).stream()
                  .anyMatch(o -> !o.getDomProperty("value").isBlank())
              || !d.findElements(By.cssSelector(".collab-page > .admin-empty")).isEmpty();
        });
    WebElement project = driver.findElement(By.cssSelector(".collab-toolbar label select"));
    if (!project.getDomProperty("value").isBlank())
      wait.until(
          d -> {
            UiSupport.failOnError(d);
            return !d.findElements(By.cssSelector(".collab-column")).isEmpty();
          });
  }

  public boolean isBoardVisible() {
    return driver.findElements(PAGE).stream().anyMatch(WebElement::isDisplayed);
  }

  public boolean hasTaskCreationAction() {
    return !driver.findElements(By.cssSelector(".collab-add-task")).isEmpty();
  }

  public List<String> getColumnTitles() {
    wait.until(d -> !d.findElements(By.cssSelector(".collab-column")).isEmpty());
    return driver.findElements(By.cssSelector(".collab-column header h2")).stream()
        .map(WebElement::getText)
        .toList();
  }

  public List<String> getTaskTitles() {
    return driver.findElements(By.cssSelector(".collab-task-title")).stream()
        .map(WebElement::getText)
        .toList();
  }

  public void openFirstTaskForm() {
    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".collab-add-task"))).click();
    UiSupport.dialog(driver, wait);
  }

  public String getTaskModalStatus() {
    return UiSupport.dialog(driver, wait).findElement(By.tagName("h2")).getText();
  }

  public void closeTaskForm() {
    closeDialog();
  }

  public void openProjectForm() {
    wait.until(ExpectedConditions.elementToBeClickable(UiSupport.button("Nouveau projet"))).click();
    UiSupport.dialog(driver, wait);
  }

  public boolean isProjectFormVisible() {
    return driver.findElements(By.cssSelector("dialog[open][aria-label='Nouveau projet']")).stream()
        .anyMatch(WebElement::isDisplayed);
  }

  public String getProjectFormTitle() {
    return UiSupport.dialog(driver, wait).findElement(By.tagName("h2")).getText();
  }

  public void closeProjectForm() {
    closeDialog();
  }

  public boolean isProjectViewAvailable() {
    return !driver.findElements(UiSupport.button("Projets")).isEmpty();
  }

  public void createTask(String title) {
    WebElement column =
        wait.withMessage(
                "QA fixture missing or unavailable: configure an accessible writable project with a"
                    + " TODO column")
            .until(
                d -> {
                  UiSupport.failOnError(d);
                  return column("TODO");
                });
    column.findElement(By.cssSelector(".collab-add-task")).click();
    UiSupport.fill(UiSupport.dialog(driver, wait), "Titre", title);
    UiSupport.firstOption(UiSupport.dialog(driver, wait).findElement(UiSupport.label("Collaborateur")));
    String dueDate = java.time.LocalDate.now().plusDays(7).toString();
    WebElement dueDateInput = UiSupport.dialog(driver, wait).findElement(UiSupport.label("Échéance"));
    // Use the ISO date value rather than Chrome's locale-dependent date keyboard segments.
    ((JavascriptExecutor) driver).executeScript(
        "const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;"
            + "setter.call(arguments[0], arguments[1]);"
            + "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));"
            + "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
        dueDateInput, dueDate);
    wait.until(d -> dueDate.equals(dueDateInput.getDomProperty("value")));
    UiSupport.submit(driver, wait);
    wait.until(d -> find(title) != null);
  }

  public String getTaskStatus(String title) {
    return status(wait.until(d -> find(title)));
  }

  public void moveTaskToStatus(String title, String state) {
    WebElement source = wait.until(d -> find(title)).findElement(By.cssSelector(".collab-drag"));
    WebElement target = wait.until(d -> column(state));
    new Actions(driver)
        .moveToElement(source)
        .clickAndHold()
        .moveByOffset(12, 0)
        .moveToElement(target)
        .release()
        .perform();
    wait.until(
        d -> {
          UiSupport.failOnError(d);
          WebElement card = find(title);
          return card != null
              && state.equals(status(card))
              && card.findElement(By.cssSelector(".collab-drag")).isEnabled();
        });
  }

  public void archiveTask(String title) {
    if (!title.startsWith("QA Kanban "))
      throw new IllegalArgumentException("Only tracked QA tasks may be archived");
    wait.until(d -> find(title)).findElement(By.cssSelector(".collab-task-title")).click();
    UiSupport.dialog(driver, wait);
    wait.until(ExpectedConditions.elementToBeClickable(UiSupport.button("Archiver"))).click();
    UiSupport.confirm(driver, wait);
    wait.until(
        d -> {
          UiSupport.failOnError(d);
          return find(title) == null && d.findElements(By.cssSelector("dialog[open]")).isEmpty();
        });
  }

  /** Legacy method retained; current application offers archiving, not hard deletion. */
  @Deprecated
  public void deleteTask(String title) {
    archiveTask(title);
  }

  private void closeDialog() {
    UiSupport.dialog(driver, wait)
        .findElement(By.cssSelector("button[aria-label='Fermer']"))
        .click();
    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("dialog[open]")));
  }

  private WebElement column(String state) {
    if (!Set.of("TODO", "IN_PROGRESS", "REVIEW", "DONE").contains(state))
      throw new IllegalArgumentException("Unsupported board state");
    return driver.findElements(By.cssSelector(".collab-column")).stream()
        .filter(
            c ->
                !c.findElements(
                        By.cssSelector(".collab-status-dot." + state.toLowerCase(Locale.ROOT)))
                    .isEmpty())
        .findFirst()
        .orElse(null);
  }

  private String status(WebElement task) {
    WebElement column =
        task.findElement(
            By.xpath(
                "ancestor::section[contains(concat(' ',normalize-space(@class),' '),' collab-column"
                    + " ')][1]"));
    for (String state : List.of("TODO", "IN_PROGRESS", "REVIEW", "DONE"))
      if (!column
          .findElements(By.cssSelector(".collab-status-dot." + state.toLowerCase(Locale.ROOT)))
          .isEmpty()) return state;
    throw new IllegalStateException("No canonical state on task column");
  }

  private WebElement find(String title) {
    return driver.findElements(CARDS).stream()
        .filter(
            c ->
                c.findElements(By.cssSelector(".collab-task-title")).stream()
                    .anyMatch(t -> title.equals(t.getText().trim())))
        .findFirst()
        .orElse(null);
  }
}
