package com.eventify.controller;

import com.eventify.dto.EventDto;
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
class OrganizerControllerTest {

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

    private User organizer;
    private User otherOrganizer;
    private Event testEvent;
    private String organizerToken;
    private String otherOrganizerToken;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        organizer = new User();
        organizer.setName("Organizer");
        organizer.setEmail("organizer@example.com");
        organizer.setPassword(passwordEncoder.encode("password123"));
        organizer.setRole("ROLE_ORGANIZER");
        organizer = userRepository.save(organizer);
        organizerToken = tokenService.generateToken(organizer);

        otherOrganizer = new User();
        otherOrganizer.setName("Other Organizer");
        otherOrganizer.setEmail("other@example.com");
        otherOrganizer.setPassword(passwordEncoder.encode("password123"));
        otherOrganizer.setRole("ROLE_ORGANIZER");
        otherOrganizer = userRepository.save(otherOrganizer);
        otherOrganizerToken = tokenService.generateToken(otherOrganizer);

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
    void getOrganizerEvents_ShouldReturnOrganizerEvents() throws Exception {
        mockMvc.perform(get("/api/organizer/events")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Event"));
    }

    @Test
    void getOrganizerEvents_WithNoEvents_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/organizer/events")
                        .header("Authorization", "Bearer " + otherOrganizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createEvent_WithValidData_ShouldCreateEvent() throws Exception {
        EventDto eventDto = new EventDto();
        eventDto.setTitle("New Event");
        eventDto.setDescription("New Description");
        eventDto.setLocation("New Location");
        eventDto.setDateTime(LocalDateTime.now().plusDays(14));
        eventDto.setCapacity(50);

        mockMvc.perform(post("/api/organizer/events")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Event"))
                .andExpect(jsonPath("$.organizerId").value(organizer.getId()));
    }

    @Test
    void createEvent_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        EventDto eventDto = new EventDto();
        eventDto.setTitle(""); // Invalid: empty title
        eventDto.setLocation("Location");
        eventDto.setDateTime(LocalDateTime.now().plusDays(7));
        eventDto.setCapacity(50);

        mockMvc.perform(post("/api/organizer/events")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_WithPastDate_ShouldReturnBadRequest() throws Exception {
        EventDto eventDto = new EventDto();
        eventDto.setTitle("Past Event");
        eventDto.setLocation("Location");
        eventDto.setDateTime(LocalDateTime.now().minusDays(1));
        eventDto.setCapacity(50);

        mockMvc.perform(post("/api/organizer/events")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEvent_AsOwner_ShouldUpdateEvent() throws Exception {
        EventDto eventDto = new EventDto();
        eventDto.setTitle("Updated Event");
        eventDto.setDescription("Updated Description");
        eventDto.setLocation("Updated Location");
        eventDto.setDateTime(LocalDateTime.now().plusDays(14));
        eventDto.setCapacity(200);

        mockMvc.perform(put("/api/organizer/events/" + testEvent.getId())
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event"))
                .andExpect(jsonPath("$.capacity").value(200));
    }

    @Test
    void updateEvent_AsNonOwner_ShouldReturnForbidden() throws Exception {
        EventDto eventDto = new EventDto();
        eventDto.setTitle("Updated Event");
        eventDto.setLocation("Updated Location");
        eventDto.setDateTime(LocalDateTime.now().plusDays(14));
        eventDto.setCapacity(200);

        mockMvc.perform(put("/api/organizer/events/" + testEvent.getId())
                        .header("Authorization", "Bearer " + otherOrganizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateEvent_WhenNotFound_ShouldReturnNotFound() throws Exception {
        EventDto eventDto = new EventDto();
        eventDto.setTitle("Updated Event");
        eventDto.setLocation("Updated Location");
        eventDto.setDateTime(LocalDateTime.now().plusDays(14));
        eventDto.setCapacity(200);

        mockMvc.perform(put("/api/organizer/events/99999")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEvent_AsOwner_ShouldDeleteEvent() throws Exception {
        mockMvc.perform(delete("/api/organizer/events/" + testEvent.getId())
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEvent_AsNonOwner_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/organizer/events/" + testEvent.getId())
                        .header("Authorization", "Bearer " + otherOrganizerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEvent_WhenNotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/organizer/events/99999")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrganizerProfile_ShouldReturnProfile() throws Exception {
        mockMvc.perform(get("/api/organizer/profile")
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Organizer"))
                .andExpect(jsonPath("$.email").value("organizer@example.com"));
    }
}
