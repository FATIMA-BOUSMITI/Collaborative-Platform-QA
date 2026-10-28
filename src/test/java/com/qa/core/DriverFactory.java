package com.qa.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create() {
        return switch (ConfigManager.browser()) {
            case CHROME -> createChrome();
            case FIREFOX -> createFirefox();
        };
    }

    private static WebDriver createChrome() {
        ChromeOptions options = new ChromeOptions();
        if (ConfigManager.headless()) {
            options.addArguments("--headless=new");
        }
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefox() {
        FirefoxOptions options = new FirefoxOptions();
        if (ConfigManager.headless()) {
            options.addArguments("-headless");
        }
        return new FirefoxDriver(options);
    }
}
