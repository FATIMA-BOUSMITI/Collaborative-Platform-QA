package com.qa.tests.authentication;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
import com.qa.pages.ForgotPasswordPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Authentification")
@Story("Mot de passe oublié")
class ForgotPasswordValidationTest extends BaseTest {

    @Test
    @Tag("regression")
    void emptyEmailShowsRequiredFieldMessage() {
        ForgotPasswordPage page = new ForgotPasswordPage(
                driver,
                waitDuration,
                ConfigManager.baseUri().toString().replaceAll("/$", ""));

        page.open();
        page.submitEmptyEmail();

        assertThat(page.waitForEmptyEmailError()).isEqualTo("Ce champ est obligatoire");
    }
}
