package com.qa.tests.admin;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
import com.qa.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AdminFeatureTestBase extends BaseTest {

    protected String baseUrl;

    protected boolean requiresIsolatedQaData() {
        return false;
    }

    @BeforeEach
    protected void signInAsAdministrator() {
        if (requiresIsolatedQaData()) {
            ConfigManager.assumeIsolatedQaData();
        }
        baseUrl = ConfigManager.baseUri().toString().replaceAll("/$", "");
        LoginPage loginPage = new LoginPage(driver, waitDuration, baseUrl);
        ConfigManager.Credentials credentials = ConfigManager.adminCredentials();

        loginPage.open();
        loginPage.signIn(credentials.email(), credentials.password());

        assertThat(loginPage.waitForLandingPath()).isEqualTo("/dashboard");
    }
}
