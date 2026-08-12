package com.leafree.backend.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class MqttPublisher {

    private final MqttClient client;

    public MqttPublisher(MqttConnectOptions options,
                         @Value("${mqtt.broker-url}") String brokerUrl,
                         @Value("${mqtt.client-id}") String clientId) {
        try {
            this.client = new MqttClient(brokerUrl, clientId + "-" + UUID.randomUUID(), new MemoryPersistence());
            client.connect(options);
        } catch (MqttException e) {
            throw new RuntimeException("MQTT 연결 실패", e);
        }
    }

    public void publishMessage(Long deviceId, String content, String message) {

        String topic = "/leafree/"+deviceId+"/control/"+content;

        try {
            client.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void publishImageMessage(Long deviceId, byte[] image) {
        String topic = "/leafree/" + deviceId + "/control/crop_image";

        try {
            String base64Image = Base64.getEncoder().encodeToString(image);
            MqttMessage message = new MqttMessage(base64Image.getBytes()); // UTF-8 바이트로 변환
            message.setQos(1); // QoS 설정은 필요에 따라
            client.publish(topic, message);

            log.info("MQTT 이미지 전송 성공: topic={}, 크기={} bytes", topic, image.length);

        } catch (MqttException e) {
            log.error("MQTT 이미지 전송 실패: deviceId={}, topic={}", deviceId, topic, e);
            throw new RuntimeException("MQTT 이미지 전송 중 오류 발생", e);
        }
    }

}
