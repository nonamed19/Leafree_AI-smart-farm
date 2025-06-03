package com.leafree.backend.domain.device.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserDeviceId implements Serializable {

    private Long userId;
    private Long deviceId;

}
