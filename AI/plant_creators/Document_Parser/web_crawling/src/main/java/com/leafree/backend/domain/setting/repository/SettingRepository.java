package com.leafree.backend.domain.setting.repository;

import com.leafree.backend.domain.setting.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findByDeviceId(Long deviceId);

}
