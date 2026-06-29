package automatedtesting.lab08.lab08.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import automatedtesting.lab08.lab08.dto.Dtos.LoginRequest;
import automatedtesting.lab08.lab08.dto.Dtos.RegisterRequest;
import automatedtesting.lab08.lab08.dto.Dtos.UserDto;
import automatedtesting.lab08.lab08.model.User;
import automatedtesting.lab08.lab08.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    /** R1 — POST /api/auth/register -> 201 with the new user (granted 50 MB). */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest req) {
        User user = users.register(req.email(), req.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.of(user));
    }

    /** R2 — POST /api/auth/login verifies credentials. Subsequent requests use
     *  HTTP Basic with the same email/password. */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest req) {
        User user = users.getByEmail(req.email());
        if (!users.passwordMatches(user, req.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(UserDto.of(user));
    }
}
