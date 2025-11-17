package com.eventify.controller;

import com.eventify.dto.*;
import com.eventify.model.User;
import com.eventify.security.TokenService;
import com.eventify.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public PublicController(UserService userService,
                            AuthenticationManager authenticationManager,
                            TokenService tokenService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/users")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDto dto) {
        try {
            User user = userService.registerUser(dto);
            user.setPassword(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(
                            java.time.LocalDateTime.now(),
                            400,
                            "Bad Request",
                            e.getMessage(),
                            "/api/public/users"
                    )
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = userService.findByEmail(loginRequest.getEmail());
            String token = tokenService.generateToken(user);

            AuthResponse response = new AuthResponse(token, user.getEmail(), user.getRole());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse(
                            java.time.LocalDateTime.now(),
                            401,
                            "Unauthorized",
                            "Invalid email or password",
                            "/api/public/login"
                    )
            );
        }
    }

    @GetMapping("/events")
    public ResponseEntity<String> getPublicEvents() {
        return ResponseEntity.ok("List of all public events - no auth required");
    }
}