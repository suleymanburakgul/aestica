package dev.kebab.aestica.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.kebab.aestica.DestinationScreen
import dev.kebab.aestica.IgViewModel
import dev.kebab.aestica.main.CommonDivider
import dev.kebab.aestica.main.CommonImage
import dev.kebab.aestica.main.CommonProgressSpinner
import dev.kebab.aestica.main.UserImageCard
import dev.kebab.aestica.main.navigateTo

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: IgViewModel
) {
    val isLoading = viewModel.inProgress.value

    if(isLoading) {
        CommonProgressSpinner()
    } else {
        val userData = viewModel.userData.value
        var name by rememberSaveable {
            mutableStateOf(userData?.name ?: "")
        }
        var userName by rememberSaveable {
            mutableStateOf(userData?.username ?: "")
        }
        var bio by rememberSaveable {
            mutableStateOf(userData?.bio ?: "")
        }

        ProfileContent(
            viewModel = viewModel,
            name = name,
            userName = userName,
            bio = bio,
            onNameChanged = { name = it },
            onUserNameChanged = { userName = it },
            onBioChanged = { bio = it },
            onSave = {
                viewModel.updateProfileData(
                    name = name,
                    username = userName,
                    bio = bio
                )
            },
            onBack = {
                navigateTo(
                    navController = navController,
                    dest = DestinationScreen.MyPosts
                )
            },
            onLogout = {
                viewModel.onLogout()
                navigateTo(
                    navController = navController,
                    dest = DestinationScreen.Login
                )
            }
        )
    }
}

@Composable
fun ProfileContent(
    viewModel: IgViewModel,
    name: String,
    userName: String,
    bio: String,
    onNameChanged: (String) -> Unit,
    onUserNameChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    val imageUrl = viewModel.userData.value?.imageUrl

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Back",
                modifier = Modifier
                    .clickable {
                        onBack()
                    },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Save",
                modifier = Modifier
                    .clickable {
                        onSave()
                    },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        CommonDivider()
        //User Image
        ProfileImage(
            imageUrl = imageUrl,
            viewModel = viewModel
        )
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Name",
                modifier = Modifier.width(100.dp)
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                label = { Text("Name") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Username",
                modifier = Modifier.width(100.dp)
            )
            OutlinedTextField(
                value = userName,
                onValueChange = onUserNameChanged,
                label = { Text("Username") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray
                )
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Bio",
                modifier = Modifier.width(100.dp)
            )
            OutlinedTextField(
                value = bio,
                onValueChange = onBioChanged,
                modifier = Modifier.height(150.dp),
                label = { Text("Bio") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray
                ),
                singleLine = false
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Logout", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ProfileImage(
    imageUrl: String?,
    viewModel: IgViewModel
) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.uploadProfileImage(uri)
            }
        }
    )

    Box(
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable {
                    launcher.launch(
                        "image/*"
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserImageCard(userImage = imageUrl, modifier = Modifier.padding(8.dp).size(100.dp))
            Text("Change Profile Picture")
        }
        val isLoading = viewModel.inProgress.value
        if(isLoading) CommonProgressSpinner()
    }
}