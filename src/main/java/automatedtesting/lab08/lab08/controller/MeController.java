package automatedtesting.lab08.lab08.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import automatedtesting.lab08.lab08.config.CurrentUser;
import automatedtesting.lab08.lab08.dto.Dtos.MeResponse;
import automatedtesting.lab08.lab08.dto.Dtos.UpdateProfileRequest;
import automatedtesting.lab08.lab08.model.Folder;
import automatedtesting.lab08.lab08.model.User;
import automatedtesting.lab08.lab08.service.FolderService;
import automatedtesting.lab08.lab08.service.StorageService;
import automatedtesting.lab08.lab08.service.UserService;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final CurrentUser currentUser;
    private final UserService users;
    private final StorageService storage;
    private final FolderService folders;

    public MeController(CurrentUser currentUser, UserService users, StorageService storage,
                        FolderService folders) {
        this.currentUser = currentUser;
        this.users = users;
        this.storage = storage;
        this.folders = folders;
    }

    /** R3 — GET /api/me -> profile + quota usage. */
    @GetMapping
    public MeResponse me() {
        return profileOf(currentUser.require());
    }

    /** R3 — PUT /api/me -> update display name / password. */
    @PutMapping
    public MeResponse update(@RequestBody UpdateProfileRequest req) {
        User updated = users.updateProfile(currentUser.require(), req.displayName(), req.password());
        return profileOf(updated);
    }

    /** R4 — DELETE /api/me -> delete own account + all data. */
    @DeleteMapping
    public ResponseEntity<Void> delete() {
        users.deleteAccount(currentUser.require());
        return ResponseEntity.noContent().build();
    }

    private MeResponse profileOf(User user) {
        var folderNames = folders.listAll(user).stream().map(Folder::getName).toList();
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getQuotaBytes(),
                storage.usedBytes(user),
                storage.freeBytes(user),
                folderNames);
    }
}
