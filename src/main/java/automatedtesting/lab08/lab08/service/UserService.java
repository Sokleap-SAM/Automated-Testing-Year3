package automatedtesting.lab08.lab08.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import automatedtesting.lab08.lab08.exception.NotFoundException;
import automatedtesting.lab08.lab08.model.Folder;
import automatedtesting.lab08.lab08.model.User;
import automatedtesting.lab08.lab08.repository.FileRepository;
import automatedtesting.lab08.lab08.repository.FolderRepository;
import automatedtesting.lab08.lab08.repository.UserRepository;

/**
 * Accounts: register (R1), profile (R3) and self-delete (R4).
 */
@Service
public class UserService {

    private final UserRepository users;
    private final FolderRepository folders;
    private final FileRepository files;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, FolderRepository folders, FileRepository files,
                       PasswordEncoder passwordEncoder) {
        this.users = users;
        this.folders = folders;
        this.files = files;
        this.passwordEncoder = passwordEncoder;
    }

    /** R1 — create a user with a 50 MB quota and a starter "Documents" folder. */
    @Transactional
    public User register(String email, String rawPassword) {
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        String displayName = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        User user = new User(email, displayName, passwordEncoder.encode(rawPassword));
        user = users.save(user);
        folders.save(new Folder(user.getId(), "Documents", null));
        return user;
    }

    public User getByEmail(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No such user: " + email));
    }

    public boolean passwordMatches(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    /** R3 — update display name and/or password; blank fields are left untouched. */
    @Transactional
    public User updateProfile(User user, String displayName, String rawPassword) {
        User managed = users.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("No such user"));
        if (displayName != null && !displayName.isBlank()) {
            managed.setDisplayName(displayName);
        }
        if (rawPassword != null && !rawPassword.isBlank()) {
            managed.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        return users.save(managed);
    }

    /** R4 — delete the account and every file/folder it owns. */
    @Transactional
    public void deleteAccount(User user) {
        files.deleteByOwnerId(user.getId());
        folders.deleteByOwnerId(user.getId());
        users.deleteById(user.getId());
    }
}
