package com.leafree.backend.domain.device.repository;

import com.leafree.backend.domain.device.dto.UserDeviceResponse;
import com.leafree.backend.domain.device.entity.UserDevice;
import com.leafree.backend.domain.device.entity.UserDeviceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDeviceRepository extends JpaRepository<UserDevice, UserDeviceId> {
    List<UserDeviceResponse> findByUserId(Long userId);

    @Query("SELECT ud.userId FROM UserDevice ud WHERE ud.deviceId = :deviceId")
    List<Long> findUserIdsByDeviceId(@Param("deviceId") Long deviceId);


}
