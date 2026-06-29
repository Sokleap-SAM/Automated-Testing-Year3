package automatedtesting.lab08.lab08.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import automatedtesting.lab08.lab08.model.User;
import automatedtesting.lab08.lab08.service.UserService;

/**
 * Resolves the {@link User} behind the current HTTP Basic credentials, so that
 * "each request acts as the logged-in user" (R2) and never touches anyone else.
 */
@Component
public class CurrentUser {

    private final UserService users;

    public CurrentUser(UserService users) {
        this.users = users;
    }

    public User require() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("No authenticated user");
        }
        return users.getByEmail(auth.getName());
    }
}
