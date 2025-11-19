package com.eventify.controller;

import com.eventify.dto.ErrorResponse;
import com.eventify.dto.EventDto;
import com.eventify.dto.EventResponseDto;
import com.eventify.model.Event;
import com.eventify.model.User;
import com.eventify.service.EventService;
import com.eventify.service.RegistrationService;
import com.eventify.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizer")
public class OrganizerController {

    private final UserService userService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    public OrganizerController(UserService userService,
                               EventService eventService,
                               RegistrationService registrationService) {
        this.userService = userService;
        this.eventService = eventService;
        this.registrationService = registrationService;
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventResponseDto>> getOrganizerEvents(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        List<Event> events = eventService.getEventsByOrganizerId(user.getId());

        List<EventResponseDto> eventDtos = events.stream()
                .map(event -> {
                    long registrationCount = registrationService.getRegistrationCount(event.getId());
                    return EventResponseDto.fromEvent(event, registrationCount);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventDtos);
    }

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(Authentication authentication, @Valid @RequestBody EventDto eventDto) {
        User user = userService.findByEmail(authentication.getName());

        Event event = new Event();
        event.setTitle(eventDto.getTitle());
        event.setDescription(eventDto.getDescription());
        event.setLocation(eventDto.getLocation());
        event.setDateTime(eventDto.getDateTime());
        event.setCapacity(eventDto.getCapacity());
        event.setOrganizerId(user.getId());

        Event savedEvent = eventService.createEvent(event);
        EventResponseDto responseDto = EventResponseDto.fromEvent(savedEvent, 0L);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id,
                                         Authentication authentication,
                                         @Valid @RequestBody EventDto eventDto) {
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
                            "/api/organizer/events/" + id
                    )
            );
        }

        Event event = eventOpt.get();

        // Check if user is the owner
        if (!event.getOrganizerId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse(
                            LocalDateTime.now(),
                            403,
                            "Forbidden",
                            "You are not authorized to update this event",
                            "/api/organizer/events/" + id
                    )
            );
        }

        // Update event fields
        event.setTitle(eventDto.getTitle());
        event.setDescription(eventDto.getDescription());
        event.setLocation(eventDto.getLocation());
        event.setDateTime(eventDto.getDateTime());
        event.setCapacity(eventDto.getCapacity());

        Event updatedEvent = eventService.updateEvent(event);
        long registrationCount = registrationService.getRegistrationCount(id);
        EventResponseDto responseDto = EventResponseDto.fromEvent(updatedEvent, registrationCount);

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id, Authentication authentication) {
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
                            "/api/organizer/events/" + id
                    )
            );
        }

        Event event = eventOpt.get();

        // Check if user is the owner
        if (!event.getOrganizerId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse(
                            LocalDateTime.now(),
                            403,
                            "Forbidden",
                            "You are not authorized to delete this event",
                            "/api/organizer/events/" + id
                    )
            );
        }

        // Delete all registrations for this event first
        registrationService.deleteRegistrationsByEventId(id);

        // Delete the event
        eventService.deleteEvent(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getOrganizerProfile(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }
}
