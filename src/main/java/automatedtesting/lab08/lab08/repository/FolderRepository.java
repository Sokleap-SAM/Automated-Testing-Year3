package automatedtesting.lab08.lab08.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import automatedtesting.lab08.lab08.model.Folder;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByOwnerIdOrderByNameAsc(Long ownerId);

    List<Folder> findByOwnerIdAndParentIdOrderByNameAsc(Long ownerId, Long parentId);

    /** Isolation guard: a folder is only reachable through its owner. */
    Optional<Folder> findByIdAndOwnerId(Long id, Long ownerId);

    void deleteByOwnerId(Long ownerId);
}
