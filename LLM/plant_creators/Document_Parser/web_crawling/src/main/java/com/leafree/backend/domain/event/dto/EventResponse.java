package com.leafree.backend.domain.event.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leafree.backend.domain.event.entity.Event;
import com.leafree.backend.domain.event.enums.EventContentCode;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Data
@Builder
public class EventResponse {
    String eventType;
    EventContentCode code;
    LocalDateTime occurredAt;
    private Map<String,Object> eventData;


    public static EventResponse fromEntity(Event event, ObjectMapper objectMapper) {
        Map<String, Object> parsedEventData = null;
        try {
            parsedEventData = objectMapper.readValue(event.getEventData(), Map.class);
        } catch (Exception e) {
            log.error("eventData 파싱 실패", e);
        }

        return EventResponse.builder()
                .eventType(event.getEventType())
                .code(event.getEventContentCode())
                .occurredAt(event.getOccurredAt())
                .eventData(parsedEventData)
                .build();
    }

}
