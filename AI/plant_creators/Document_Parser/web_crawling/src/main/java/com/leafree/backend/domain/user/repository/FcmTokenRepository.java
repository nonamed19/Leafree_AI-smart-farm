package com.leafree.backend.domain.user.repository;

import com.leafree.backend.domain.user.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findByUserIdIn(List<Long> userIds);
}
