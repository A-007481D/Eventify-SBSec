package com.eventify.controller;

import com.eventify.dto.ErrorResponse;
import com.eventify.model.Event;
import com.eventify.model.User;
import com.eventify.service.EventService;
import com.eventify.service.RegistrationService;
import com.eventify.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    public AdminController(UserService userService,
                          EventService eventService,
                          RegistrationService registrationService) {
        this.userService = userService;
        this.eventService = eventService;
        this.registrationService = registrationService;
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
            @PathVariable("id") Long id,
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

    @DeleteMapping("/events/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        // Check if event exists
        Optional<Event> eventOpt = eventService.getEventById(id);
        if (eventOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(
                            LocalDateTime.now(),
                            404,
                            "Not Found",
                            "Event not found with id: " + id,
                            "/api/admin/events/" + id
                    )
            );
        }

        // Delete all registrations for this event first
        registrationService.deleteRegistrationsByEventId(id);

        // Delete the event
        eventService.deleteEvent(id);

        return ResponseEntity.noContent().build();
    }
}