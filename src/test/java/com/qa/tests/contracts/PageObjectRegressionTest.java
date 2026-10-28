package com.qa.tests.contracts;

import static org.junit.jupiter.api.Assertions.*;

import com.qa.core.BaseTest;
import com.qa.pages.LoginPage;
import com.qa.pages.UsersPage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Native-browser regressions on self-contained HTML. These do not validate ERP APIs. */
@Tag("page-object-contract")
class PageObjectRegressionTest extends BaseTest {
  private UsersPage users() {
    return new UsersPage(driver, waitDuration, "http://fixture.invalid");
  }

  private void html(String markup) {
    driver.get(
        "data:text/html;charset=utf-8;base64,"
            + Base64.getEncoder().encodeToString(markup.getBytes(StandardCharsets.UTF_8)));
  }

  private void userForm() {
    html(
        """
<!doctype html><meta charset="utf-8"><style>th{text-transform:uppercase}</style>
<div class="users-page"><div class="users-table"><table><thead><tr><th>Utilisateur</th><th>Email</th><th>Département</th><th>Rôle</th><th>Statut</th><th>Actions</th></tr></thead><tbody></tbody></table></div>
<button class="add-user-button" onclick="document.querySelector('dialog').showModal()">Ajouter</button></div>
<dialog class="admin-modal" aria-label="Nouvel utilisateur"><header><button type="button" aria-label="Fermer" onclick="this.closest('dialog').close()">×</button></header>
<form onsubmit="event.preventDefault()"><label>Prénom<input type="text" required></label><label>Nom<input type="text" required></label><label>Email<input type="email" required></label>
<label>Département<select required><option value="">Sélectionner</option><option value="dept">QA Department</option></select></label>
<label>Rôle<select required><option value="">Sélectionner</option><option value="employee">EMPLOYEE</option></select></label>
<div class="admin-actions"><button class="primary-button">Enregistrer</button></div></form></dialog>
""");
  }

  @Test
  void headerMeaningDoesNotDependOnCssCapitalization() {
    userForm();
    assertEquals(
        java.util.List.of("Utilisateur", "Email", "Département", "Rôle", "Statut", "Actions"),
        users().getColumnHeaders());
  }

  @Test
  void emptyRequiredFieldIsRejectedByNativeDialogForm() {
    userForm();
    UsersPage page = users();
    page.openCreateModal();
    page.fillCreateUserForm("", "Automation", "qa@example.invalid");
    page.submitCreateForm();
    assertTrue(page.isRequiredFieldInvalid("Prénom"));
    assertEquals(UsersPage.CreateResult.REJECTED, page.waitForCreateResult());
    assertFalse(page.getCreateError().isBlank());
    assertTrue(page.isCreateModalOpen());
  }

  @Test
  void malformedEmailIsRejectedWithoutBackendRequest() {
    userForm();
    UsersPage page = users();
    page.openCreateModal();
    page.fillCreateUserForm("QA", "Automation", "not-an-email");
    page.submitCreateForm();
    assertEquals(UsersPage.CreateResult.REJECTED, page.waitForCreateResult());
    assertFalse(page.getCreateError().isBlank());
    assertTrue(page.isCreateModalOpen());
  }

  @Test
  void loginErrorIsReportedWithoutRetryingCredentials() {
    html(
        """
<!doctype html><meta charset="utf-8"><script>window.loginAttempts=0</script>
<form class="login-form" onsubmit="event.preventDefault();window.loginAttempts++;document.querySelector('.error-box').hidden=false"><input type="email"><input type="password"><button type="submit">Connexion</button></form>
<p class="error-box" hidden>Authentication unavailable: HTTP 401</p>
""");
    LoginPage login = new LoginPage(driver, waitDuration, "http://fixture.invalid");
    login.signIn("qa@example.invalid", "fixture-password");
    IllegalStateException failure =
        assertThrows(IllegalStateException.class, login::waitForLandingPath);
    assertTrue(failure.getMessage().contains("401"));
    assertEquals(
        1L,
        ((Number)
                ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("return window.loginAttempts"))
            .longValue());
  }
}
