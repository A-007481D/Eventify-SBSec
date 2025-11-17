package com.eventify.controller;

import com.eventify.model.User;
import com.eventify.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {

        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        if (!role.equals("ROLE_USER") &&
                !role.equals("ROLE_ORGANIZER") &&
                !role.equals("ROLE_ADMIN")) {
            return ResponseEntity.badRequest()
                    .body("Invalid role. Allowed: ROLE_USER, ROLE_ORGANIZER, ROLE_ADMIN");
        }

        User updatedUser = userService.updateUserRole(id, role);
        updatedUser.setPassword(null);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}