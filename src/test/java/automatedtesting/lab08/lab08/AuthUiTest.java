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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import automatedtesting.lab08.lab08.service.UserService;
import automatedtesting.lab08.lab08.support.Screenshots;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Browser-driven signup, login, bad-login and logout flows (R1, R2) via Playwright.
 *
 * <p>Demonstrates the <b>Visual/snapshot</b> method end-to-end over session-based
 * form login. Self-skips if Playwright/Chromium cannot be provisioned.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Epic("UI")
@Feature("Authentication")
class AuthUiTest {

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

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("A visitor can sign up and then log in to reach their dashboard")
    @DisplayName("R1/R2 — sign up then log in to reach the dashboard (visual)")
    void signUpThenLogIn() {
        String email = uniqueEmail("ui-signup");
        String password = "Secret123!";

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();

            // --- sign up ---
            page.navigate(url("/signup"));
            page.getByTestId("signup-email").fill(email);
            page.getByTestId("signup-password").fill(password);
            page.getByTestId("signup-btn").click();

            // redirected to the login page with a success notice
            assertThat(page).hasURL(Pattern.compile(".*/login.*"));
            assertThat(page.getByTestId("registered")).isVisible();

            // --- log in ---
            page.getByTestId("email-input").fill(email);
            page.getByTestId("password-input").fill(password);
            page.getByTestId("login-btn").click();

            // landed on the dashboard, showing the granted 50 MB quota
            assertThat(page).hasURL(Pattern.compile(".*/dashboard"));
            assertThat(page.getByTestId("quota")).hasText("50 MB");

            Screenshots.capture(page, "auth-after-login.png", "after login");
        }
    }

    @Test
    @Story("Logging in with the wrong password is rejected")
    @DisplayName("R2 — login with the wrong password is rejected (visual)")
    void loginWithWrongPasswordIsRejected() {
        String email = uniqueEmail("ui-bad");
        users.register(email, "Secret123!");

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(url("/login"));
            page.getByTestId("email-input").fill(email);
            page.getByTestId("password-input").fill("wrong-password");
            page.getByTestId("login-btn").click();

            // bounced back to the login page with an error
            assertThat(page).hasURL(Pattern.compile(".*/login\\?error"));
            assertThat(page.getByTestId("login-error")).isVisible();

            Screenshots.capture(page, "auth-login-error.png", "login error");
        }
    }

    @Test
    @Story("A signed-in user can log out")
    @DisplayName("R2 — a signed-in user can log out (visual)")
    void canLogOut() {
        String email = uniqueEmail("ui-logout");
        String password = "Secret123!";
        users.register(email, password);

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(url("/login"));
            page.getByTestId("email-input").fill(email);
            page.getByTestId("password-input").fill(password);
            page.getByTestId("login-btn").click();
            assertThat(page).hasURL(Pattern.compile(".*/dashboard"));

            // --- log out ---
            page.getByTestId("logout-btn").click();
            assertThat(page).hasURL(Pattern.compile(".*/login\\?logout"));
            assertThat(page.getByTestId("logout-msg")).isVisible();

            Screenshots.capture(page, "auth-after-logout.png", "after logout");
        }
    }
}
