package com.ecommerce.tests;

import com.ecommerce.driver.DriverManager;
import com.ecommerce.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @DataProvider(name = "credentials")
    public Object[][] credentials() {
        return new Object[][]{
                {"tomsmith", "SuperSecretPassword!", true},
                {"wronguser", "wrongpass", false}
        };
    }

    @Test(dataProvider = "credentials")
    public void loginTest(String user, String pass, boolean shouldSucceed) {
        WebDriver driver = DriverManager.getDriver();
        LoginPage login = new LoginPage(driver);
        login.open();
        login.login(user, pass);
        String flash = login.getFlashText();
        if (shouldSucceed) {
            Assert.assertTrue(flash.toLowerCase().contains("secure area"), "Expected successful login message");
        } else {
            Assert.assertTrue(flash.toLowerCase().contains("invalid"), "Expected failure message");
        }
    }
}
