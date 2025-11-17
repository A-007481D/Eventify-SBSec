package com.eventify.controller;

import com.eventify.model.User;
import com.eventify.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        user.setPassword(null); // Don't expose password
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestBody User updatedInfo) {

        User currentUser = userService.findByEmail(authentication.getName());

        currentUser.setName(updatedInfo.getName());

        User savedUser = userService.getCurrentUser();
        savedUser.setPassword(null);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testUserAccess(Authentication authentication) {
        return ResponseEntity.ok("Hello " + authentication.getName() + "! You have USER access.");
    }
}