
package com.qa.tests.roles;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
import com.qa.pages.LoginPage;
import com.qa.pages.RolesPage;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Gestion des rôles")
@Story("Liste des rôles attribués")
class RoleListTest extends BaseTest {

    private RolesPage rolesPage;

    @BeforeEach
    void signInAsAdministrator() {

        String baseUrl = ConfigManager.baseUri()
                .toString()
                .replaceAll("/$", "");

        LoginPage loginPage = new LoginPage(
                driver, waitDuration, baseUrl
        );

        ConfigManager.Credentials credentials =
                ConfigManager.adminCredentials();

        loginPage.open();

        loginPage.signIn(
                credentials.email(),
                credentials.password()
        );

        // Redirection observée pour ce compte
        assertThat(loginPage.waitForLandingPath())
                .isEqualTo("/dashboard");

        rolesPage = new RolesPage(
                driver, waitDuration, baseUrl
        );

        rolesPage.open();
    }

    @Test
    @Tag("regression")
    @Description("Vérifier les colonnes du tableau des rôles")
    void roleTableDisplaysExpectedColumns() {

        assertThat(rolesPage.getColumnHeaders())
                .containsExactly(
                        "Collaborateur",
                        "Email",
                        "Rôle"
                );
    }

    @Test
    @Tag("regression")
    @Description("Vérifier la recherche par email")
    void roleSearchFiltersUsersByEmail() {

        String email =
                ConfigManager.adminCredentials().email();

        rolesPage.searchFor(email);

        // Attendre jusqu'à 30 secondes que l'email apparaisse
        WebDriverWait wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(30)
        );

        wait.until(webDriver ->
                rolesPage.getVisibleEmails()
                        .stream()
                        .anyMatch(value ->
                                value.equalsIgnoreCase(email)
                        )
        );

        // Vérification du résultat final
        assertThat(rolesPage.getVisibleEmails())
                .containsExactly(email);
    }
}
