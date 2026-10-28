package com.qa.tests.navigation;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
import com.qa.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Navigation et accès")
@Story("Accès interdit aux routes administrateur")
class AdminRouteAccessTest extends BaseTest {

    @Test
    @Tag("security")
    @Tag("e2e")
    @Description("Un utilisateur authentifié sans rôle administrateur ne peut pas ouvrir la gestion des rôles.")
    void nonAdministratorIsRedirectedFromRolesPage() {
        String baseUrl = ConfigManager.baseUri().toString().replaceAll("/$", "");
        LoginPage loginPage = new LoginPage(driver, waitDuration, baseUrl);
        ConfigManager.Credentials credentials = ConfigManager.nonAdminCredentials();

        loginPage.open();
        loginPage.signIn(credentials.email(), credentials.password());
        loginPage.waitForLandingPath();
        driver.get(baseUrl + "/roles");

        String path = new WebDriverWait(driver, waitDuration).until(currentDriver -> {
            String currentPath = URI.create(currentDriver.getCurrentUrl()).getPath();
            return "/".equals(currentPath) ? currentPath : null;
        });

        assertThat(path).isEqualTo("/");
        assertThat(loginPage.isLoginFormVisible()).isTrue();
    }
}
