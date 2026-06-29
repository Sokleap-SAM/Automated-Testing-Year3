package automatedtesting.lab08.lab08.support;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;

import com.microsoft.playwright.Page;

import io.qameta.allure.Allure;

/**
 * Captures a Playwright screenshot to the project's {@code images/} folder
 * <em>and</em> attaches it to the Allure report, so the UI result is saved both
 * as a standalone file and inside the report.
 */
public final class Screenshots {

    private Screenshots() {
    }

    /**
     * @param fileName e.g. {@code "dashboard-quota.png"} — written to {@code images/<fileName>}
     * @param title    the Allure attachment title
     */
    public static void capture(Page page, String fileName, String title) {
        byte[] png = page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("images", fileName))
                .setFullPage(true));
        Allure.addAttachment(title, "image/png", new ByteArrayInputStream(png), "png");
    }
}
