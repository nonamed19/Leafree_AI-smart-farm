package com.leafree.app.ui.components.setting

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leafree.app.ui.components.common.CommonOutlinedTextField
import com.leafree.app.util.formatMinutesToDayHourMinute

@Composable
fun SettingCard(
    type: String,
    unit: String,
    currentValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Float,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onExpandClose: () -> Unit,
    onApply: (Float) -> Unit
) {
    var sliderValue by remember { mutableStateOf(currentValue) }

    var days by remember { mutableStateOf((currentValue / 1440).toInt().toString()) }
    var hours by remember { mutableStateOf(((currentValue % 1440) / 60).toInt().toString()) }
    var minutes by remember { mutableStateOf((currentValue % 60).toInt().toString()) }

    LaunchedEffect(currentValue) {
        if (unit == "분") {
            days = (currentValue / 1440).toInt().toString()
            hours = ((currentValue % 1440) / 60).toInt().toString()
            minutes = (currentValue % 60).toInt().toString()
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 기준값 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.padding(5.dp))

                    Text(
                        text = if (unit == "분")
                            "기준값: ${formatMinutesToDayHourMinute(currentValue.toInt())}"
                        else
                            "기준값: ${currentValue} $unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(onClick = { onExpandToggle() }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "펼치기"
                    )
                }
            }

            // 펼쳤을 때의 콘텐츠
            if (expanded) {
                if (unit == "분") {
                    Spacer(modifier = Modifier.padding(8.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CommonOutlinedTextField(
                                value = days,
                                onValueChange = { if (it.all(Char::isDigit)) days = it },
                                label = "일",
                                modifier = Modifier.weight(1f)
                            )
                            CommonOutlinedTextField(
                                value = hours,
                                onValueChange = { if (it.all(Char::isDigit)) hours = it },
                                label = "시간",
                                modifier = Modifier.weight(1f)
                            )
                            CommonOutlinedTextField(
                                value = minutes,
                                onValueChange = { if (it.all(Char::isDigit)) minutes = it },
                                label = "분",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val totalMinutes = (days.toIntOrNull() ?: 0) * 1440 +
                                        (hours.toIntOrNull() ?: 0) * 60 +
                                        (minutes.toIntOrNull() ?: 0)
                                onApply(totalMinutes.toFloat())
                                onExpandClose()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("적용")
                        }
                    }
                } else {
                    SettingSliderContent(
                        value = sliderValue,
                        unit = unit,
                        valueRange = valueRange,
                        step = step,
                        onValueChange = { sliderValue = it },
                        onApply = {
                            onApply(sliderValue)
                            onExpandClose()
                        }
                    )
                }
            }
        }
    }
}
