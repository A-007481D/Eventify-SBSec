package com.eventify.controller;

import com.eventify.dto.ErrorResponse;
import com.eventify.dto.EventResponseDto;
import com.eventify.dto.RegistrationResponseDto;
import com.eventify.model.Event;
import com.eventify.model.Registration;
import com.eventify.model.User;
import com.eventify.service.EventService;
import com.eventify.service.RegistrationService;
import com.eventify.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    public UserController(UserService userService,
                         EventService eventService,
                         RegistrationService registrationService) {
        this.userService = userService;
        this.eventService = eventService;
        this.registrationService = registrationService;
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

    @PostMapping("/events/{id}/register")
    public ResponseEntity<?> registerForEvent(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());

        // Check if event exists
        Optional<Event> eventOpt = eventService.getEventById(id);
        if (eventOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(
                            LocalDateTime.now(),
                            404,
                            "Not Found",
                            "Event not found with id: " + id,
                            "/api/user/events/" + id + "/register"
                    )
            );
        }

        Event event = eventOpt.get();

        // Check if event is in the past
        if (event.getDateTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(
                            LocalDateTime.now(),
                            400,
                            "Bad Request",
                            "Cannot register for past events",
                            "/api/user/events/" + id + "/register"
                    )
            );
        }

        // Check if already registered
        if (registrationService.isUserRegisteredForEvent(user.getId(), id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ErrorResponse(
                            LocalDateTime.now(),
                            409,
                            "Conflict",
                            "You are already registered for this event",
                            "/api/user/events/" + id + "/register"
                    )
            );
        }

        // Check capacity
        if (registrationService.isEventAtCapacity(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(
                            LocalDateTime.now(),
                            400,
                            "Bad Request",
                            "Event is at full capacity",
                            "/api/user/events/" + id + "/register"
                    )
            );
        }

        // Register user
        Registration registration = registrationService.registerUserForEvent(user.getId(), id);
        long registrationCount = registrationService.getRegistrationCount(id);
        EventResponseDto eventDto = EventResponseDto.fromEvent(event, registrationCount);
        RegistrationResponseDto responseDto = RegistrationResponseDto.fromRegistration(registration, eventDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/registrations")
    public ResponseEntity<List<RegistrationResponseDto>> getUserRegistrations(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        List<Registration> registrations = registrationService.getUserRegistrations(user.getId());

        List<RegistrationResponseDto> responseDtos = registrations.stream()
                .map(registration -> {
                    Optional<Event> eventOpt = eventService.getEventById(registration.getEventId());
                    if (eventOpt.isPresent()) {
                        Event event = eventOpt.get();
                        long registrationCount = registrationService.getRegistrationCount(event.getId());
                        EventResponseDto eventDto = EventResponseDto.fromEvent(event, registrationCount);
                        return RegistrationResponseDto.fromRegistration(registration, eventDto);
                    }
                    return RegistrationResponseDto.fromRegistration(registration);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }
}