package com.qa.tests.authentication;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
import com.qa.pages.ResetPasswordPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Authentification")
@Story("Réinitialisation du mot de passe")
class ResetPasswordValidationTest extends BaseTest {

    private ResetPasswordPage createPage() {
        return new ResetPasswordPage(
                driver,
                waitDuration,
                ConfigManager.baseUri().toString().replaceAll("/$", ""));
    }

    @Test
    @Tag("regression")
    @Description("L'accès sans jeton affiche un message et ne présente pas le formulaire.")
    void missingTokenIsReportedWithoutShowingForm() {
        ResetPasswordPage page = createPage();

        page.openWithoutToken();

        assertThat(page.waitForError())
                .isEqualTo("Le lien de réinitialisation est invalide ou incomplet.");
        assertThat(page.isFormVisible()).isFalse();
    }

    @Test
    @Tag("regression")
    @Description("Les mots de passe saisis doivent être identiques avant l'appel au service.")
    void mismatchedPasswordsAreRejectedBeforeRequest() {
        ResetPasswordPage page = createPage();

        page.openWithToken("qa-ui-validation-token");
        page.submitMismatchedPasswords("ValidPassword123", "DifferentPassword123");

        assertThat(page.waitForError()).isEqualTo("Les mots de passe ne correspondent pas.");
        assertThat(page.isFormVisible()).isTrue();
    }
}
