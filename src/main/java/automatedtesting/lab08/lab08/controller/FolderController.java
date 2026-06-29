package automatedtesting.lab08.lab08.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import automatedtesting.lab08.lab08.config.CurrentUser;
import automatedtesting.lab08.lab08.dto.Dtos.CreateFolderRequest;
import automatedtesting.lab08.lab08.dto.Dtos.FolderDto;
import automatedtesting.lab08.lab08.dto.Dtos.UpdateFolderRequest;
import automatedtesting.lab08.lab08.model.Folder;
import automatedtesting.lab08.lab08.model.User;
import automatedtesting.lab08.lab08.service.FolderService;

import jakarta.validation.Valid;

/** R5 — folders, scoped to the current user (R8). */
@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final CurrentUser currentUser;
    private final FolderService folders;

    public FolderController(CurrentUser currentUser, FolderService folders) {
        this.currentUser = currentUser;
        this.folders = folders;
    }

    @PostMapping
    public ResponseEntity<FolderDto> create(@Valid @RequestBody CreateFolderRequest req) {
        User user = currentUser.require();
        Folder folder = folders.create(user, req.name(), req.parentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(FolderDto.of(folder));
    }

    @GetMapping
    public List<FolderDto> list(@RequestParam(name = "parent", required = false) Long parentId) {
        User user = currentUser.require();
        return folders.list(user, parentId).stream().map(FolderDto::of).toList();
    }

    @PatchMapping("/{id}")
    public FolderDto update(@PathVariable Long id, @RequestBody UpdateFolderRequest req) {
        User user = currentUser.require();
        Folder folder = folders.get(user, id);
        if (req.name() != null && !req.name().isBlank()) {
            folder = folders.rename(user, id, req.name());
        }
        if (req.parentId() != null) {
            folder = folders.move(user, id, req.parentId());
        }
        return FolderDto.of(folder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        folders.delete(currentUser.require(), id);
        return ResponseEntity.noContent().build();
    }
}
