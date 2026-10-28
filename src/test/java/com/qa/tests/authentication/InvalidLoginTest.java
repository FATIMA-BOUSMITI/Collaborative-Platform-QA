package com.qa.tests.authentication;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
import com.qa.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Authentification")
@Story("Connexion")
class InvalidLoginTest extends BaseTest {

    @Test
    @Tag("regression")
    @Tag("security")
    @Tag("e2e")
    @Description("Des identifiants de test explicitement invalides ne créent pas de session.")
    void invalidCredentialsDisplayAnErrorAndDoNotCreateAnAccessToken() {
        LoginPage loginPage = new LoginPage(
                driver,
                waitDuration,
                ConfigManager.baseUri().toString().replaceAll("/$", ""));
        ConfigManager.Credentials credentials = ConfigManager.invalidCredentials();

        loginPage.open();
        loginPage.signIn(credentials.email(), credentials.password());

        assertThat(loginPage.waitForLoginError()).isNotBlank();
        assertThat(driver.getCurrentUrl()).isEqualTo(
                ConfigManager.baseUri().toString().replaceAll("/$", "") + "/");
        assertThat(((JavascriptExecutor) driver).executeScript(
                "return window.localStorage.getItem('accessToken');")).isNull();
    }
}
