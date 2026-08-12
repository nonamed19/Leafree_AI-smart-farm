package com.leafree.backend.domain.crop.entity;

import com.leafree.backend.domain.crop.enums.CropStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "crops")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Crop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crop_id")
    private Long id;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "crop_type", nullable = false)
    private String cropType;

    @Column(name = "crop_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CropStatus status = CropStatus.GROWING;

    @Column(name = "crop_image")
    private byte[] cropImage;

    @Column(name = "harvested_at")
    private LocalDate harvestedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void deleteCrop(){
        this.status = CropStatus.DEAD;
    }
}
