package automatedtesting.lab08.lab08.dto;

import java.time.Instant;
import java.util.List;

import automatedtesting.lab08.lab08.model.FileEntity;
import automatedtesting.lab08.lab08.model.Folder;
import automatedtesting.lab08.lab08.model.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request/response payloads for the REST API, grouped in one file for brevity.
 */
public final class Dtos {

    private Dtos() {
    }

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record UpdateProfileRequest(String displayName, String password) {
    }

    public record CreateFolderRequest(@NotBlank String name, Long parentId) {
    }

    public record UpdateFolderRequest(String name, Long parentId) {
    }

    public record UpdateFileRequest(String name, Long folderId) {
    }

    /** Shape returned by GET /api/me — the contract the schema test pins. */
    public record MeResponse(
            Long id,
            String email,
            String displayName,
            long quotaBytes,
            long usedBytes,
            long freeBytes,
            List<String> folders) {
    }

    public record FolderDto(Long id, String name, Long parentId) {
        public static FolderDto of(Folder f) {
            return new FolderDto(f.getId(), f.getName(), f.getParentId());
        }
    }

    public record FileDto(
            Long id,
            String name,
            Long folderId,
            long sizeBytes,
            Instant createdAt,
            String downloadUrl) {
        public static FileDto of(FileEntity f) {
            return new FileDto(f.getId(), f.getName(), f.getFolderId(), f.getSizeBytes(),
                    f.getCreatedAt(), "/api/files/" + f.getId() + "/download");
        }
    }

    public record UserDto(Long id, String email, String displayName) {
        public static UserDto of(User u) {
            return new UserDto(u.getId(), u.getEmail(), u.getDisplayName());
        }
    }
}
