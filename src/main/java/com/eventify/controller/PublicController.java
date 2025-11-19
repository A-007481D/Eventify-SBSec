package com.eventify.controller;

import com.eventify.dto.AuthResponse;
import com.eventify.dto.ErrorResponse;
import com.eventify.dto.EventResponseDto;
import com.eventify.dto.LoginRequest;
import com.eventify.dto.UserRegistrationDto;
import com.eventify.model.Event;
import com.eventify.model.User;
import com.eventify.security.TokenService;
import com.eventify.service.EventService;
import com.eventify.service.RegistrationService;
import com.eventify.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private static final Logger log = LoggerFactory.getLogger(PublicController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    public PublicController(AuthenticationManager authenticationManager,
                           UserService userService,
                           TokenService tokenService,
                           EventService eventService,
                           RegistrationService registrationService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenService = tokenService;
        this.eventService = eventService;
        this.registrationService = registrationService;
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
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        try {
            log.debug("Attempting to authenticate user: {}", loginRequest.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
            log.debug("Authentication successful for user: {}", loginRequest.getEmail());
            User user = userService.findByEmail(loginRequest.getEmail());
            if (user == null) {
                throw new UsernameNotFoundException("User not found after successful authentication");
            }
            log.debug("Retrieved user details for: {}", user.getEmail());

            String token = tokenService.generateToken(user);
            log.debug("Generated token for user: {}", user.getEmail());

            AuthResponse response = new AuthResponse(token, user.getEmail(), user.getRole());
            log.info("Login successful for user: {}", user.getEmail());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Bad credentials for user: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse(
                            LocalDateTime.now(),
                            401,
                            "Unauthorized",
                            "Invalid email or password",
                            "/api/public/login"
                    )
            );
        } catch (UsernameNotFoundException e) {
            log.warn("User not found: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ErrorResponse(
                            LocalDateTime.now(),
                            401,
                            "Unauthorized",
                            "Invalid email or password",
                            "/api/public/login"
                    )
            );
        } catch (Exception e) {
            log.error("Login error for user {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(
                            LocalDateTime.now(),
                            500,
                            "Internal Server Error",
                            "An error occurred during authentication: " + e.getMessage(),
                            "/api/public/login"
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