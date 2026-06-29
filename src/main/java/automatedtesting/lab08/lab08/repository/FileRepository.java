package automatedtesting.lab08.lab08.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import automatedtesting.lab08.lab08.model.FileEntity;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findByOwnerIdOrderByNameAsc(Long ownerId);

    List<FileEntity> findByOwnerIdAndFolderIdOrderByNameAsc(Long ownerId, Long folderId);

    /** Isolation guard: a file is only reachable through its owner. */
    Optional<FileEntity> findByIdAndOwnerId(Long id, Long ownerId);

    void deleteByOwnerId(Long ownerId);

    /** Sum of all file sizes for a user — the basis of the quota formula. */
    @org.springframework.data.jpa.repository.Query(
            "select coalesce(sum(f.sizeBytes), 0) from FileEntity f where f.ownerId = :ownerId")
    long sumSizeByOwnerId(Long ownerId);
}
