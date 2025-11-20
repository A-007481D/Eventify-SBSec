package com.eventify.service;

import com.eventify.model.Event;
import com.eventify.model.Registration;
import com.eventify.repository.EventRepository;
import com.eventify.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private RegistrationService registrationService;

    private Registration testRegistration;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testRegistration = new Registration();
        testRegistration.setId(1L);
        testRegistration.setUserId(1L);
        testRegistration.setEventId(1L);
        testRegistration.setRegisteredAt(LocalDateTime.now());
        testRegistration.setStatus("CONFIRMED");

        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setCapacity(100);
        testEvent.setOrganizerId(1L);
    }

    @Test
    void registerUserForEvent_ShouldCreateRegistration() {
        when(registrationRepository.save(any(Registration.class))).thenReturn(testRegistration);

        Registration result = registrationService.registerUserForEvent(1L, 1L);

        assertNotNull(result);
        assertEquals("CONFIRMED", result.getStatus());
        verify(registrationRepository, times(1)).save(any(Registration.class));
    }

    @Test
    void getUserRegistrations_ShouldReturnUserRegistrations() {
        when(registrationRepository.findByUserId(1L)).thenReturn(Arrays.asList(testRegistration));

        List<Registration> result = registrationService.getUserRegistrations(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    @Test
    void getEventRegistrations_ShouldReturnEventRegistrations() {
        when(registrationRepository.findByEventId(1L)).thenReturn(Arrays.asList(testRegistration));

        List<Registration> result = registrationService.getEventRegistrations(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getEventId());
    }

    @Test
    void isUserRegisteredForEvent_WhenRegistered_ShouldReturnTrue() {
        when(registrationRepository.existsByUserIdAndEventId(1L, 1L)).thenReturn(true);

        boolean result = registrationService.isUserRegisteredForEvent(1L, 1L);

        assertTrue(result);
    }

    @Test
    void isUserRegisteredForEvent_WhenNotRegistered_ShouldReturnFalse() {
        when(registrationRepository.existsByUserIdAndEventId(1L, 2L)).thenReturn(false);

        boolean result = registrationService.isUserRegisteredForEvent(1L, 2L);

        assertFalse(result);
    }

    @Test
    void getRegistration_WhenExists_ShouldReturnRegistration() {
        when(registrationRepository.findByUserIdAndEventId(1L, 1L)).thenReturn(Optional.of(testRegistration));

        Optional<Registration> result = registrationService.getRegistration(1L, 1L);

        assertTrue(result.isPresent());
    }

    @Test
    void getRegistration_WhenNotExists_ShouldReturnEmpty() {
        when(registrationRepository.findByUserIdAndEventId(1L, 99L)).thenReturn(Optional.empty());

        Optional<Registration> result = registrationService.getRegistration(1L, 99L);

        assertFalse(result.isPresent());
    }

    @Test
    void cancelRegistration_ShouldCallDeleteById() {
        doNothing().when(registrationRepository).deleteById(1L);

        registrationService.cancelRegistration(1L);

        verify(registrationRepository, times(1)).deleteById(1L);
    }

    @Test
    void getRegistrationCount_ShouldReturnCount() {
        when(registrationRepository.countByEventId(1L)).thenReturn(50L);

        long result = registrationService.getRegistrationCount(1L);

        assertEquals(50L, result);
    }

    @Test
    void isEventAtCapacity_WhenAtCapacity_ShouldReturnTrue() {
        testEvent.setCapacity(50);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(registrationRepository.countByEventId(1L)).thenReturn(50L);

        boolean result = registrationService.isEventAtCapacity(1L);

        assertTrue(result);
    }

    @Test
    void isEventAtCapacity_WhenNotAtCapacity_ShouldReturnFalse() {
        testEvent.setCapacity(100);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(registrationRepository.countByEventId(1L)).thenReturn(50L);

        boolean result = registrationService.isEventAtCapacity(1L);

        assertFalse(result);
    }

    @Test
    void isEventAtCapacity_WhenEventNotFound_ShouldReturnTrue() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = registrationService.isEventAtCapacity(99L);

        assertTrue(result);
    }

    @Test
    void isEventAtCapacity_WhenCapacityNull_ShouldReturnFalse() {
        testEvent.setCapacity(null);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        boolean result = registrationService.isEventAtCapacity(1L);

        assertFalse(result);
    }

    @Test
    void deleteRegistrationsByEventId_ShouldCallDeleteByEventId() {
        doNothing().when(registrationRepository).deleteByEventId(1L);

        registrationService.deleteRegistrationsByEventId(1L);

        verify(registrationRepository, times(1)).deleteByEventId(1L);
    }
}
