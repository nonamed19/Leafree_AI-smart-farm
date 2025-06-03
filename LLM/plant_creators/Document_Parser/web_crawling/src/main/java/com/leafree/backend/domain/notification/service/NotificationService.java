package com.leafree.backend.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.leafree.backend.domain.user.dto.FcmTokenDto;

import java.util.List;

public interface NotificationService {
    public void notifyUsersOfDevice(Long deviceId, String title, String body);
    public void sendMessage(String token, String title, String body) throws FirebaseMessagingException;
    public List<FcmTokenDto> getFcmTokenForDevice(Long deviceId);
}
