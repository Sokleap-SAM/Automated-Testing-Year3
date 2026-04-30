package com.ecommerce.listeners;

import com.ecommerce.driver.DriverManager;
import com.ecommerce.utils.ScreenshotUtil;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.openqa.selenium.WebDriver;

public class TestListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        WebDriver driver = DriverManager.getDriver();
        String testName = result.getMethod().getMethodName() + "_SUCCESS";
        String path = ScreenshotUtil.takeScreenshot(driver, testName);
        if (path != null) {
            Reporter.log("Saved screenshot: " + path, true);
            Reporter.log("<a href='" + path + "'>Screenshot</a>", true);
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = DriverManager.getDriver();
        String testName = result.getMethod().getMethodName();
        String path = ScreenshotUtil.takeScreenshot(driver, testName);
        if (path != null) {
            Reporter.log("Saved screenshot: " + path, true);
            Reporter.log("<a href='" + path + "'>Screenshot</a>", true);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    @Override
    public void onStart(ITestContext context) {
    }

    @Override
    public void onFinish(ITestContext context) {
    }
}
