package com.leafree.app.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leafree.app.api.LeafreeApi
import com.leafree.app.di.NetworkModule
import com.leafree.app.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class LoginViewModel(private val context: Context) : ViewModel() {
    private val api: LeafreeApi = NetworkModule.provideApi(context)
    private val tokenManager = TokenManager(context)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = LoginUiState.Loading
                val response = api.login(LeafreeApi.LoginRequest(email, password))
                tokenManager.saveToken(response.accessToken)
                _uiState.value = LoginUiState.Success(
                    userId = response.userId,
                    accessToken = response.accessToken
                )


            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = try {
                    val json = JSONObject(errorBody ?: "")
                    json.getString("message")
                } catch (ex: Exception) {
                    "서버 에러 응답 파싱 실패"
                }
                _uiState.value = LoginUiState.Error(errorMessage)

            } catch (e: Exception) {
                // 기타 예외
                _uiState.value = LoginUiState.Error("알 수 없는 오류가 발생했습니다.")
            }
        }
    }


    fun resetState() {
        _uiState.value = LoginUiState.Initial
    }
}

sealed class LoginUiState {
    object Initial : LoginUiState()

    object Loading : LoginUiState()

    data class Success(
        val userId: String,
        val accessToken: String
    ) : LoginUiState()

    data class Error(val message: String) : LoginUiState()
}