package com.eventify.controller;

import com.eventify.model.Event;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
class AdminControllerTest {

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

    private User admin;
    private User regularUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRole("ROLE_ADMIN");
        admin = userRepository.save(admin);

        regularUser = new User();
        regularUser.setName("Regular User");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword(passwordEncoder.encode("password123"));
        regularUser.setRole("ROLE_USER");
        regularUser = userRepository.save(regularUser);

        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setLocation("Test Location");
        testEvent.setDateTime(LocalDateTime.now().plusDays(7));
        testEvent.setCapacity(100);
        testEvent.setOrganizerId(admin.getId());
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    void getAllUsers_AsAdmin_ShouldReturnAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(httpBasic("admin@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllUsers_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(httpBasic("user@example.com", "password123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserRole_AsAdmin_ShouldUpdateRole() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/role")
                        .with(httpBasic("admin@example.com", "password123"))
                        .param("role", "ROLE_ORGANIZER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ORGANIZER"));
    }

    @Test
    void updateUserRole_WithoutRolePrefix_ShouldAddPrefix() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/role")
                        .with(httpBasic("admin@example.com", "password123"))
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void updateUserRole_WithInvalidRole_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/role")
                        .with(httpBasic("admin@example.com", "password123"))
                        .param("role", "INVALID_ROLE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserRole_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/role")
                        .with(httpBasic("user@example.com", "password123"))
                        .param("role", "ROLE_ORGANIZER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_AsAdmin_ShouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + regularUser.getId())
                        .with(httpBasic("admin@example.com", "password123")))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + regularUser.getId())
                        .with(httpBasic("user@example.com", "password123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEvent_AsAdmin_ShouldDeleteEvent() throws Exception {
        mockMvc.perform(delete("/api/admin/events/" + testEvent.getId())
                        .with(httpBasic("admin@example.com", "password123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEvent_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/events/" + testEvent.getId())
                        .with(httpBasic("user@example.com", "password123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEvent_WhenNotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/events/99999")
                        .with(httpBasic("admin@example.com", "password123")))
                .andExpect(status().isNotFound());
    }
}
