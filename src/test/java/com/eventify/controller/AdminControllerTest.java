package com.eventify.controller;

import com.eventify.model.Event;
import com.eventify.model.User;
import com.eventify.repository.EventRepository;
import com.eventify.repository.RegistrationRepository;
import com.eventify.repository.UserRepository;
import com.eventify.security.TokenService;
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

    @Autowired
    private TokenService tokenService;

    private User admin;
    private User regularUser;
    private Event testEvent;
    private String adminToken;
    private String userToken;

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
        adminToken = tokenService.generateToken(admin);

        regularUser = new User();
        regularUser.setName("Regular User");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword(passwordEncoder.encode("password123"));
        regularUser.setRole("ROLE_USER");
        regularUser = userRepository.save(regularUser);
        userToken = tokenService.generateToken(regularUser);

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
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllUsers_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserRole_AsAdmin_ShouldUpdateRole() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("role", "ROLE_ORGANIZER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ORGANIZER"));
    }

    @Test
    void updateUserRole_WithoutRolePrefix_ShouldAddPrefix() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void updateUserRole_WithInvalidRole_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("role", "INVALID_ROLE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserRole_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/admin/users/" + regularUser.getId() + "/role")
                        .header("Authorization", "Bearer " + userToken)
                        .param("role", "ROLE_ORGANIZER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_AsAdmin_ShouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + regularUser.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEvent_AsAdmin_ShouldDeleteEvent() throws Exception {
        mockMvc.perform(delete("/api/admin/events/" + testEvent.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEvent_AsNonAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/events/" + testEvent.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEvent_WhenNotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/events/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
