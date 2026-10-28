package com.qa.tests.authentication;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
import com.qa.pages.AuthenticatedLayout;
import com.qa.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Authentification")
@Story("Déconnexion")
class LogoutTest extends BaseTest {

    @Test
    @Tag("smoke")
    @Tag("security")
    @Tag("e2e")
    @Description("La déconnexion retourne à l'écran de connexion et efface les tokens locaux.")
    void logoutReturnsToLoginAndClearsStoredTokens() {
        String baseUrl = ConfigManager.baseUri().toString().replaceAll("/$", "");
        LoginPage loginPage = new LoginPage(driver, waitDuration, baseUrl);
        ConfigManager.Credentials credentials = ConfigManager.validCredentials();
        String expectedPath = ConfigManager.validLandingPath();

        loginPage.open();
        loginPage.signIn(credentials.email(), credentials.password());
        assertThat(loginPage.waitForLandingPath()).isEqualTo(expectedPath);

        new AuthenticatedLayout(driver, waitDuration).logOut();

        String path = new WebDriverWait(driver, waitDuration).until(
                currentDriver -> "/".equals(URI.create(currentDriver.getCurrentUrl()).getPath())
                        ? "/"
                        : null);
        assertThat(path).isEqualTo("/");
        assertThat(((JavascriptExecutor) driver).executeScript(
                "return window.localStorage.getItem('accessToken');")).isNull();
        assertThat(((JavascriptExecutor) driver).executeScript(
                "return window.localStorage.getItem('refreshToken');")).isNull();
    }
}
