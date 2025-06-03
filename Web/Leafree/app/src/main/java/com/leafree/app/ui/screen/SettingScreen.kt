package com.leafree.app.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.leafree.app.ui.components.common.HeaderBar
import com.leafree.app.ui.components.common.SectionHeader
import com.leafree.app.ui.components.setting.SettingCard
import com.leafree.app.util.SharedPreferencesHelper
import com.leafree.app.viewModel.SettingViewModel


/**
 * 설정 화면 컴포저블
 * 스마트팜의 환경 제어 설정을 관리하는 화면
 *
 * @param navController 화면 전환을 위한 네비게이션 컨트롤러
 * @param notifications 알림 메시지 목록
 * @param viewModel 설정 데이터를 관리하는 ViewModel
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavController? = null,
    notifications: MutableList<String> = mutableStateListOf(),
    viewModel: SettingViewModel,
    prefs: SharedPreferencesHelper
) {
    val settingInfo by viewModel.settings.collectAsState()
    val expandedIndex = remember { mutableStateOf<Int?>(null) }
    val deviceId = 1L

    LaunchedEffect(Unit) {
        viewModel.fetchSettings(deviceId)
    }

    val nonNullSettingInfo = settingInfo ?: run {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    Scaffold(
        topBar = {
            HeaderBar(navController, notifications, prefs)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(
                    start = 30.dp,
                    end = 30.dp,
                    bottom = 30.dp
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionHeader(
                title = "환경 제어 설정"
            )

            val settingList = listOf(
                Triple("온도", "℃", nonNullSettingInfo.temperature.toFloat()),
                Triple("습도", "%", nonNullSettingInfo.humidity.toFloat()),
                Triple("조도", "lx", nonNullSettingInfo.light.toFloat()),
                Triple("CO₂", "ppm", nonNullSettingInfo.co2Ppm.toFloat()),
                Triple("토양 수분","%", nonNullSettingInfo.soilHumidity.toFloat()),
                Triple("점등 시간", "분", nonNullSettingInfo.lightDuration.toFloat()),
                Triple("급수 간격", "분", nonNullSettingInfo.wateringInterval.toFloat())
            )

            LazyColumn {
                itemsIndexed(settingList) { index, (type, unit, value) ->
                    SettingCard(
                        type = type,
                        unit = unit,
                        currentValue = value,
                        valueRange = when (unit) {
                            "℃" -> 10f..60f
                            "%" -> 0f..100f
                            "lx" -> 0f..20000f
                            "ppm" -> 0f..2000f
                            "분" -> 0f ..43200f
                            else -> 0f..100f
                        },
                        step = 1f,
                        expanded = expandedIndex.value == index,
                        onExpandToggle = { expandedIndex.value = if (expandedIndex.value == index) null else index },
                        onExpandClose = { expandedIndex.value = null },
                        onApply = { newValue ->
                            val updated = nonNullSettingInfo.copy(
                                temperature = if (index == 0) newValue.toDouble() else nonNullSettingInfo.temperature,
                                humidity = if (index == 1) newValue.toDouble() else nonNullSettingInfo.humidity,
                                light = if (index == 2) newValue.toDouble() else nonNullSettingInfo.light,
                                co2Ppm = if (index == 3) newValue.toDouble() else nonNullSettingInfo.co2Ppm,
                                soilHumidity = if (index == 4) newValue.toDouble() else nonNullSettingInfo.soilHumidity,
                                lightDuration = if (index == 5) newValue.toDouble() else nonNullSettingInfo.lightDuration,
                                wateringInterval = if (index == 6) newValue.toDouble() else nonNullSettingInfo.wateringInterval
                            )
                            viewModel.updateSetting(deviceId, updated)
                        }
                    )
                }
            }
        }
    }
}

