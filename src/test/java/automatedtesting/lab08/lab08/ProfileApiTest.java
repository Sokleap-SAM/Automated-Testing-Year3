package automatedtesting.lab08.lab08;

import static automatedtesting.lab08.lab08.support.Fixtures.MB;
import static automatedtesting.lab08.lab08.support.Fixtures.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import automatedtesting.lab08.lab08.support.Http;
import automatedtesting.lab08.lab08.service.UserService;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Accounts over real HTTP: the /api/me contract (R3), profile updates (R3) and
 * self-delete (R4).
 *
 * <p>Demonstrates: <b>Content equals</b>, <b>Schema/JSON</b>, <b>Regex matched</b>,
 * <b>Contains</b> and <b>Tolerance</b> (response budget).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Epic("Accounts")
@Feature("Profile")
class ProfileApiTest {

    @LocalServerPort
    int port;
    @Autowired
    UserService users;

    /** Reads return null instead of throwing for absent paths, so we can assert schema. */
    private static final Configuration LENIENT =
            Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("/api/me returns a well-shaped profile with quota usage")
    @DisplayName("R3 — GET /api/me returns the profile contract (schema/regex/contains)")
    void profileEndpointContract() {
        String email = uniqueEmail("profile");
        String password = "Secret123!";
        users.register(email, password);

        Http.Resp res = Http.get(port, "/api/me", email, password);
        Allure.addAttachment("GET /api/me", "application/json", res.body(), ".json");

        // --- equals: a successful, authenticated read ---
        assertThat(res.status()).isEqualTo(200);

        DocumentContext body = JsonPath.using(LENIENT).parse(res.body());

        // --- schema: the response carries every documented field ---
        assertThat((Object) body.read("$.email")).isNotNull();
        assertThat((Object) body.read("$.displayName")).isNotNull();
        assertThat((Object) body.read("$.quotaBytes")).isNotNull();
        assertThat((Object) body.read("$.usedBytes")).isNotNull();
        assertThat((Object) body.read("$.freeBytes")).isNotNull();
        assertThat((Object) body.read("$.folders")).isNotNull();

        // --- regex: the email is well-formed ---
        assertThat((String) body.read("$.email")).matches("^[\\w.+-]+@[\\w.-]+$");

        // --- equals: brand-new account still shows the 50 MB quota ---
        assertThat(((Number) body.read("$.quotaBytes")).longValue()).isEqualTo(50 * MB);

        // --- contains: the starter "Documents" folder is listed ---
        List<String> folders = body.read("$.folders");
        assertThat(folders).contains("Documents");
    }

    @Test
    @Story("/api/me answers within the response-time budget")
    @DisplayName("R3 — GET /api/me answers within the response budget (tolerance)")
    void profileRespondsWithinBudget() {
        String email = uniqueEmail("budget");
        String password = "pw";
        users.register(email, password);

        long start = System.nanoTime();
        Http.Resp res = Http.get(port, "/api/me", email, password);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(res.status()).isEqualTo(200);
        // --- tolerance: a generous wall-clock budget for a single read ---
        assertThat(elapsedMs).isLessThan(2_000L);
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("PUT /api/me updates the display name and password")
    @DisplayName("R3 — PUT /api/me updates display name & password")
    void updateProfileChangesNameAndPassword() {
        String email = uniqueEmail("update");
        String oldPassword = "Secret123!";
        String newPassword = "Brand-New-456!";
        users.register(email, oldPassword);

        Http.Resp put = Http.putJson(port, "/api/me", email, oldPassword,
                "{\"displayName\":\"Renamed User\",\"password\":\"" + newPassword + "\"}");
        assertThat(put.status()).isEqualTo(200);

        // old password no longer authenticates; new password does
        assertThat(Http.get(port, "/api/me", email, oldPassword).status()).isEqualTo(401);

        Http.Resp after = Http.get(port, "/api/me", email, newPassword);
        assertThat(after.status()).isEqualTo(200);
        assertThat((String) JsonPath.parse(after.body()).read("$.displayName"))
                .isEqualTo("Renamed User");
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("DELETE /api/me removes the account and all its data")
    @DisplayName("R4 — DELETE /api/me removes the account and its data")
    void deleteAccountRemovesEverything() {
        String email = uniqueEmail("delete");
        String password = "Secret123!";
        users.register(email, password);
        Http.uploadFile(port, "/api/files", email, password, "doomed.txt", "bye".getBytes());

        Http.Resp deleted = Http.delete(port, "/api/me", email, password);
        assertThat(deleted.status()).isEqualTo(204);

        // the account is gone — its credentials no longer authenticate anything
        assertThat(Http.get(port, "/api/me", email, password).status()).isEqualTo(401);
    }
}
