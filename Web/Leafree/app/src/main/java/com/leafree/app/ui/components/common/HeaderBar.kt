package com.leafree.app.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.leafree.app.util.SharedPreferencesHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBar(
    navController: NavController? = null,
    notifications: MutableList<String>,
    prefs: SharedPreferencesHelper
) {
    var expanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        modifier = Modifier.padding(16.dp),
        title = {
            Row {
                Text(
                    text = "Lea",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = "free",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

            }
                },
        navigationIcon = {
            if (navController != null) {
                IconButton(onClick = { navController.navigate("home") }) {
                    Icon(
                        Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = "toHome"
                    )
                }
            }
        },
        actions = {
            Box {
                IconButton(onClick = { expanded = true}) {
                    Icon(
                        imageVector = if (notifications.isEmpty()) Icons.Default.NotificationsNone else Icons.Default.Notifications,
                        contentDescription = if (notifications.isEmpty()) "알림없음" else "알림")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(300.dp)
                        .padding(end = 8.dp) // 살짝 여백
                        .heightIn(max = 300.dp)
                ) {
                    if (notifications.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("알림이 없습니다.") },
                            onClick = { expanded = false }
                        )
                    } else {
                        notifications.forEachIndexed { index, msg ->
                            DropdownMenuItem(
                                text = {
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        Text(
                                            text = msg,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = {
                                            notifications.removeAt(index)
                                            prefs.saveNotificationList(notifications)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "삭제",
                                            )
                                        }
                                    }
                                },
                                onClick = { expanded = false }
                            )
                        }
                    }
                }
            }
        }
    )
}