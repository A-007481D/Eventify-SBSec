package com.eventify.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizer")
public class OrganizerController {

    @GetMapping("/events")
    public ResponseEntity<String> getOrganizerEvents(Authentication authentication) {
        return ResponseEntity.ok("Organizer " + authentication.getName() + " is viewing their events");
    }

    @PostMapping("/events")
    public ResponseEntity<String> createEvent(Authentication authentication, @RequestBody String eventDetails) {
        return ResponseEntity.ok("Organizer " + authentication.getName() + " created a new event: " + eventDetails);
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getOrganizerProfile(Authentication authentication) {
        return ResponseEntity.ok("Organizer profile for: " + authentication.getName());
    }
}