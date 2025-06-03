package com.leafree.app.network

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.android.service.MqttAndroidClient.Ack
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttClientManager(
    private val context: Context,
    private val brokerUrl: String = "tcp://leafree.kr:1883",
    private val clientId: String = MqttClient.generateClientId(),
    private val username: String = "android_user",
    private val user_password: String = "leafree",
    private val topic: String = "/leafree/#"
    ) {

    private val mqttClient = MqttAndroidClient(context, brokerUrl, clientId, Ack.AUTO_ACK)

    fun connectAndSubscribe(onMessage: (message: String, topic: String) -> Unit) {
        val options = MqttConnectOptions().apply {
            isCleanSession = true
            setUserName(username)
            password = user_password.toCharArray()
            isAutomaticReconnect = true
            connectionTimeout = 30
            keepAliveInterval = 60
        }

        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                if (topic != null && message != null) {
                    onMessage(message.toString(), topic)
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Log.e("MQTT", "연결 끊김: ${cause?.message}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MQTT", "연결 성공")
                mqttClient.subscribe(topic, 1)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "연결 실패: ${exception?.message}")
            }
        })
    }


    fun disconnect() {
        mqttClient.disconnect()
    }

}