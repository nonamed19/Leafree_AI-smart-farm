package com.leafree.app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leafree.app.model.CropInfo
import com.leafree.app.repository.CropRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CropViewModel(
    private val repository: CropRepository
) : ViewModel() {
    private val _cropInfo = MutableStateFlow<CropInfo?>(null)
    val cropInfo: StateFlow<CropInfo?> = _cropInfo

    fun loadCropInfo(deviceId: Long = 1L) {
        viewModelScope.launch {
            try {
                val result = repository.getCropInfo(deviceId)
                _cropInfo.value = result
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    fun resetState() {
        _cropInfo.value = null
    }
}

sealed class CropUiState {
    object Initial : CropUiState()
    object Loading : CropUiState()
    data class Success(val cropInfo: CropInfo) : CropUiState()
    data class Error(val message: String) : CropUiState()
}

