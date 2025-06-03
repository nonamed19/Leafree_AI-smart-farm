package com.leafree.app.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leafree.app.model.SettingInfo
import com.leafree.app.repository.SettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingViewModel(private val context: Context) : ViewModel() {
    private val _settings = MutableStateFlow<SettingInfo?>(null)
    val settings: StateFlow<SettingInfo?> = _settings

    private val repository = SettingRepository(context)

    fun fetchSettings(deviceId: Long) {
        viewModelScope.launch {
            _settings.value = repository.getSettings(deviceId)
        }
    }

    fun updateSetting(deviceId: Long, newSetting: SettingInfo) {
        viewModelScope.launch {
            _settings.value = newSetting
            repository.updateSetting(deviceId, newSetting)
        }
    }
}
