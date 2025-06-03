package com.leafree.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.leafree.app.api.LeafreeApi.FcmTokenRequest
import com.leafree.app.di.NetworkModule
import com.leafree.app.repository.CropImageRepository
import com.leafree.app.repository.CropRepository
import com.leafree.app.ui.screen.ArchiveScreen
import com.leafree.app.ui.screen.HomeScreen
import com.leafree.app.ui.screen.LoginScreen
import com.leafree.app.ui.screen.SettingScreen
import com.leafree.app.ui.theme.LeafreeTheme
import com.leafree.app.util.SharedPreferencesHelper
import com.leafree.app.viewModel.CropViewModel
import com.leafree.app.viewModel.LoginViewModel
import com.leafree.app.viewModel.SettingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LeafreeTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val prefs = remember { SharedPreferencesHelper(context) }

                val cropImageRepository = remember { CropImageRepository() }
                val cropRepository = remember { CropRepository(context, cropImageRepository) }
                val cropViewModel = remember { CropViewModel(cropRepository) }
                val settingViewModel = remember { SettingViewModel(context) }
                val loginViewModel = remember { LoginViewModel(context) }

                val notifications = remember {
                    mutableStateListOf<String>().apply {
                        val rawSet = prefs.getNotificationList()
                        addAll(rawSet)
                    }
                }

                fun addNotification(message: String) {
                    notifications.add(0, message)
                    prefs.saveNotificationList(notifications)
                }

                DisposableEffect(Unit) {
                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            val message = intent?.getStringExtra("message") ?: return

                            notifications.add(0, message)
                            prefs.saveNotificationList(notifications)

                            if (message.contains("심었어요") || message.contains("Crop registered")) {
                                cropViewModel.loadCropInfo()
                            }
                        }
                    }

                    val filter = IntentFilter("com.leafree.NEW_NOTIFICATION")

                    context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)

                    onDispose {
                        context.unregisterReceiver(receiver)
                    }
                }


                LaunchedEffect(Unit) {
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->

                        val deviceId = 1L;
                        val api = NetworkModule.provideApi(context)

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = api.sendFcmToken(
                                    deviceId = deviceId,
                                    token = FcmTokenRequest(fcmToken = token)
                                )
                            } catch (e: Exception) {
                            }
                        }

                    }.addOnFailureListener {
                    }
                }

                val startDestination = if (prefs.getToken() != null) "home" else "login"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { token ->
                                prefs.saveToken(token)
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            viewModel = loginViewModel
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            navController,
                            notifications,
                            viewModel = cropViewModel,
                            prefs = prefs
                        )
                    }
                    composable("setting") {
                        SettingScreen(
                            navController,
                            notifications,
                            viewModel = settingViewModel,
                            prefs = prefs
                        )
                    }
                    composable("archive") {
                        ArchiveScreen(
                            navController,
                            notifications,
                            prefs = prefs
                        )
                    }
                }
            }
        }
    }
}

