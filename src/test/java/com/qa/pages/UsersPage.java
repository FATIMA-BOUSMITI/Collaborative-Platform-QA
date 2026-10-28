package com.qa.pages;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class UsersPage {

  private static final By PAGE = By.cssSelector(".users-page");
  private static final By TABLE = By.cssSelector(".users-table table");
  private static final By USER_ROWS =
      By.cssSelector(".users-table tbody tr[data-testid='user-row']");
  private static final By USER_LIST_ERROR = By.cssSelector("[data-testid='user-list-error']");
  private static final By USERS_LOADING = By.cssSelector("[data-testid='users-loading']");
  private static final By USERS_PAGE_READY =
      By.cssSelector("[data-testid='users-page-ready'][data-users-state='ready']");
  private static final By USER_DELETE_ERROR = By.cssSelector("[data-testid='user-delete-error']");
  private static final By USER_DELETE_STATUS = By.cssSelector("[data-testid='user-delete-status']");
  private static final By SEARCH_INPUT =
      By.cssSelector("input[placeholder='Rechercher un utilisateur...']");
  private static final By CREATE_BUTTON = By.cssSelector(".add-user-button");
  private static final By CREATE_MODAL =
      By.cssSelector("dialog.admin-modal[aria-label='Nouvel utilisateur']");
  private static final By CREATE_ERROR =
      By.cssSelector("dialog.admin-modal[aria-label='Nouvel utilisateur'] .admin-feedback.error");
  private static final By CREATE_MODAL_ERROR = By.cssSelector(".admin-feedback.error");
  private static final By CREATE_SUBMIT_BUTTON =
      By.cssSelector("dialog.admin-modal[aria-label='Nouvel utilisateur'] .primary-button");
  private static final By EDIT_MODAL =
      By.cssSelector("dialog.admin-modal[aria-label='Modifier l’utilisateur']");
  private static final Duration PAGE_LOAD_TIMEOUT = Duration.ofSeconds(60);
  private static final Duration CREATE_RESULT_TIMEOUT = Duration.ofSeconds(60);
  private static final Pattern TEST_USER_EMAIL =
      Pattern.compile("qa\\.selenium\\.[0-9a-f]{32}@example\\.invalid");
  private static final Set<String> PROTECTED_ADMIN_ROLES =
      Set.of(
          "ADMIN",
          "ROLE_ADMIN",
          "ADMINISTRATOR",
          "ROLE_ADMINISTRATOR",
          "ADMINISTRATEUR",
          "ROLE_ADMINISTRATEUR");

  private final WebDriver driver;
  private final WebDriverWait wait;
  private final Duration timeout;
  private final String baseUrl;

  public UsersPage(WebDriver driver, Duration timeout, String baseUrl) {
    this.driver = driver;
    this.timeout = timeout;
    this.wait = new WebDriverWait(driver, timeout);
    this.baseUrl = baseUrl;
  }

  public void open() {
    String requestedUrl = baseUrl + "/users";
    System.out.println("URL demandée : " + requestedUrl);
    driver.get(requestedUrl);
    System.out.println("URL actuelle : " + driver.getCurrentUrl());

    waitForPageContent(new WebDriverWait(driver, PAGE_LOAD_TIMEOUT));
  }

  public List<String> getColumnHeaders() {
    return driver.findElements(By.cssSelector(".users-table thead th")).stream()
        .map(header -> header.getDomProperty("textContent").trim())
        .toList();
  }

  public int getVisibleUserCount() {
    return driver.findElements(USER_ROWS).size();
  }

  public List<String> getVisibleUserEmails() {
    return driver.findElements(USER_ROWS).stream()
        .map(row -> row.findElements(By.tagName("td")).get(1).getText().trim())
        .toList();
  }

  public boolean isUserVisible(String email) {
    searchFor(email);
    return findRowByEmail(email).isPresent();
  }

  public boolean waitForUser(String email) {
    searchFor(email);
    return wait.until(currentDriver -> findRowByEmail(email).isPresent());
  }

  public String getUserName(String email) {
    searchFor(email);
    WebElement row = wait.until(currentDriver -> findRowByEmail(email).orElse(null));
    return row.findElement(By.cssSelector("td h4")).getText();
  }

  public String getUserDepartment(String email) {
    searchFor(email);
    WebElement row = wait.until(currentDriver -> findRowByEmail(email).orElse(null));
    return row.findElements(By.tagName("td")).get(2).getText().trim();
  }

  public String getUserRole(String email) {
    searchFor(email);
    WebElement row = wait.until(currentDriver -> findRowByEmail(email).orElse(null));
    return row.findElements(By.tagName("td")).get(3).getText().trim();
  }

  public void searchFor(String value) {
    WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
    input.clear();
    input.sendKeys(value);
  }

  public void refresh() {
    driver.navigate().refresh();
    waitForPageContent(new WebDriverWait(driver, PAGE_LOAD_TIMEOUT));
  }

  public void openCreateModal() {
    wait.until(ExpectedConditions.elementToBeClickable(CREATE_BUTTON)).click();
    wait.until(ExpectedConditions.visibilityOfElementLocated(CREATE_MODAL));
    wait.until(
        currentDriver -> {
          List<WebElement> selects =
              currentDriver.findElements(
                  By.cssSelector("dialog.admin-modal[aria-label='Nouvel utilisateur'] select"));
          return selects.size() == 2
              && hasSelectableOption(selects.get(0))
              && hasSelectableOption(selects.get(1));
        });
  }

  public void fillCreateUserForm(String firstName, String lastName, String email) {
    WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(CREATE_MODAL));
    List<WebElement> textInputs = modal.findElements(By.cssSelector("input[type='text']"));
    textInputs.get(0).clear();
    textInputs.get(0).sendKeys(firstName);
    textInputs.get(1).clear();
    textInputs.get(1).sendKeys(lastName);

    WebElement emailInput = modal.findElement(By.cssSelector("input[type='email']"));
    emailInput.clear();
    emailInput.sendKeys(email);

    List<WebElement> selects = modal.findElements(By.tagName("select"));
    selectFirstAvailableOption(selects.get(0));
    selectNonAdminRole(selects.get(1));
  }

  public void leaveDepartmentUnselected() {
    new Select(
            wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("dialog.admin-modal[aria-label='Nouvel utilisateur'] select"))))
        .selectByIndex(0);
  }

  public void leaveRoleUnselected() {
    List<WebElement> selects =
        wait.until(
            currentDriver -> {
              List<WebElement> elements =
                  currentDriver.findElements(
                      By.cssSelector("dialog.admin-modal[aria-label='Nouvel utilisateur'] select"));
              return elements.size() == 2 ? elements : null;
            });
    new Select(selects.get(1)).selectByIndex(0);
  }

  public void submitCreateForm() {
    wait.until(ExpectedConditions.elementToBeClickable(CREATE_SUBMIT_BUTTON)).click();
  }

  public CreateResult waitForCreateResult() {
    WebDriverWait createWait =
        new WebDriverWait(driver, CREATE_RESULT_TIMEOUT, Duration.ofMillis(250));
    createWait.ignoring(StaleElementReferenceException.class);
    try {
      return createWait.until(
          currentDriver -> {
            List<WebElement> modals = currentDriver.findElements(CREATE_MODAL);
            if (modals.isEmpty() || !modals.get(0).isDisplayed()) {
              return CreateResult.CREATED;
            }

            if (!modals
                .get(0)
                .findElements(By.cssSelector("input:invalid,select:invalid"))
                .isEmpty()) return CreateResult.REJECTED;
            List<WebElement> errors = modals.get(0).findElements(CREATE_MODAL_ERROR);
            return errors.stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(message -> !message.isBlank())
                .findFirst()
                .map(message -> CreateResult.REJECTED)
                .orElse(null);
          });
    } catch (TimeoutException exception) {
      throw new TimeoutException(
          "User creation did not complete within "
              + CREATE_RESULT_TIMEOUT.toSeconds()
              + " seconds. Current form state: "
              + createFormDiagnostics(),
          exception);
    }
  }

  private String createFormDiagnostics() {
    Object state =
        ((JavascriptExecutor) driver)
            .executeScript(
                """
                const modal = document.querySelector("dialog[open]");
                const visible = element => element !== null
                  && element.getClientRects().length > 0
                  && getComputedStyle(element).visibility !== 'hidden';
                const error = modal?.querySelector('.admin-feedback.error');
                const submit = modal?.querySelector('.primary-button');
                return JSON.stringify({
                  modal: modal === null ? 'absent' : visible(modal) ? 'visible' : 'hidden',
                  error: visible(error) ? error.innerText.trim() : null,
                  submitText: submit?.innerText.trim() ?? null,
                  submitDisabled: submit?.disabled ?? null
                });
                """);
    return String.valueOf(state);
  }

  public boolean isRequiredFieldInvalid(String label) {
    WebElement field = UiSupport.dialog(driver, wait).findElement(UiSupport.label(label));
    return Boolean.TRUE.equals(
        ((JavascriptExecutor) driver)
            .executeScript(
                "return arguments[0].required && arguments[0].validity.valueMissing;", field));
  }

  public String getCreateError() {
    return wait.until(
        d -> {
          var errors = d.findElements(CREATE_ERROR);
          for (var e : errors) if (e.isDisplayed() && !e.getText().isBlank()) return e.getText();
          var invalid =
              d.findElements(
                  By.cssSelector("dialog[open] input:invalid,dialog[open] select:invalid"));
          return invalid.isEmpty() ? null : invalid.get(0).getDomProperty("validationMessage");
        });
  }

  public boolean isCreateModalOpen() {
    return !driver.findElements(CREATE_MODAL).isEmpty();
  }

  public void closeCreateModalIfOpen() {
    List<WebElement> modals = driver.findElements(CREATE_MODAL);
    if (modals.isEmpty() || !modals.get(0).isDisplayed()) {
      return;
    }

    wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(
                    "dialog.admin-modal[aria-label='Nouvel utilisateur']"
                        + " button[aria-label='Fermer']")))
        .click();
    wait.until(ExpectedConditions.invisibilityOfElementLocated(CREATE_MODAL));
  }

  public void openEditUser(String email) {
    wait.until(currentDriver -> findRowByEmail(email).orElse(null));
    openRowActions(email);
    findRowByEmail(email)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "The user row disappeared before its actions were opened."));
    clickRowAction(email, "Modifier");
    wait.until(ExpectedConditions.visibilityOfElementLocated(EDIT_MODAL));
  }

  public void changeFirstName(String firstName) {
    WebElement input =
        wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//dialog[@open]//label[normalize-space(text())='Prénom']/input")));
    input.clear();
    input.sendKeys(firstName);
  }

  public void saveUserChanges() {
    wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector(
                    "dialog.admin-modal[aria-label='Modifier l’utilisateur'] .primary-button")))
        .click();
    wait.until(ExpectedConditions.invisibilityOfElementLocated(EDIT_MODAL));
  }

  public void deleteUser(String email) {
    requireTestUserEmail(email);
    wait.until(currentDriver -> findRowByEmail(email).orElse(null));
    System.out.println("User cleanup: found test user in table.");
    openRowActions(email);
    clickRowAction(email, "Supprimer");
    wait.until(ExpectedConditions.alertIsPresent()).accept();

    String outcome;
    try {
      outcome =
          wait.until(
              currentDriver -> {
                String error = visibleText(currentDriver, USER_DELETE_ERROR);
                if (error != null) {
                  return "error:" + error;
                }
                String status = visibleText(currentDriver, USER_DELETE_STATUS);
                return status == null ? null : "status:" + status;
              });
    } catch (TimeoutException exception) {
      throw new TimeoutException(
          "No HTTP deletion result or UI error appeared within "
              + waitDurationSeconds()
              + " seconds. Test-user row still present: "
              + findRowByEmail(email).isPresent()
              + "; URL: "
              + driver.getCurrentUrl(),
          exception);
    }

    if (outcome.startsWith("error:")) {
      throw new IllegalStateException(
          "The application rejected deletion of the tracked test user: "
              + outcome.substring("error:".length()));
    }
    if (!outcome.contains("HTTP 204")) {
      throw new IllegalStateException(
          "Expected the user DELETE endpoint to return HTTP 204, but the UI reported: "
              + outcome.substring("status:".length()));
    }
    System.out.println("User cleanup: API confirmed deletion with HTTP 204.");

    try {
      wait.until(currentDriver -> findRowByEmail(email).isEmpty());
    } catch (TimeoutException exception) {
      throw new TimeoutException(
          "The DELETE endpoint returned HTTP 204, but the test-user row "
              + "did not disappear from the React table.",
          exception);
    }
    System.out.println("User cleanup: row disappeared from the React table.");

    driver.navigate().refresh();
    waitForPageContent(new WebDriverWait(driver, PAGE_LOAD_TIMEOUT));
    searchFor(email);
    if (findRowByEmail(email).isPresent()) {
      throw new IllegalStateException(
          "The tracked test user reappeared after reload; backend deletion was not persisted.");
    }
    System.out.println("User cleanup: user remains absent after reloading the list.");
  }

  public void deleteUserIfPresent(String email) {
    requireTestUserEmail(email);
    closeCreateModalIfOpen();

    refresh();
    searchFor(email);
    Optional<WebElement> row = findRowByEmail(email);
    if (row.isPresent()) {
      System.out.println("User cleanup: tracked test user is present; deleting it.");
      deleteUser(email);
    } else {
      System.out.println("User cleanup: tracked test user is already absent.");
    }
  }

  private void clickRowAction(String email, String action) {
    By actionButtonLocator =
        switch (action.toLowerCase()) {
          case "modifier" -> By.cssSelector("[data-testid='edit-user-action']");
          case "supprimer" -> By.cssSelector("[data-testid='delete-user-action']");
          default -> throw new IllegalArgumentException("Unsupported user action: " + action);
        };
    WebElement row =
        wait.until(
            currentDriver ->
                findRowByEmail(email)
                    .filter(currentRow -> !currentRow.findElements(actionButtonLocator).isEmpty())
                    .orElse(null));
    WebElement actionButton = row.findElement(actionButtonLocator);
    wait.until(ExpectedConditions.visibilityOf(actionButton));
    wait.until(ExpectedConditions.elementToBeClickable(actionButton)).click();
    System.out.println("User action clicked: " + action + ".");
  }

  private void openRowActions(String email) {
    By moreButtonLocator = By.cssSelector("[data-testid='user-row-actions']");
    WebElement moreButton =
        wait.until(
            currentDriver ->
                findRowByEmail(email)
                    .map(
                        row ->
                            row.findElements(moreButtonLocator).stream().findFirst().orElse(null))
                    .orElse(null));

    ((JavascriptExecutor) driver)
        .executeScript(
            "arguments[0].scrollIntoView({block: 'center', inline: 'center'});", moreButton);

    moreButton =
        wait.until(
            currentDriver ->
                findRowByEmail(email)
                    .map(
                        row ->
                            row.findElements(moreButtonLocator).stream()
                                .filter(WebElement::isDisplayed)
                                .findFirst()
                                .orElse(null))
                    .orElse(null));
    new Actions(driver).moveToElement(moreButton).perform();
    wait.until(ExpectedConditions.elementToBeClickable(moreButton)).click();
    wait.until(
        currentDriver ->
            findRowByEmail(email)
                .map(
                    row ->
                        row.findElements(By.cssSelector(".action-dropdown")).stream()
                            .anyMatch(WebElement::isDisplayed))
                .orElse(false));
    System.out.println("User cleanup: row action menu opened.");
  }

  private Optional<WebElement> findRowByEmail(String email) {
    return driver.findElements(USER_ROWS).stream()
        .filter(
            row ->
                row.findElements(By.cssSelector("[data-testid='user-email']")).stream()
                    .anyMatch(cell -> email.equals(cell.getText().trim())))
        .findFirst();
  }

  private void waitForPageContent(WebDriverWait pageWait) {
    String pageState;
    try {
      pageState =
          pageWait.until(
              currentDriver -> {
                String error = visibleText(currentDriver, USER_LIST_ERROR);
                if (error != null) {
                  return "error:" + error;
                }

                boolean readyMarkerVisible =
                    currentDriver.findElements(USERS_PAGE_READY).stream()
                        .anyMatch(WebElement::isDisplayed);
                boolean pageVisible =
                    currentDriver.findElements(PAGE).stream().anyMatch(WebElement::isDisplayed);
                boolean tableVisible =
                    currentDriver.findElements(TABLE).stream().anyMatch(WebElement::isDisplayed);
                return readyMarkerVisible && pageVisible && tableVisible ? "ready" : null;
              });
    } catch (TimeoutException exception) {
      throw new TimeoutException(
          "The React users page did not leave its loading state within "
              + PAGE_LOAD_TIMEOUT.toSeconds()
              + " seconds. "
              + pageLoadDiagnostics(),
          exception);
    }

    if (pageState.startsWith("error:")) {
      throw new IllegalStateException(
          "The users list could not be loaded: "
              + pageState.substring("error:".length())
              + ". "
              + pageLoadDiagnostics());
    }
  }

  private String pageLoadDiagnostics() {
    String bodyText = driver.findElement(By.tagName("body")).getText().trim();
    if (bodyText.length() > 500) {
      bodyText = bodyText.substring(0, 500);
    }
    Object requestDiagnostics =
        ((JavascriptExecutor) driver)
            .executeScript(
                """
const entries = performance.getEntriesByType('resource')
  .filter(entry => entry.name.includes('/api/users'))
  .slice(-8)
  .map(entry => ({
    endpoint: new URL(entry.name).pathname,
    durationMs: Math.round(entry.duration),
    responseStatus: entry.responseStatus ?? null
  }));
return JSON.stringify({
  readyState: document.readyState,
  usersState: document.querySelector('[data-users-state]')?.getAttribute('data-users-state') ?? 'unmounted',
  usersRequestResources: entries
});
""");

    return "URL: "
        + driver.getCurrentUrl()
        + "; title: "
        + driver.getTitle()
        + "; loading visible: "
        + driver.findElements(USERS_LOADING).stream().anyMatch(WebElement::isDisplayed)
        + "; ready marker visible: "
        + driver.findElements(USERS_PAGE_READY).stream().anyMatch(WebElement::isDisplayed)
        + "; table visible: "
        + driver.findElements(TABLE).stream().anyMatch(WebElement::isDisplayed)
        + "; visible text: "
        + bodyText
        + "; browser state and users API timing: "
        + requestDiagnostics;
  }

  private String visibleText(org.openqa.selenium.WebDriver currentDriver, By locator) {
    return currentDriver.findElements(locator).stream()
        .filter(WebElement::isDisplayed)
        .map(WebElement::getText)
        .map(String::trim)
        .filter(text -> !text.isBlank())
        .findFirst()
        .orElse(null);
  }

  private long waitDurationSeconds() {
    return timeout.toSeconds();
  }

  private void requireTestUserEmail(String email) {
    if (email == null || !TEST_USER_EMAIL.matcher(email).matches()) {
      throw new IllegalArgumentException(
          "Deletion is restricted to users created with the QA test email format.");
    }
  }

  private boolean hasSelectableOption(WebElement selectElement) {
    return new Select(selectElement)
        .getOptions().stream().anyMatch(option -> !option.getAttribute("value").isBlank());
  }

  private void selectFirstAvailableOption(WebElement selectElement) {
    List<WebElement> options = new Select(selectElement).getOptions();
    for (int index = 0; index < options.size(); index++) {
      if (!options.get(index).getAttribute("value").isBlank()) {
        new Select(selectElement).selectByIndex(index);
        return;
      }
    }
    throw new IllegalStateException("The user form has no available department or role option.");
  }

  private void selectNonAdminRole(WebElement selectElement) {
    Select roleSelect = new Select(selectElement);
    for (WebElement option : roleSelect.getOptions()) {
      String value = option.getAttribute("value");
      String roleName = option.getText().trim();
      String normalizedRoleName = roleName.toUpperCase(Locale.ROOT).replaceAll("[\\s-]+", "_");

      if (value != null
          && !value.isBlank()
          && !PROTECTED_ADMIN_ROLES.contains(normalizedRoleName)) {
        roleSelect.selectByValue(value);
        System.out.println("User creation: selected non-administrator role '" + roleName + "'.");
        return;
      }
    }

    throw new IllegalStateException(
        "The user form has no selectable non-administrator role; "
            + "refusing to create a protected QA test account.");
  }

  public enum CreateResult {
    CREATED,
    REJECTED
  }
}
