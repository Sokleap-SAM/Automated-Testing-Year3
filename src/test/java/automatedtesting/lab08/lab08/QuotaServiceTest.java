package automatedtesting.lab08.lab08;

import static automatedtesting.lab08.lab08.support.Fixtures.MB;
import static automatedtesting.lab08.lab08.support.Fixtures.file;
import static automatedtesting.lab08.lab08.support.Fixtures.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.function.Predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import automatedtesting.lab08.lab08.exception.QuotaExceededException;
import automatedtesting.lab08.lab08.model.User;
import automatedtesting.lab08.lab08.service.StorageService;
import automatedtesting.lab08.lab08.service.UserService;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Quota maths at the service layer (R1, R7).
 *
 * <p>Demonstrates: <b>Content equals</b>, <b>Formula matched</b>, <b>Exception</b>,
 * <b>Tolerance</b> and <b>Predicate</b>.
 */
@SpringBootTest
@Epic("Storage")
@Feature("Quota")
class QuotaServiceTest {

    @Autowired
    StorageService storage;
    @Autowired
    UserService users;

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("A new user is granted exactly 50 MB and the free-space formula holds")
    @DisplayName("R1/R7 — new user gets 50 MB; free = quota − Σ(sizes); over-quota throws")
    void newUserGets50MbAndFormulaHolds() {
        User u = Allure.step("Register a new user",
                () -> users.register(uniqueEmail("sok"), "Secret123!"));

        // --- equals: every new account is granted exactly 50 MB ---
        assertThat(u.getQuotaBytes()).isEqualTo(50 * MB);

        storage.upload(u, file(10 * MB));
        storage.upload(u, file(15 * MB));

        // --- formula: free = quota - Σ(file sizes) ---
        long free = u.getQuotaBytes() - 25 * MB;
        assertThat(storage.freeBytes(u)).isEqualTo(free);

        // --- exception: an upload past the quota is rejected ---
        assertThatThrownBy(() -> storage.upload(u, file(40 * MB)))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("exceed");
    }

    @Test
    @Story("Used storage reported in MB is within tolerance of the expected value")
    @DisplayName("R7 — used MB is within tolerance of the expected value")
    void usedMegabytesAreWithinTolerance() {
        User u = users.register(uniqueEmail("tolerance"), "pw");
        storage.upload(u, file(25 * MB));

        // --- tolerance: 25 MB give-or-take a rounding epsilon ---
        double usedMb = storage.usedBytes(u) / (double) MB;
        assertThat(usedMb).isCloseTo(25.0, within(0.001));
    }

    @Test
    @Story("usedBytes never exceeds quotaBytes")
    @DisplayName("R7 — usedBytes never exceeds quota (predicate)")
    void usedBytesNeverExceedsQuota() {
        User u = users.register(uniqueEmail("predicate"), "pw");
        storage.upload(u, file(10 * MB));
        storage.upload(u, file(20 * MB));

        // --- predicate: the core invariant, expressed as a Predicate ---
        Predicate<User> withinQuota = x -> storage.usedBytes(x) <= x.getQuotaBytes();
        assertThat(u).matches(withinQuota, "usedBytes <= quotaBytes");
    }
}
