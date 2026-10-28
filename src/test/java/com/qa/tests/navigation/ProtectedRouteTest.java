package com.qa.tests.navigation;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
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

@Feature("Navigation et accès")
@Story("Route protégée")
class ProtectedRouteTest extends BaseTest {

    @Test
    @Tag("smoke")
    @Tag("security")
    @Tag("regression")
    void unauthenticatedUserIsRedirectedFromKanbanToLogin() {
        String baseUrl = ConfigManager.baseUri().toString().replaceAll("/$", "");
        driver.get(baseUrl + "/");
        driver.manage().deleteAllCookies();
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");

        driver.get(baseUrl + "/kanban");

        String path = new WebDriverWait(driver, waitDuration).until(
                currentDriver -> {
                    String currentPath = URI.create(currentDriver.getCurrentUrl()).getPath();
                    return "/".equals(currentPath) ? currentPath : null;
                });

        assertThat(path).isEqualTo("/");
        assertThat(new LoginPage(driver, waitDuration, baseUrl).isLoginFormVisible()).isTrue();
    }
}
