package com.leafree.app.ui.components.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt


/**
 * 일반적인 값 조정용 슬라이더 UI
 *
 *  - value: Float
 *  - onValueChange: (Float) -> Unit
 *  - onApply: () -> Unit
 *  - unit: String
 *  - valueRange: ClosedFloatingPointRange<Float>
 *  - step: Float
 *
 */


@Composable
fun SettingSliderContent(
    value: Float,
    unit: String,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Float = 1f,
    onValueChange: (Float) -> Unit,
    onApply: () -> Unit
) {
    val steps = ((valueRange.endInclusive - valueRange.start) / step).roundToInt() - 1

    Slider(
        value = value,
        onValueChange = { onValueChange(it.roundToInt().toFloat()) },
        valueRange = valueRange,
        steps = steps
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            text = "현재 선택 : ${value.toInt()} $unit"
        )
        Button(onClick = onApply){
            Text("적용")
        }
    }
}
