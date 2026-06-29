package automatedtesting.lab08.lab08.support;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import automatedtesting.lab08.lab08.service.UploadInput;

/**
 * Tiny helpers shared by the test suite so call sites read like the lecture
 * slides, e.g. {@code storage.upload(user, file(10 * MB))}.
 */
public final class Fixtures {

    /** One megabyte in bytes. */
    public static final long MB = 1_048_576L;

    private static final AtomicInteger SEQ = new AtomicInteger();

    private Fixtures() {
    }

    /** A file of exactly {@code size} bytes (zero-filled). */
    public static UploadInput file(long size) {
        return new UploadInput("file-" + SEQ.incrementAndGet() + ".bin", new byte[(int) size]);
    }

    /** A small named file with deterministic contents. */
    public static UploadInput named(String name) {
        return new UploadInput(name, ("contents of " + name).getBytes(StandardCharsets.UTF_8));
    }

    /** A unique, well-formed email so tests never collide on registration. */
    public static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@itc.edu";
    }
}
