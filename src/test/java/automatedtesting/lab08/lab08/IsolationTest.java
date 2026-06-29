package automatedtesting.lab08.lab08;

import static automatedtesting.lab08.lab08.support.Fixtures.named;
import static automatedtesting.lab08.lab08.support.Fixtures.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import automatedtesting.lab08.lab08.exception.NotFoundException;
import automatedtesting.lab08.lab08.model.FileEntity;
import automatedtesting.lab08.lab08.model.User;
import automatedtesting.lab08.lab08.service.StorageService;
import automatedtesting.lab08.lab08.service.UserService;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * The hard rule (R8): a user must never read, write or delete another user's data.
 *
 * <p>Demonstrates: <b>Collection</b>, <b>Predicate</b> and <b>Exception</b>.
 */
@SpringBootTest
@Epic("Storage")
@Feature("Isolation")
class IsolationTest {

    @Autowired
    StorageService storage;
    @Autowired
    UserService users;

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("Two users cannot see each other's files")
    @DisplayName("R8 — two users cannot see each other's files")
    void usersCannotSeeEachOther() {
        User a = users.register(uniqueEmail("a"), "pw");
        User b = users.register(uniqueEmail("b"), "pw");
        storage.upload(a, named("secret.txt"));

        List<String> bFiles = storage.list(b, "/");

        // --- collection: b's listing contains nothing of a's, and is empty ---
        assertThat(bFiles).doesNotContain("secret.txt").isEmpty();

        // --- predicate: b consumes no storage at all ---
        assertThat(b).matches(x -> storage.usedBytes(x) == 0, "b uses nothing");
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @Story("A user cannot download another user's file by id")
    @DisplayName("R8 — cannot download another user's file by id (service)")
    void cannotDownloadAnotherUsersFile() {
        User a = users.register(uniqueEmail("owner"), "pw");
        User b = users.register(uniqueEmail("intruder"), "pw");
        FileEntity victim = storage.upload(a, named("private.txt"));

        // --- exception: reaching for someone else's file id is a 404, not a leak ---
        assertThatThrownBy(() -> storage.download(b, victim.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
