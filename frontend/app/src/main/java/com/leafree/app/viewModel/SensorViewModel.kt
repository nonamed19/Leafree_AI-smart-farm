package com.leafree.app.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leafree.app.model.SensorInfo
import com.leafree.app.network.MqttClientManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SensorViewModel(
    private val context: Context
) : ViewModel() {

    private val deviceId = 1L

    // ✅ 더미 센서 데이터로 초기화
    private val _sensorList = MutableStateFlow<List<SensorInfo>>(
        listOf(
            SensorInfo("온도", "-", "℃"),
            SensorInfo("습도", "-", "%"),
            SensorInfo("조도", "-", "lx"),
            SensorInfo("CO₂", "-", "ppm"),
            SensorInfo("토양 수분", "-", "%"),
            SensorInfo("펌프 수위", "-", "%")
        )
    )
    val sensorList: StateFlow<List<SensorInfo>> = _sensorList.asStateFlow()

    private val mqttClient = MqttClientManager(
        context = context,
        topic = "/leafree/$deviceId/sensor/#"
    )

    init {
        connectMqtt()
    }

    private fun connectMqtt() {
        mqttClient.connectAndSubscribe { message, topic ->
            viewModelScope.launch {
                val value = message.trim().toFloatOrNull() ?: return@launch
                if (!value.isFinite()) return@launch
                val updated = _sensorList.value.toMutableList()

                when {
                    topic.endsWith("temperature") -> updated[0] = updated[0].copy(value = value.toString())
                    topic.endsWith("humidity") -> updated[1] = updated[1].copy(value = value.toString())
                    topic.endsWith("light") -> updated[2] = updated[2].copy(value = value.toString())
                    topic.endsWith("co2_ppm") -> updated[3] = updated[3].copy(value = value.toString())
                    topic.endsWith("soil_moisture") -> updated[4] = updated[4].copy(value = value.toString())
                    topic.endsWith("water_level") -> updated[5] = updated[5].copy(value = value.toString())
                }

                if (updated != _sensorList.value) _sensorList.value = updated
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mqttClient.disconnect()
    }

}
