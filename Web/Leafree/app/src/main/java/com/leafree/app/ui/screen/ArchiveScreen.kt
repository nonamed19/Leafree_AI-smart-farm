package com.leafree.app.ui.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.leafree.app.ui.components.common.HeaderBar
import java.time.LocalDate
import java.time.YearMonth
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.leafree.app.model.EventResponse
import com.leafree.app.util.formatTimeKorean
import com.leafree.app.util.parseUtcToLocalDateTime
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leafree.app.ui.components.common.SectionHeader
import com.leafree.app.util.SharedPreferencesHelper
import com.leafree.app.viewModel.EventViewModel


/**
 * 아카이브 화면 컴포저블
 * 작물 재배 기록과 통계를 보여주는 화면
 *
 * @param navController 화면 전환을 위한 네비게이션 컨트롤러
 * @param notifications 알림 메시지 목록
 */
@Composable
fun ArchiveScreen(
    navController: NavController? = null,
    notifications: MutableList<String> = mutableStateListOf(),
    prefs: SharedPreferencesHelper
    ){

    Scaffold (
        topBar = {
            HeaderBar(
                navController,
                notifications,
                prefs
            )
        }
    ){ innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 30.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SectionHeader(
                title = "식물일지"
            )

            val context = LocalContext.current
            val factory = remember { EventViewModelFactory(context) }
            val eventViewModel: EventViewModel = viewModel(factory = factory)

            var selectedDate by remember { mutableStateOf(LocalDate.now()) }

            LaunchedEffect(selectedDate) {
                eventViewModel.fetchEvents(deviceId = 1L, date = selectedDate)
            }

            val events by eventViewModel.events.collectAsState()

            val filteredEvents = events
                .filter { parseUtcToLocalDateTime(it.occuredAt).toLocalDate() == selectedDate }
                .sortedBy { parseUtcToLocalDateTime(it.occuredAt) }

            val currentMonth = remember { YearMonth.now() }
            val startMonth = remember { currentMonth.minusMonths(12) }
            val endMonth = remember { currentMonth.plusMonths(12) }
            val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

            val calendarState = rememberCalendarState(
                startMonth = startMonth,
                endMonth = endMonth,
                firstVisibleMonth = currentMonth,
                firstDayOfWeek = firstDayOfWeek
            )

            Column {
                ElevatedCard {
                    HorizontalCalendar(
                        state = calendarState,
                        dayContent = { day ->

                            val isCurrentMonth = day.position == com.kizitonwose.calendar.core.DayPosition.MonthDate

                            Day(
                                day = day,
                                isSelected = day.date == selectedDate,
                                isCurrentMonth = isCurrentMonth,
                                onClick = {selectedDate = day.date}
                            ) },
                        monthHeader = { month ->
                            Text(
                                text = "${month.yearMonth.month}",
                                modifier = Modifier.padding(6.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(330.dp)
                            .padding(10.dp)
                    )
                }
            }


            Row(
                modifier = Modifier
                    .padding(
                        vertical = 10.dp
                    )
                    .fillMaxWidth()
            ) {
                Text(
                    text = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 이벤트",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .fillMaxWidth()
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {
                items(filteredEvents) { event ->
                    EventCard(event)
                }
            }


        }
    }
}

@Composable
fun EventCard(event: EventResponse) {
    val backgroundColor = when (event.eventType) {
        "NOTIFY" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        "WARNING" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
        "ACTUATING" -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val message = formatMessage(event.eventContentCode, event.eventData)
    val timeText = formatTimeKorean(parseUtcToLocalDateTime(event.occuredAt))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(color = backgroundColor, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(1.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    vertical = 6.dp
                )
            )
        }
    }
}


val eventMessageTemplates = mapOf(
    // ACTUATING
    "TEMP_CHANGED" to "온도를 {from}°C에서 {to}°C로 변경했어요.",
    "HUM_CHANGED" to "습도를 {from}%에서 {to}%로 변경했어요.",
    "LIGHT_CHANGED" to "조도를 {from}lx에서 {to}lx로 변경했어요.",
    "CO2_PPM_CHANGED" to "이산화탄소 농도를 {from}ppm에서 {to}ppm으로 변경했어요.",
    "LIGHT_DURATION_CHANGED" to "조명 시간을 {from}분에서 {to}분으로 변경했어요.",
    "WATERING_INTERVAL_CHANGED" to "물주기 간격을 {from}분에서 {to}분으로 변경했어요.",
    "SOIL_HUMI_CHANGED" to "토양 수분을 {from}%에서 {to}%로 변경했어요.",

    // WARNING
    "TEMP_HIGH" to "온도가 너무 높았어요. {value}°C",
    "TEMP_LOW" to "온도가 너무 낮았어요. {value}°C",
    "HUM_HIGH" to "습도가 너무 높았어요. {value}%",
    "HUM_LOW" to "습도가 너무 낮았어요. {value}%",
    "WATER_LOW" to "물탱크 수위가 부족했어요, {value}%",

    // NOTIFY
    "CROP_REGISTERED" to "{cropType}을(를) 심었어요.",
    "CROP_DELETED" to "{cropType} 키우기를 종료했어요. ",
    "CROP_HARVESTED" to "{cropType}을(를) 수확했어요."
)

fun formatMessage(contentCode: String, data: Map<String, Any>): String {
    val template = eventMessageTemplates[contentCode] ?: "알 수 없는 이벤트입니다."
    var message = template
    data.forEach { (key, value) ->
        message = message.replace("{$key}", value.toString())
    }
    return message
}

@Composable
fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    onClick: () -> Unit) {

    val textColor = if (isCurrentMonth) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .then(
                if (isSelected) Modifier
                    .padding(2.dp)
                    .height(40.dp)
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

class EventViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EventViewModel(context) as T
    }
}

