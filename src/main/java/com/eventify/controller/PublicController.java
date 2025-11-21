package com.eventify.controller;

import com.eventify.dto.ErrorResponse;
import com.eventify.dto.EventResponseDto;
import com.eventify.dto.UserRegistrationDto;
import com.eventify.model.Event;
import com.eventify.model.User;
import com.eventify.service.EventService;
import com.eventify.service.RegistrationService;
import com.eventify.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final UserService userService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    public PublicController(UserService userService,
                           EventService eventService,
                           RegistrationService registrationService) {
        this.userService = userService;
        this.eventService = eventService;
        this.registrationService = registrationService;
    }

    @PostMapping("/users")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto dto) {
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

    @GetMapping("/events")
    public ResponseEntity<List<EventResponseDto>> getPublicEvents() {
        List<Event> events = eventService.getUpcomingEvents();
        List<EventResponseDto> eventDtos = events.stream()
                .map(event -> {
                    long registrationCount = registrationService.getRegistrationCount(event.getId());
                    return EventResponseDto.fromEvent(event, registrationCount);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventDtos);
    }
}