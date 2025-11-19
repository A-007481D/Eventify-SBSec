package com.eventify.dto;

import com.eventify.model.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime dateTime;
    private Integer capacity;
    private Long organizerId;
    private Long availableSpots;

    public static EventResponseDto fromEvent(Event event) {
        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setLocation(event.getLocation());
        dto.setDateTime(event.getDateTime());
        dto.setCapacity(event.getCapacity());
        dto.setOrganizerId(event.getOrganizerId());
        return dto;
    }

    public static EventResponseDto fromEvent(Event event, Long registrationCount) {
        EventResponseDto dto = fromEvent(event);
        if (event.getCapacity() != null) {
            dto.setAvailableSpots(event.getCapacity() - registrationCount);
        }
        return dto;
    }
}
