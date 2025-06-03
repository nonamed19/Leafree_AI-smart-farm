package com.leafree.app.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leafree.app.model.EventResponse
import com.leafree.app.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventViewModel(context: Context) : ViewModel() {
    private val repository = EventRepository(context)

    private val _events = MutableStateFlow<List<EventResponse>>(emptyList())
    val events: StateFlow<List<EventResponse>> = _events

    fun fetchEvents(deviceId: Long, date: LocalDate) {
        viewModelScope.launch {
            try {
                val formattedDate = date.format(DateTimeFormatter.ISO_DATE)
                println("📡 이벤트 요청: $formattedDate")
                val response = repository.getEvents(deviceId, formattedDate)
                println("✅ 이벤트 응답 수: ${response.size}")
                _events.value = response
            } catch (e: Exception) {
                println("❌ 이벤트 불러오기 실패: ${e.message}")
                e.printStackTrace()
            }
        }
    }

}
