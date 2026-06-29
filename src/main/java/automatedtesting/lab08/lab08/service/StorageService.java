package automatedtesting.lab08.lab08.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import automatedtesting.lab08.lab08.exception.NotFoundException;
import automatedtesting.lab08.lab08.exception.QuotaExceededException;
import automatedtesting.lab08.lab08.model.FileEntity;
import automatedtesting.lab08.lab08.model.User;
import automatedtesting.lab08.lab08.repository.FileRepository;
import automatedtesting.lab08.lab08.repository.UserRepository;

/**
 * Files + quota (R6, R7) with per-user isolation (R8). Every read/write is scoped
 * to an owner id, so a user can only ever touch their own bytes.
 */
@Service
public class StorageService {

    private final FileRepository files;
    private final UserRepository users;

    public StorageService(FileRepository files, UserRepository users) {
        this.files = files;
        this.users = users;
    }

    /** Upload to the storage root. */
    public FileEntity upload(User owner, UploadInput input) {
        return upload(owner, null, input);
    }

    /** R6/R7 — upload to a folder, rejecting anything that would exceed the quota. */
    @Transactional
    public FileEntity upload(User owner, Long folderId, UploadInput input) {
        long projected = usedBytes(owner) + input.sizeBytes();
        if (projected > owner.getQuotaBytes()) {
            throw new QuotaExceededException(
                    "Upload of " + input.sizeBytes() + " bytes would exceed quota; "
                            + freeBytes(owner) + " bytes free");
        }
        FileEntity saved = files.save(
                new FileEntity(owner.getId(), folderId, input.name(), input.content()));
        syncUsedBytes(owner);
        return saved;
    }

    /** R6 — download by id, but only if the file belongs to {@code owner} (R8). */
    public FileEntity download(User owner, Long fileId) {
        return files.findByIdAndOwnerId(fileId, owner.getId())
                .orElseThrow(() -> new NotFoundException("No such file: " + fileId));
    }

    @Transactional
    public FileEntity rename(User owner, Long fileId, String newName) {
        FileEntity file = download(owner, fileId);
        file.setName(newName);
        return files.save(file);
    }

    @Transactional
    public FileEntity move(User owner, Long fileId, Long newFolderId) {
        FileEntity file = download(owner, fileId);
        file.setFolderId(newFolderId);
        return files.save(file);
    }

    @Transactional
    public void delete(User owner, Long fileId) {
        FileEntity file = download(owner, fileId);
        files.delete(file);
        syncUsedBytes(owner);
    }

    /** Files at the storage root, by name. The {@code path} is accepted for a
     *  natural call site ({@code list(user, "/")}); only the root is modelled. */
    public List<String> list(User owner, String path) {
        return files.findByOwnerIdAndFolderIdOrderByNameAsc(owner.getId(), null)
                .stream().map(FileEntity::getName).toList();
    }

    public List<FileEntity> listFiles(User owner, Long folderId) {
        return files.findByOwnerIdAndFolderIdOrderByNameAsc(owner.getId(), folderId);
    }

    public List<FileEntity> listAll(User owner) {
        return files.findByOwnerIdOrderByNameAsc(owner.getId());
    }

    /** Σ(file sizes) for the user. */
    public long usedBytes(User owner) {
        return files.sumSizeByOwnerId(owner.getId());
    }

    /** The quota formula: free = quota − Σ(file sizes). */
    public long freeBytes(User owner) {
        return owner.getQuotaBytes() - usedBytes(owner);
    }

    private void syncUsedBytes(User owner) {
        users.findById(owner.getId()).ifPresent(u -> {
            long used = files.sumSizeByOwnerId(u.getId());
            u.setUsedBytes(used);
            users.save(u);
            owner.setUsedBytes(used);
        });
    }
}
