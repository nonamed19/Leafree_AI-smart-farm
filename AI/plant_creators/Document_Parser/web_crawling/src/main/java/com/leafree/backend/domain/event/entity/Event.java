package com.leafree.backend.domain.event.entity;

import com.leafree.backend.domain.event.enums.EventContentCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "event_type", nullable = false)
    private String eventType; // 예: ACTUATING, WARNING, ERROR, NOTIFY

    @Enumerated(EnumType.STRING)
    @Column(name = "event_content_code", nullable = false, length = 50)
    private EventContentCode eventContentCode;

    @Lob
    @Column(name = "event_data", columnDefinition = "JSON")
    private String eventData;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
