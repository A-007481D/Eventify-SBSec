package com.eventify.controller;

import com.eventify.dto.LoginRequest;
import com.eventify.dto.UserRegistrationDto;
import com.eventify.model.Event;
import com.eventify.model.User;
import com.eventify.repository.EventRepository;
import com.eventify.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        eventRepository.deleteAll();

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("ROLE_USER");
        testUser = userRepository.save(testUser);
    }

    @Test
    void registerUser_WithValidData_ShouldReturnCreated() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("New User");
        dto.setEmail("new@example.com");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/public/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Duplicate User");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/public/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Invalid Email User");
        dto.setEmail("invalid-email");
        dto.setPassword("password123");

        mockMvc.perform(post("/api/public/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Short Password User");
        dto.setEmail("short@example.com");
        dto.setPassword("12345");

        mockMvc.perform(post("/api/public/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");  // User doesn't exist
        loginRequest.setPassword("anypassword");

        mockMvc.perform(post("/api/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPublicEvents_ShouldReturnEventsList() throws Exception {
        Event event = new Event();
        event.setTitle("Public Event");
        event.setDescription("Test Description");
        event.setLocation("Test Location");
        event.setDateTime(LocalDateTime.now().plusDays(7));
        event.setCapacity(100);
        event.setOrganizerId(testUser.getId());
        eventRepository.save(event);

        mockMvc.perform(get("/api/public/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Public Event"));
    }

    @Test
    void getPublicEvents_WithNoEvents_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/public/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
