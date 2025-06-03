package com.leafree.backend.domain.device.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(UserDeviceId.class)
@EntityListeners(AuditingEntityListener.class)
public class UserDevice {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "device_id")
    private Long deviceId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
