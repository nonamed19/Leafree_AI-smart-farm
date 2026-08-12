package com.leafree.backend.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.leafree.backend.domain.device.repository.UserDeviceRepository;
import com.leafree.backend.domain.user.dto.FcmTokenDto;
import com.leafree.backend.domain.user.entity.FcmToken;
import com.leafree.backend.domain.user.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final FcmTokenRepository fcmTokenRepository;
    private final UserDeviceRepository userDeviceRepository;

    /**
     * deviceId에 연결된 모든 사용자들의 FCM 토큰을 조회하고 알림 전송
     */
    @Override
    public void notifyUsersOfDevice(Long deviceId, String title, String body) {
        List<FcmTokenDto> tokens = getFcmTokenForDevice(deviceId);

        for (FcmTokenDto token : tokens) {
            try {
                sendMessage(token.getFcmToken(), title, body);
            } catch (FirebaseMessagingException e) {
                log.error("🔥 FCM 전송 실패: " + e.getMessage());
            }
        }
    }

    /**
     * FCM 메시지 전송 (단일 토큰 대상)
     */
    public void sendMessage(String token, String title, String body) throws FirebaseMessagingException {
        String messageId = FirebaseMessaging.getInstance().send(
                Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setToken(token)
                        .build()
        );
        log.info("✅ FCM 메시지 전송 성공: " + messageId);
    }

    @Override
    public List<FcmTokenDto> getFcmTokenForDevice(Long deviceId) {

        List<Long> userIds = userDeviceRepository.findUserIdsByDeviceId(deviceId);
        List<FcmToken> tokens = fcmTokenRepository.findByUserIdIn(userIds);

        return tokens.stream()
                .map(token -> FcmTokenDto.builder()
                        .fcmToken(token.getFcmToken())
                        .build())
                .collect(Collectors.toList());
    }
}
