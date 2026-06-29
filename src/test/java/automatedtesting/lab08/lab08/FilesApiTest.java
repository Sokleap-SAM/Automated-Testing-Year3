package automatedtesting.lab08.lab08;

import static automatedtesting.lab08.lab08.support.Fixtures.MB;
import static automatedtesting.lab08.lab08.support.Fixtures.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import automatedtesting.lab08.lab08.service.UserService;
import automatedtesting.lab08.lab08.support.Http;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Files over real HTTP (R6, R7, R8).
 *
 * <p>Demonstrates: <b>Contains</b>, <b>Collection</b>, <b>Content equals</b>
 * (byte round-trip + id round-trip), <b>Regex matched</b> (download link) and
 * <b>Exception</b> (over-quota 413, cross-user 404).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Epic("Storage")
@Feature("Files")
class FilesApiTest {

    @LocalServerPort
    int port;
    @Autowired
    UserService users;

    private record Account(String email, String password) {
    }

    private Account newAccount(String prefix) {
        String email = uniqueEmail(prefix);
        users.register(email, "Secret123!");
        return new Account(email, "Secret123!");
    }

    private Http.Resp upload(Account who, String filename, byte[] bytes) {
        return Http.uploadFile(port, "/api/files", who.email(), who.password(), filename, bytes);
    }

    @Test
    @Story("A folder listing contains an uploaded file's name")
    @DisplayName("R6 — listing contains an uploaded file's name (contains)")
    void listingContainsUploadedFileName() {
        Account u = newAccount("files");
        upload(u, "report.pdf", "hello".getBytes(StandardCharsets.UTF_8));

        Http.Resp res = Http.get(port, "/api/files", u.email(), u.password());
        List<String> names = JsonPath.parse(res.body()).read("$[*].name");

        // --- contains ---
        assertThat(names).contains("report.pdf");
    }

    @Test
    @Story("Listings are ordered, correctly sized and free of duplicates")
    @DisplayName("R6 — listing is ordered, sized and de-duplicated (collection)")
    void listingIsOrderedSizedAndDeduplicated() {
        Account u = newAccount("collection");
        upload(u, "gamma.txt", "g".getBytes());
        upload(u, "alpha.txt", "a".getBytes());
        upload(u, "beta.txt", "b".getBytes());

        Http.Resp res = Http.get(port, "/api/files", u.email(), u.password());
        List<String> names = JsonPath.parse(res.body()).read("$[*].name");

        // --- collection: size, ordering and uniqueness all asserted together ---
        assertThat(names)
                .hasSize(3)
                .doesNotHaveDuplicates()
                .containsExactly("alpha.txt", "beta.txt", "gamma.txt");
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Uploaded bytes and the file id round-trip through download")
    @DisplayName("R6 — file bytes & id round-trip through download (content equals)")
    void fileContentAndIdRoundTrip() {
        Account u = newAccount("roundtrip");
        byte[] original = "the quick brown fox".getBytes(StandardCharsets.UTF_8);

        DocumentContext created = JsonPath.parse(upload(u, "fox.txt", original).body());
        long id = ((Number) created.read("$.id")).longValue();

        Http.BytesResp downloaded = Http.getBytes(port, "/api/files/" + id + "/download",
                u.email(), u.password());

        // --- content equals: same id, same bytes ---
        assertThat(downloaded.status()).isEqualTo(200);
        assertThat(downloaded.body()).isEqualTo(original);
        assertThat(id).isPositive();
    }

    @Test
    @Story("The generated download link matches the expected URL shape")
    @DisplayName("R6 — download link matches URL pattern (regex)")
    void downloadLinkMatchesRegex() {
        Account u = newAccount("regex");
        DocumentContext created = JsonPath.parse(upload(u, "link.txt", "x".getBytes()).body());

        // --- regex: /api/files/<digits>/download ---
        assertThat((String) created.read("$.downloadUrl"))
                .matches("^/api/files/\\d+/download$");
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("An upload past the quota is rejected with a 4xx")
    @DisplayName("R7 — over-quota upload rejected with 413 (exception)")
    void overQuotaUploadIsRejected() {
        Account u = newAccount("overquota");
        byte[] tooBig = new byte[(int) (51 * MB)]; // > 50 MB quota

        Http.Resp res = upload(u, "huge.bin", tooBig);

        // --- exception (HTTP): 413 Payload Too Large ---
        assertThat(res.status()).isEqualTo(413);
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("A user cannot download another user's file over HTTP")
    @DisplayName("R8 — cannot download another user's file over HTTP (404)")
    void cannotDownloadAnotherUsersFile() {
        Account owner = newAccount("httpowner");
        Account intruder = newAccount("httpintruder");
        long id = ((Number) JsonPath.parse(upload(owner, "secret.txt", "top secret".getBytes())
                .body()).read("$.id")).longValue();

        Http.Resp res = Http.get(port, "/api/files/" + id + "/download",
                intruder.email(), intruder.password());

        // --- isolation: looks exactly like "not found" to the intruder ---
        assertThat(res.status()).isEqualTo(404);
    }
}
