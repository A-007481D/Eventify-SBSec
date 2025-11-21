package com.eventify.controller;

import com.eventify.dto.UserUpdateDto;
import com.eventify.model.Event;
import com.eventify.model.Registration;
import com.eventify.model.User;
import com.eventify.repository.EventRepository;
import com.eventify.repository.RegistrationRepository;
import com.eventify.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("user@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("ROLE_USER");
        testUser = userRepository.save(testUser);

        User organizer = new User();
        organizer.setName("Organizer");
        organizer.setEmail("organizer@example.com");
        organizer.setPassword(passwordEncoder.encode("password123"));
        organizer.setRole("ROLE_ORGANIZER");
        organizer = userRepository.save(organizer);

        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setLocation("Test Location");
        testEvent.setDateTime(LocalDateTime.now().plusDays(7));
        testEvent.setCapacity(100);
        testEvent.setOrganizerId(organizer.getId());
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    void getProfile_WithValidCredentials_ShouldReturnProfile() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .with(httpBasic("user@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getProfile_WithoutCredentials_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_WithValidData_ShouldUpdateProfile() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Updated Name");

        mockMvc.perform(put("/api/user/profile")
                        .with(httpBasic("user@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void registerForEvent_WithValidEvent_ShouldCreateRegistration() throws Exception {
        mockMvc.perform(post("/api/user/events/" + testEvent.getId() + "/register")
                        .with(httpBasic("user@example.com", "password123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.eventId").value(testEvent.getId()));
    }

    @Test
    void registerForEvent_WhenAlreadyRegistered_ShouldReturnConflict() throws Exception {
        // First registration
        Registration registration = new Registration();
        registration.setUserId(testUser.getId());
        registration.setEventId(testEvent.getId());
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setStatus("CONFIRMED");
        registrationRepository.save(registration);

        // Try to register again
        mockMvc.perform(post("/api/user/events/" + testEvent.getId() + "/register")
                        .with(httpBasic("user@example.com", "password123")))
                .andExpect(status().isConflict());
    }

    @Test
    void registerForEvent_WhenEventNotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(post("/api/user/events/99999/register")
                        .with(httpBasic("user@example.com", "password123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerForEvent_WhenEventAtCapacity_ShouldReturnBadRequest() throws Exception {
        testEvent.setCapacity(0);
        eventRepository.save(testEvent);

        mockMvc.perform(post("/api/user/events/" + testEvent.getId() + "/register")
                        .with(httpBasic("user@example.com", "password123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserRegistrations_ShouldReturnUserRegistrations() throws Exception {
        Registration registration = new Registration();
        registration.setUserId(testUser.getId());
        registration.setEventId(testEvent.getId());
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setStatus("CONFIRMED");
        registrationRepository.save(registration);

        mockMvc.perform(get("/api/user/registrations")
                        .with(httpBasic("user@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value(testEvent.getId()))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void getUserRegistrations_WithNoRegistrations_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/user/registrations")
                        .with(httpBasic("user@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
