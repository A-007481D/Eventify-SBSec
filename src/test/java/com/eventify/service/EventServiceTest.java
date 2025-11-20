package com.eventify.service;

import com.eventify.model.Event;
import com.eventify.repository.EventRepository;
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
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setLocation("Test Location");
        testEvent.setDateTime(LocalDateTime.now().plusDays(7));
        testEvent.setCapacity(100);
        testEvent.setOrganizerId(1L);
    }

    @Test
    void createEvent_ShouldSaveAndReturnEvent() {
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event result = eventService.createEvent(testEvent);

        assertNotNull(result);
        assertEquals("Test Event", result.getTitle());
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    void getEventById_WhenExists_ShouldReturnEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        Optional<Event> result = eventService.getEventById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Event", result.get().getTitle());
    }

    @Test
    void getEventById_WhenNotExists_ShouldReturnEmpty() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Event> result = eventService.getEventById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAllEvents_ShouldReturnAllEvents() {
        Event event2 = new Event();
        event2.setId(2L);
        event2.setTitle("Event 2");

        when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent, event2));

        List<Event> result = eventService.getAllEvents();

        assertEquals(2, result.size());
    }

    @Test
    void getUpcomingEvents_ShouldReturnFutureEvents() {
        when(eventRepository.findByDateTimeAfterOrderByDateTimeAsc(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testEvent));

        List<Event> result = eventService.getUpcomingEvents();

        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
    }

    @Test
    void getEventsByOrganizerId_ShouldReturnOrganizerEvents() {
        when(eventRepository.findByOrganizerId(1L)).thenReturn(Arrays.asList(testEvent));

        List<Event> result = eventService.getEventsByOrganizerId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getOrganizerId());
    }

    @Test
    void updateEvent_ShouldSaveAndReturnUpdatedEvent() {
        testEvent.setTitle("Updated Title");
        when(eventRepository.save(testEvent)).thenReturn(testEvent);

        Event result = eventService.updateEvent(testEvent);

        assertEquals("Updated Title", result.getTitle());
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    void deleteEvent_ShouldCallDeleteById() {
        doNothing().when(eventRepository).deleteById(1L);

        eventService.deleteEvent(1L);

        verify(eventRepository, times(1)).deleteById(1L);
    }

    @Test
    void existsById_WhenExists_ShouldReturnTrue() {
        when(eventRepository.existsById(1L)).thenReturn(true);

        boolean result = eventService.existsById(1L);

        assertTrue(result);
    }

    @Test
    void existsById_WhenNotExists_ShouldReturnFalse() {
        when(eventRepository.existsById(99L)).thenReturn(false);

        boolean result = eventService.existsById(99L);

        assertFalse(result);
    }

    @Test
    void isEventOwner_WhenOwner_ShouldReturnTrue() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        boolean result = eventService.isEventOwner(1L, 1L);

        assertTrue(result);
    }

    @Test
    void isEventOwner_WhenNotOwner_ShouldReturnFalse() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        boolean result = eventService.isEventOwner(1L, 99L);

        assertFalse(result);
    }

    @Test
    void isEventOwner_WhenEventNotFound_ShouldReturnFalse() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = eventService.isEventOwner(99L, 1L);

        assertFalse(result);
    }
}
