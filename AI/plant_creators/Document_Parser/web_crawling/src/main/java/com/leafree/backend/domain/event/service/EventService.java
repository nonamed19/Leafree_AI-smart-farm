package com.leafree.backend.domain.event.service;

import com.leafree.backend.domain.event.dto.EventResponse;
import com.leafree.backend.domain.event.enums.EventContentCode;

import java.time.LocalDate;
import java.util.List;

public interface EventService {

    public void logActuatingEvent(Long deviceId, EventContentCode code, Object from, Object to);
    public void logWarningEvent(Long deviceId, EventContentCode code, Object value);
    public void logNotifyEvent(Long deviceId, EventContentCode code, String cropType);

    List<EventResponse> getEvent(Long deviceId, LocalDate date);
}
