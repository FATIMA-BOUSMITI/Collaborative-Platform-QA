package com.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ForgotPasswordPage {

    private static final By PAGE = By.cssSelector(".forgot-page");
    private static final By EMAIL_INPUT = By.cssSelector(".forgot-page input.input-field");
    private static final By SUBMIT_BUTTON =
            By.cssSelector(".forgot-page form button[type='submit']");
    private static final By EMPTY_EMAIL_ERROR =
            By.cssSelector(".forgot-page .input-group .error-message");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    public ForgotPasswordPage(WebDriver driver, Duration timeout, String baseUrl) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
        this.baseUrl = baseUrl;
    }

    public void open() {
        driver.get(baseUrl + "/forgot-password");
        wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE));
    }

    public void submitEmptyEmail() {
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
    }

    public String waitForEmptyEmailError() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(EMPTY_EMAIL_ERROR))
                .getText();
    }

    public boolean isEmailInvalid() {
        WebElement email = wait.until(
                ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT));
        return "true".equals(email.getAttribute("aria-invalid"));
    }
}
