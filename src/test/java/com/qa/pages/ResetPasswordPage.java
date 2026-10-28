package com.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ResetPasswordPage {

    private static final By PAGE = By.cssSelector(".reset-page");
    private static final By FORM = By.cssSelector(".reset-page form");
    private static final By INPUTS = By.cssSelector(".reset-page form input.input-field");
    private static final By SUBMIT_BUTTON =
            By.cssSelector(".reset-page form button[type='submit']");
    private static final By ERROR = By.cssSelector(".reset-page [role='alert']");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    public ResetPasswordPage(WebDriver driver, Duration timeout, String baseUrl) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
        this.baseUrl = baseUrl;
    }

    public void openWithoutToken() {
        driver.get(baseUrl + "/reset-password");
        wait.until(ExpectedConditions.visibilityOfElementLocated(PAGE));
    }

    public void openWithToken(String token) {
        driver.get(baseUrl + "/reset-password?token=" + token);
        wait.until(ExpectedConditions.visibilityOfElementLocated(FORM));
    }

    public void submitEmptyTokenForm() {
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
    }

    public void submitMismatchedPasswords(String password, String confirmation) {
        List<WebElement> inputs = wait.until(currentDriver -> {
            List<WebElement> elements = currentDriver.findElements(INPUTS);
            return elements.size() == 2 ? elements : null;
        });
        inputs.get(0).sendKeys(password);
        inputs.get(1).sendKeys(confirmation);
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();
    }

    public String waitForError() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR)).getText();
    }

    public boolean isFormVisible() {
        return !driver.findElements(FORM).isEmpty();
    }
}
