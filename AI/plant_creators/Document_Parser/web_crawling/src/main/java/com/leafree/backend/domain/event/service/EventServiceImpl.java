package com.leafree.backend.domain.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leafree.backend.domain.event.dto.EventResponse;
import com.leafree.backend.domain.event.entity.Event;
import com.leafree.backend.domain.event.enums.EventContentCode;
import com.leafree.backend.domain.event.repository.EventRepository;
import com.leafree.backend.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Override
    public void logActuatingEvent(Long deviceId, EventContentCode code, Object from, Object to) {
        Map<String, Object> data = Map.of("from", from, "to", to);
        saveEvent(deviceId, "ACTUATING", code, data);
    }

    @Override
    public void logWarningEvent(Long deviceId, EventContentCode code, Object value) {
        Map<String, Object> data = Map.of("value", value);
        saveEvent(deviceId, "WARNING", code, data);

        String message = code.format(data);
        notificationService.notifyUsersOfDevice(deviceId,"Leafree",message);

    }

    @Override
    public void logNotifyEvent(Long deviceId, EventContentCode code, String cropType) {
        Map<String, Object> data = Map.of("cropType", cropType);
        saveEvent(deviceId, "NOTIFY", code, data);

        String message = code.format(data);
        notificationService.notifyUsersOfDevice(deviceId,"Leafree",message);
    }


    @Override
    public List<EventResponse> getEvent(Long deviceId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        return eventRepository.findByDeviceIdAndDateRange(deviceId, start, end)
                .stream()
                .<EventResponse>map(event -> (EventResponse) EventResponse.fromEntity(event, objectMapper))
                .collect(Collectors.toList());
    }


    private void saveEvent(Long deviceId, String eventType, EventContentCode code, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            Event event = Event.builder()
                    .deviceId(deviceId)
                    .eventType(eventType)
                    .eventContentCode(code)
                    .eventData(json)
                    .occurredAt(LocalDateTime.now())
                    .build();
            eventRepository.save(event);
            log.info("✅ Event saved: type={}, code={}, deviceId={}", eventType, code, deviceId);
        } catch (Exception e) {
            log.error("❌ 이벤트 저장 실패: type={}, code={}, deviceId={}", eventType, code, deviceId, e);
        }
    }
}
