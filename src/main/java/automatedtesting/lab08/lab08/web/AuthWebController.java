package automatedtesting.lab08.lab08.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import automatedtesting.lab08.lab08.service.UserService;

/**
 * Browser-facing login & signup pages. Login itself is handled by Spring
 * Security's form-login filter (POST /login); this controller just renders the
 * pages and processes the signup form by delegating to {@link UserService}.
 */
@Controller
public class AuthWebController {

    private final UserService users;

    public AuthWebController(UserService users) {
        this.users = users;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String doSignup(@RequestParam String email, @RequestParam String password) {
        try {
            users.register(email, password);
        } catch (IllegalArgumentException e) {
            return "redirect:/signup?error";
        }
        return "redirect:/login?registered";
    }
}
