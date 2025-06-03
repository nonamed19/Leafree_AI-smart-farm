package com.leafree.app.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.leafree.app.model.CropInfo
import com.leafree.app.ui.components.common.HeaderBar
import com.leafree.app.ui.components.common.SectionHeader
import com.leafree.app.ui.components.home.SensorList
import com.leafree.app.util.SharedPreferencesHelper
import com.leafree.app.viewModel.CropViewModel
import com.leafree.app.viewModel.SensorViewModel

class SensorViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SensorViewModel(context) as T
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
    notifications: MutableList<String> = mutableStateListOf(),
    viewModel: CropViewModel,
    prefs: SharedPreferencesHelper
) {
    val cropInfo by viewModel.cropInfo.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCropInfo()
    }

    val context = LocalContext.current
    val sensorViewModel: SensorViewModel = viewModel(
        factory = SensorViewModelFactory(context)
    )
    val sensorList by sensorViewModel.sensorList.collectAsState()

    Scaffold(
        topBar = {
            HeaderBar(null, notifications, prefs = prefs)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(
                    start = 30.dp,
                    end = 30.dp,
                    bottom = 20.dp
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SectionHeader(
                title = "스마트팜",
                actions = {
                    IconButton(onClick = { navController?.navigate("archive") }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "달력")
                    }
                    IconButton(onClick = { navController?.navigate("setting") {
                        launchSingleTop = true
                    } }) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                }
            )

            ElevatedCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 10.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    CropInfoCard(cropInfo)

                    SensorList(sensorList = sensorList)
                }
            }


        }
    }
}

@Composable
fun CropInfoCard(info: CropInfo?) {
    Column(
        modifier = Modifier
            .padding(30.dp)
            .fillMaxWidth()
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (info == null) {
            Text(
                text = "작물 정보를 불러오는 중...",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        } else {
            Text(
                text = info.cropType,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp, bottom = 10.dp, start = 12.dp),
                style = MaterialTheme.typography.titleLarge
            )
            ProfileImage(info.cropImage)
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InfoText("재배 시작일 : ${info.createdAt.substring(0, 10)}")
                InfoText("수확 예상일 : ${info.harvestedAt ?: "미정"}")
            }
        }
    }
}


@Composable
fun ProfileImage(profileUri: String? = null) {
    Surface(
        shape = CircleShape,
        shadowElevation = 3.dp
    ) {
        AsyncImage(
            model = profileUri,
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape),
        )
    }
}

@Composable
fun InfoText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(5.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}



