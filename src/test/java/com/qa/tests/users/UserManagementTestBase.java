package com.qa.tests.users;

import static org.assertj.core.api.Assertions.assertThat;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
import com.qa.pages.LoginPage;
import com.qa.pages.UsersPage;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

abstract class UserManagementTestBase extends BaseTest {

  protected UsersPage usersPage;
  private String testUserEmail;

  @BeforeEach
  void signInAsQaAdministrator() {
    usersPage = null;
    testUserEmail = null;

    // Autoriser les tests CRUD uniquement
    // sur une base QA isolée.
    ConfigManager.assumeIsolatedQaData();

    String baseUrl = ConfigManager.baseUri().toString().replaceAll("/$", "");

    LoginPage loginPage = new LoginPage(driver, waitDuration, baseUrl);

    ConfigManager.Credentials credentials = ConfigManager.adminCredentials();

    // Connexion administrateur
    loginPage.open();
    loginPage.signIn(credentials.email(), credentials.password());

    assertThat(loginPage.waitForLandingPath()).isEqualTo("/dashboard");

    // Ouvrir la page de gestion des utilisateurs
    usersPage = new UsersPage(driver, waitDuration, baseUrl);

    usersPage.open();
  }

  @AfterEach
  void removeTestUser() {
    // Nettoyage des données créées par le test
    if (usersPage == null || testUserEmail == null) {
      return;
    }
    usersPage.deleteUserIfPresent(testUserEmail);
  }

  protected String createTestUser() {

    // 1. Générer un email unique
    String email = newTestEmail();

    // 2. Ouvrir le formulaire
    usersPage.openCreateModal();

    // 3. Remplir les champs obligatoires
    usersPage.fillCreateUserForm("QA Selenium", "Automation", email);

    trackForCleanup(email);

    // 4. Cliquer sur le bouton "Créer"
    usersPage.submitCreateForm();

    // 5. Vérifier le résultat de création
    assertThat(usersPage.waitForCreateResult())
        .as("The backend should accept a unique test user")
        .isEqualTo(UsersPage.CreateResult.CREATED);

    trackForCleanup(email);

    // 6. Vérifier que l'utilisateur apparaît
    assertThat(usersPage.waitForUser(email))
        .as("The created user should appear in the users table")
        .isTrue();

    return email;
  }

  protected String newTestEmail() {
    return "qa.selenium." + UUID.randomUUID().toString().replace("-", "") + "@example.invalid";
  }

  private void trackForCleanup(String email) {
    if (email == null || !email.matches("qa\\.selenium\\.[0-9a-f]{32}@example\\.invalid")) {
      throw new IllegalArgumentException(
          "Only generated QA test-user email addresses can be tracked for cleanup.");
    }
    testUserEmail = email;
  }
}
