package com.ecommerce.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;

/**
 * TestNG listener that builds an ExtentReports HTML report under target/api-reports/.
 * Attach with @Listeners({ApiTestListener.class}) on BaseApiTest.
 */
public class ApiTestListener implements ITestListener {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        new File("target/api-reports").mkdirs();

        ExtentSparkReporter spark = new ExtentSparkReporter("target/api-reports/index.html");
        spark.config().setDocumentTitle("API Test Report");
        spark.config().setReportName("REST-Assured API Tests – JSONPlaceholder");
        spark.config().setTheme(Theme.DARK);
        spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

        extent = new ExtentReports();
        extent.setSystemInfo("Environment", "jsonplaceholder.typicode.com");
        extent.setSystemInfo("Author", "Automated Testing Lab");
        extent.attachReporter(spark);
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        String desc = result.getMethod().getDescription();
        String testName = (desc != null && !desc.isEmpty())
                ? desc : result.getMethod().getMethodName();

        Object[] params = result.getParameters();
        if (params != null && params.length > 0) {
            StringBuilder sb = new StringBuilder(testName).append(" [");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(params[i]);
            }
            testName = sb.append("]").toString();
        }

        ExtentTest test = extent.createTest(testName);
        test.assignCategory(result.getTestClass().getName().replaceAll(".*\\.", ""));
        currentTest.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        currentTest.get().log(Status.PASS, "Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        currentTest.get().log(Status.FAIL, result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        currentTest.get().log(Status.SKIP, "Test skipped");
    }
}


/**
 * TestNG listener that:
 *  - Builds an ExtentReports HTML report under target/api-reports/
 *  - Persists each test result to the SQLite database via DatabaseUtil
 *
 * Attach with @Listeners({ApiTestListener.class}) on BaseApiTest.
 */
