package automatedtesting.lab08.lab08;

import static automatedtesting.lab08.lab08.support.Fixtures.uniqueEmail;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.FilePayload;

import automatedtesting.lab08.lab08.service.UserService;
import automatedtesting.lab08.lab08.support.Screenshots;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

/**
 * The visual/snapshot method (UI): drive a real browser with Playwright and
 * assert the dashboard renders the 50 MB quota, attaching a screenshot to Allure.
 *
 * <p>If Playwright/Chromium cannot be provisioned (e.g. offline CI without the
 * browser installed) the test self-skips rather than failing the build.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Epic("UI")
@Feature("Dashboard")
class DashboardVisualTest {

    @LocalServerPort
    int port;
    @Autowired
    UserService users;

    static Playwright playwright;
    static Browser browser;

    @BeforeAll
    static void launch() {
        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch();
        } catch (Throwable t) {
            Assumptions.assumeTrue(false, "Playwright/Chromium unavailable: " + t.getMessage());
        }
    }

    @AfterAll
    static void shutdown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    /** Sign in through the real form-login page and land on the dashboard. */
    private void login(Page page, String email, String password) {
        page.navigate("http://localhost:" + port + "/login");
        page.getByTestId("email-input").fill(email);
        page.getByTestId("password-input").fill(password);
        page.getByTestId("login-btn").click();
        assertThat(page).hasURL(Pattern.compile(".*/dashboard"));
    }

    @Test
    @Story("The dashboard shows the user's 50 MB quota")
    @DisplayName("R2/UI — dashboard shows the 50 MB quota after login (visual)")
    void dashboardShowsFiftyMbQuota() {
        String email = uniqueEmail("ui");
        String password = "Secret123!";
        users.register(email, password);

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            login(page, email, password);

            Locator quota = page.getByTestId("quota");
            // --- visual / snapshot: web assertion + screenshot attachment ---
            assertThat(quota).hasText("50 MB");

            Screenshots.capture(page, "dashboard-quota.png", "dashboard");
        }
    }

    @Test
    @Story("Uploading a file through the browser shows it in the file list")
    @DisplayName("R6/UI — upload a file through the browser, see it listed (visual)")
    void uploadingFileShowsItInTheList() {
        String email = uniqueEmail("upload");
        String password = "Secret123!";
        users.register(email, password);

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            login(page, email, password);

            // starts empty, then upload via the real <input type=file> + form submit
            assertThat(page.getByTestId("empty")).isVisible();
            page.getByTestId("file-input").setInputFiles(new FilePayload(
                    "notes.txt", "text/plain", "hello cloud".getBytes()));
            page.getByTestId("upload-btn").click();

            // after the redirect, the file is listed in the UI
            assertThat(page.getByTestId("file-list")).containsText("notes.txt");

            Screenshots.capture(page, "dashboard-after-upload.png", "after upload");
        }
    }
}
