package com.eventify.repository;

import com.eventify.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizerId(Long organizerId);

    List<Event> findByDateTimeAfter(LocalDateTime dateTime);

    List<Event> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime dateTime);

    List<Event> findByOrganizerIdAndDateTimeAfter(Long organizerId, LocalDateTime dateTime);
}
