package automatedtesting.lab08.lab08.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import automatedtesting.lab08.lab08.config.CurrentUser;
import automatedtesting.lab08.lab08.dto.Dtos.FileDto;
import automatedtesting.lab08.lab08.dto.Dtos.UpdateFileRequest;
import automatedtesting.lab08.lab08.model.FileEntity;
import automatedtesting.lab08.lab08.model.User;
import automatedtesting.lab08.lab08.service.StorageService;
import automatedtesting.lab08.lab08.service.UploadInput;

/** R6 — files, scoped to the current user (R8). */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final CurrentUser currentUser;
    private final StorageService storage;

    public FileController(CurrentUser currentUser, StorageService storage) {
        this.currentUser = currentUser;
        this.storage = storage;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDto> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "folder", required = false) Long folderId) throws IOException {
        User user = currentUser.require();
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
        FileEntity saved = storage.upload(user, folderId, new UploadInput(name, file.getBytes()));
        return ResponseEntity.status(HttpStatus.CREATED).body(FileDto.of(saved));
    }

    @GetMapping
    public List<FileDto> list(@RequestParam(name = "folder", required = false) Long folderId) {
        User user = currentUser.require();
        return storage.listFiles(user, folderId).stream().map(FileDto::of).toList();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        FileEntity file = storage.download(currentUser.require(), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file.getContent());
    }

    @PatchMapping("/{id}")
    public FileDto update(@PathVariable Long id, @RequestBody UpdateFileRequest req) {
        User user = currentUser.require();
        FileEntity file = storage.download(user, id);
        if (req.name() != null && !req.name().isBlank()) {
            file = storage.rename(user, id, req.name());
        }
        if (req.folderId() != null) {
            file = storage.move(user, id, req.folderId());
        }
        return FileDto.of(file);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        storage.delete(currentUser.require(), id);
        return ResponseEntity.noContent().build();
    }
}
