package automatedtesting.lab08.lab08.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import automatedtesting.lab08.lab08.exception.NotFoundException;
import automatedtesting.lab08.lab08.model.Folder;
import automatedtesting.lab08.lab08.model.User;
import automatedtesting.lab08.lab08.repository.FolderRepository;

/**
 * Folders (R5) with per-user isolation (R8).
 */
@Service
public class FolderService {

    private final FolderRepository folders;

    public FolderService(FolderRepository folders) {
        this.folders = folders;
    }

    @Transactional
    public Folder create(User owner, String name, Long parentId) {
        if (parentId != null) {
            // a folder can only be nested under one the user already owns
            requireOwned(owner, parentId);
        }
        return folders.save(new Folder(owner.getId(), name, parentId));
    }

    public List<Folder> list(User owner, Long parentId) {
        return folders.findByOwnerIdAndParentIdOrderByNameAsc(owner.getId(), parentId);
    }

    public List<Folder> listAll(User owner) {
        return folders.findByOwnerIdOrderByNameAsc(owner.getId());
    }

    public Folder get(User owner, Long folderId) {
        return requireOwned(owner, folderId);
    }

    @Transactional
    public Folder rename(User owner, Long folderId, String newName) {
        Folder folder = requireOwned(owner, folderId);
        folder.setName(newName);
        return folders.save(folder);
    }

    @Transactional
    public Folder move(User owner, Long folderId, Long newParentId) {
        Folder folder = requireOwned(owner, folderId);
        if (newParentId != null) {
            requireOwned(owner, newParentId);
        }
        folder.setParentId(newParentId);
        return folders.save(folder);
    }

    @Transactional
    public void delete(User owner, Long folderId) {
        folders.delete(requireOwned(owner, folderId));
    }

    private Folder requireOwned(User owner, Long folderId) {
        return folders.findByIdAndOwnerId(folderId, owner.getId())
                .orElseThrow(() -> new NotFoundException("No such folder: " + folderId));
    }
}
