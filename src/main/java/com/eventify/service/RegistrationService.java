package com.eventify.service;

import com.eventify.model.Event;
import com.eventify.model.Registration;
import com.eventify.repository.EventRepository;
import com.eventify.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;

    @Autowired
    public RegistrationService(RegistrationRepository registrationRepository, EventRepository eventRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
    }

    public Registration registerUserForEvent(Long userId, Long eventId) {
        Registration registration = new Registration();
        registration.setUserId(userId);
        registration.setEventId(eventId);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setStatus("CONFIRMED");
        return registrationRepository.save(registration);
    }

    public List<Registration> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserId(userId);
    }

    public List<Registration> getEventRegistrations(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    public boolean isUserRegisteredForEvent(Long userId, Long eventId) {
        return registrationRepository.existsByUserIdAndEventId(userId, eventId);
    }

    public Optional<Registration> getRegistration(Long userId, Long eventId) {
        return registrationRepository.findByUserIdAndEventId(userId, eventId);
    }

    public void cancelRegistration(Long registrationId) {
        registrationRepository.deleteById(registrationId);
    }

    public void cancelRegistrationByUserAndEvent(Long userId, Long eventId) {
        registrationRepository.findByUserIdAndEventId(userId, eventId)
                .ifPresent(registration -> registrationRepository.delete(registration));
    }

    public long getRegistrationCount(Long eventId) {
        return registrationRepository.countByEventId(eventId);
    }

    public boolean isEventAtCapacity(Long eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return true;
        }
        Event event = eventOpt.get();
        if (event.getCapacity() == null) {
            return false;
        }
        long currentRegistrations = registrationRepository.countByEventId(eventId);
        return currentRegistrations >= event.getCapacity();
    }

    public void deleteRegistrationsByEventId(Long eventId) {
        registrationRepository.deleteByEventId(eventId);
    }
}
