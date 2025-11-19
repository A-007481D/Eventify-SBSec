package com.eventify.dto;

import com.eventify.model.Registration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponseDto {

    private Long id;
    private Long userId;
    private Long eventId;
    private LocalDateTime registeredAt;
    private String status;
    private EventResponseDto event;

    public static RegistrationResponseDto fromRegistration(Registration registration) {
        RegistrationResponseDto dto = new RegistrationResponseDto();
        dto.setId(registration.getId());
        dto.setUserId(registration.getUserId());
        dto.setEventId(registration.getEventId());
        dto.setRegisteredAt(registration.getRegisteredAt());
        dto.setStatus(registration.getStatus());
        return dto;
    }

    public static RegistrationResponseDto fromRegistration(Registration registration, EventResponseDto eventDto) {
        RegistrationResponseDto dto = fromRegistration(registration);
        dto.setEvent(eventDto);
        return dto;
    }
}
