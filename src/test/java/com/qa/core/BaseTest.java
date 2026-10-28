package com.qa.core;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Dimension;

import java.io.ByteArrayInputStream;
import java.time.Duration;

@ExtendWith(FailureScreenshotExtension.class)
public abstract class BaseTest {

    protected WebDriver driver;
    protected Duration waitDuration;

    @BeforeEach
    void setUpBrowser() {
        waitDuration = ConfigManager.explicitWait();
        driver = DriverFactory.create();
        driver.manage().window().setSize(new Dimension(1440, 1000));
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        Allure.parameter("browser", ConfigManager.browser().name().toLowerCase());
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    void attachFailureScreenshot() {
        if (driver instanceof TakesScreenshot screenshotDriver) {
            byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(
                    "Failure screenshot",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    "png");
        }
    }
}
