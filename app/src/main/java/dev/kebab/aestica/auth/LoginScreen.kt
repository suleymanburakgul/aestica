package dev.kebab.aestica.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.kebab.aestica.DestinationScreen
import dev.kebab.aestica.IgViewModel
import dev.kebab.aestica.R
import dev.kebab.aestica.main.CheckSignedIn
import dev.kebab.aestica.main.CommonProgressSpinner
import dev.kebab.aestica.main.navigateTo

@Composable
fun LoginScreen(navController: NavController, viewModel: IgViewModel) {
    val colorScheme = MaterialTheme.colorScheme

    CheckSignedIn(viewModel = viewModel, navController = navController)

    val focus = LocalFocusManager.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(
                rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val emailState = remember {
                mutableStateOf(TextFieldValue())
            }
            val passState = remember {
                mutableStateOf(TextFieldValue())
            }

            Image(
                painter = painterResource(R.drawable.aestetica_logo),
                contentDescription = null,
                modifier = Modifier
                    .width(250.dp),
                contentScale = ContentScale.FillWidth
            )
            Text(
                text = "Login",
                modifier = Modifier.padding(8.dp),
                fontSize = 32.sp,
                color = colorScheme.primary,
            )
            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                label = { Text("Email", color = colorScheme.onSurface) },
                textStyle = LocalTextStyle.current.copy(color = colorScheme.onSurface)
            )

            OutlinedTextField(
                value = passState.value,
                onValueChange = { passState.value = it },
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                label = { Text("Password", color = colorScheme.onSurface) },
                visualTransformation = PasswordVisualTransformation(),
                textStyle = LocalTextStyle.current.copy(color = colorScheme.onSurface)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    focus.clearFocus(force = true)
                    viewModel.onLogin(
                        email = emailState.value.text,
                        pass = passState.value.text
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text("Login", fontSize = 16.sp)
            }
            TextButton(
                onClick = {
                    navigateTo(navController, DestinationScreen.Signup)
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    "Need an account? Sign up",
                    color = colorScheme.primary
                )
            }
        }
        val isLoading = viewModel.inProgress.value
        if(isLoading) {
            CommonProgressSpinner()
        }
    }
}