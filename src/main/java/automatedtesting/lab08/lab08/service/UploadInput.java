package automatedtesting.lab08.lab08.service;

/**
 * A name + bytes pair handed to {@link StorageService#upload}. Using a small value
 * object (instead of Spring's MultipartFile) keeps the service callable directly
 * from unit tests, e.g. {@code storage.upload(user, file(10 * MB))}.
 */
public record UploadInput(String name, byte[] content) {

    public long sizeBytes() {
        return content.length;
    }
}
