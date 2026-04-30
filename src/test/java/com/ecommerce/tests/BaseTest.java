package com.ecommerce.tests;

import com.ecommerce.driver.DriverFactory;
import com.ecommerce.driver.DriverManager;
import com.ecommerce.listeners.TestListener;
import org.openqa.selenium.WebDriver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners({TestListener.class})
public class BaseTest {
    @BeforeMethod
    public void setUp() {
        try {
            Files.createDirectories(Path.of("target", "screenshots"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        WebDriver driver = DriverFactory.createChromeDriver();
        DriverManager.setDriver(driver);
    }

    @AfterMethod
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
