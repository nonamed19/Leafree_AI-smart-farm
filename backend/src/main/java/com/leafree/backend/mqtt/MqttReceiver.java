package com.leafree.backend.mqtt;

import com.leafree.backend.domain.crop.dto.AddCropRequest;
import com.leafree.backend.domain.crop.service.CropService;
import com.leafree.backend.domain.sensor.service.SensorBufferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttReceiver implements MqttCallback {

    private MqttClient mqttClient;
    private final SensorBufferService sensorBufferService;
    private final CropService cropService;

    public void setMqttClient(final MqttClient mqttClient) {
        this.mqttClient = mqttClient;
        this.mqttClient.setCallback(this);
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error("MQTT Connection lost: {}", cause.getMessage(), cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String payload = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8).trim();
        log.debug("Message arrived : topic : {}", topic);

        String[] topicParts = topic.split("/");
        if (topicParts.length != 5) {
            log.error("Invalid topic format: {}", topic);
            return;
        }

        Long deviceId;
        try {
            deviceId = Long.parseLong(topicParts[2]);
        } catch (NumberFormatException exception) {
            log.warn("Invalid device id in MQTT topic: {}", topic);
            return;
        }
        String type = topicParts[3];
        String content = topicParts[4];

        if ("sensor".equals(type)) {
            try {
                float value = Float.parseFloat(payload);
                sensorBufferService.bufferSensorData(deviceId, content, value);
            } catch (NumberFormatException e) {
                log.error("Invalid sensor value format: {}", payload);
            }
        }
        else if ("event".equals(type) && "sown".equals(content)) {

            String cropType = payload;

            AddCropRequest request = AddCropRequest.builder()
                    .deviceId(deviceId)
                    .cropType(cropType)
                    .build();

            try {
                cropService.addCrop(request);
            } catch (Exception e) {
                log.error("작물 등록 중 예외 발생 (MQTT 연결 유지됨): deviceId={}, cropType={}", deviceId, cropType, e);
            }

        }
        else {
            return;
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("Delivery completed");
    }
}
