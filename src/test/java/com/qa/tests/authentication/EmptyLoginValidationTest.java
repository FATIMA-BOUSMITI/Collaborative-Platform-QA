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
@Story("Validation du formulaire")
class EmptyLoginValidationTest extends BaseTest {

    @Test
    @Tag("regression")
    @Tag("e2e")
    @Description("Le formulaire vide affiche son erreur locale sans solliciter l'API.")
    void emptyCredentialsDisplayRequiredFieldsError() {
        LoginPage loginPage = new LoginPage(
                driver,
                waitDuration,
                ConfigManager.baseUri().toString().replaceAll("/$", ""));

        loginPage.open();
        loginPage.submitEmptyForm();

        assertThat(loginPage.waitForLoginError())
                .contains("Veuillez renseigner tous les champs.");
        assertThat(driver.getCurrentUrl()).isEqualTo(
                ConfigManager.baseUri().toString().replaceAll("/$", "") + "/");
    }
}
