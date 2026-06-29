package automatedtesting.lab08.lab08;

import static automatedtesting.lab08.lab08.support.Fixtures.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.jayway.jsonpath.JsonPath;

import automatedtesting.lab08.lab08.service.UserService;
import automatedtesting.lab08.lab08.support.Http;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Folders over real HTTP (R5) with per-user isolation (R8).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Epic("Storage")
@Feature("Folders")
class FolderApiTest {

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

    private long createFolder(Account who, String name, Long parentId) {
        String json = parentId == null
                ? "{\"name\":\"" + name + "\"}"
                : "{\"name\":\"" + name + "\",\"parentId\":" + parentId + "}";
        Http.Resp res = Http.postJson(port, "/api/folders", who.email(), who.password(), json);
        assertThat(res.status()).isEqualTo(201);
        return ((Number) JsonPath.parse(res.body()).read("$.id")).longValue();
    }

    private List<String> folderNames(Account who, String query) {
        Http.Resp res = Http.get(port, "/api/folders" + query, who.email(), who.password());
        return JsonPath.parse(res.body()).read("$[*].name");
    }

    @Test
    @Story("Create a folder and find it in the listing")
    @DisplayName("R5 — create a folder and list it")
    void createAndListFolder() {
        Account u = newAccount("folder");
        createFolder(u, "Photos", null);

        // root listing includes the new folder and the starter "Documents"
        assertThat(folderNames(u, "")).contains("Photos", "Documents");
    }

    @Test
    @Story("Rename a folder")
    @DisplayName("R5 — rename a folder")
    void renameFolder() {
        Account u = newAccount("folder");
        long id = createFolder(u, "Temp", null);

        Http.Resp res = Http.patchJson(port, "/api/folders/" + id, u.email(), u.password(),
                "{\"name\":\"Renamed\"}");
        assertThat(res.status()).isEqualTo(200);

        assertThat(folderNames(u, "")).contains("Renamed").doesNotContain("Temp");
    }

    @Test
    @Story("Move a folder under another folder")
    @DisplayName("R5 — move a folder under another")
    void moveFolder() {
        Account u = newAccount("folder");
        long parent = createFolder(u, "Parent", null);
        long child = createFolder(u, "Child", null);

        Http.Resp res = Http.patchJson(port, "/api/folders/" + child, u.email(), u.password(),
                "{\"parentId\":" + parent + "}");
        assertThat(res.status()).isEqualTo(200);

        // now a child of Parent, and no longer at the root
        assertThat(folderNames(u, "?parent=" + parent)).contains("Child");
        assertThat(folderNames(u, "")).doesNotContain("Child");
    }

    @Test
    @Story("Delete a folder")
    @DisplayName("R5 — delete a folder")
    void deleteFolder() {
        Account u = newAccount("folder");
        long id = createFolder(u, "Disposable", null);

        Http.Resp res = Http.delete(port, "/api/folders/" + id, u.email(), u.password());
        assertThat(res.status()).isEqualTo(204);

        assertThat(folderNames(u, "")).doesNotContain("Disposable");
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("A user cannot rename or delete another user's folder")
    @DisplayName("R8 — cannot touch another user's folder (404)")
    void cannotTouchAnotherUsersFolder() {
        Account owner = newAccount("owner");
        Account intruder = newAccount("intruder");
        long id = createFolder(owner, "Private", null);

        assertThat(Http.patchJson(port, "/api/folders/" + id, intruder.email(),
                intruder.password(), "{\"name\":\"Hacked\"}").status()).isEqualTo(404);
        assertThat(Http.delete(port, "/api/folders/" + id, intruder.email(),
                intruder.password()).status()).isEqualTo(404);
    }
}
