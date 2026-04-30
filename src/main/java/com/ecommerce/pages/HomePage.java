package com.ecommerce.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage {
    private final WebDriver driver;
    private final By header = By.cssSelector("div.example h2");

    public HomePage(WebDriver driver) {
        this.driver = driver;
    }

    public String getHeader() {
        return driver.findElement(header).getText();
    }
}
