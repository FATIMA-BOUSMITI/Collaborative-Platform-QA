package com.qa.tests.authentication;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
import com.qa.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Authentification")
@Story("Connexion")
class ValidLoginTest extends BaseTest {

    @Test
    @Tag("smoke")
    @Tag("e2e")
    @Description("Un compte de test valide arrive sur la route correspondant à son rôle.")
    void validCredentialsRedirectToConfiguredRoleLandingPage() {
        LoginPage loginPage = new LoginPage(
                driver,
                waitDuration,
                ConfigManager.baseUri().toString().replaceAll("/$", ""));
        ConfigManager.Credentials credentials = ConfigManager.validCredentials();
        String expectedPath = ConfigManager.validLandingPath();

        loginPage.open();
        loginPage.signIn(credentials.email(), credentials.password());

        assertThat(loginPage.waitForLandingPath()).isEqualTo(expectedPath);
    }
}
