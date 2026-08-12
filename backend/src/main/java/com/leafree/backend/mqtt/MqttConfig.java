package com.leafree.backend.mqtt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MqttConfig {

   @Value("${mqtt.broker-url}")
   private String brokerUrl;

   @Value("${mqtt.client-id}")
   private String clientId;

   @Value("${mqtt.username}")
   private String username;

   @Value("${mqtt.password}")
   private String password;

   @Value("${mqtt.topic-base}")
   private String topicBase;

   @Bean
   public MqttConnectOptions mqttConnectOptions() {
       MqttConnectOptions options = new MqttConnectOptions();
       options.setServerURIs(new String[]{brokerUrl});
       options.setCleanSession(true);
       options.setUserName(username);
       options.setPassword(password.toCharArray());
       options.setAutomaticReconnect(true);
       options.setConnectionTimeout(30);
       options.setKeepAliveInterval(60);
       return options;
   }

   @Bean
   public MqttClient mqttClient(MqttReceiver mqttReceiver) throws MqttException {
       String uniqueClientId = clientId+"-"+UUID.randomUUID();
       MqttClient mqttClient = new MqttClient(brokerUrl,uniqueClientId,new MemoryPersistence());

       mqttReceiver.setMqttClient(mqttClient);
       try {
           mqttClient.connect(mqttConnectOptions());
           mqttClient.subscribe(topicBase, 1);
       } catch (MqttException exception) {
           // The API must remain available while the broker is temporarily down.
           log.error("MQTT broker is unavailable at startup: {}", brokerUrl, exception);
       }

       return mqttClient;
   }



}
