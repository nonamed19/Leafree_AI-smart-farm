package com.leafree.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.leafree.app.viewModel.LoginViewModel
import com.leafree.app.viewModel.LoginUiState
import com.leafree.app.R
import com.leafree.app.ui.components.common.CommonOutlinedTextField

@Composable
fun LoginScreen(
onLoginSuccess: (String) -> Unit,
viewModel: LoginViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess((uiState as LoginUiState.Success).accessToken)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                vertical = 120.dp,
                horizontal = 30.dp
            )
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Leafree",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.padding(16.dp))

        Image(
            painter = painterResource(id = R.drawable.leafree_icon),
            contentDescription = "앱 이미지",
            modifier = Modifier
                .size(200.dp)
        )

        Spacer(modifier = Modifier.padding(16.dp))

        Column {

            CommonOutlinedTextField(
                value = email,
                onValueChange = {email = it},
                label = "이메일",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            CommonOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "비밀번호",
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "로그인",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (uiState is LoginUiState.Error) {
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
