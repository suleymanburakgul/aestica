package dev.kebab.aestica.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.kebab.aestica.DestinationScreen
import dev.kebab.aestica.IgViewModel
import dev.kebab.aestica.R
import dev.kebab.aestica.data.model.PostModel

data class PostRow(
    var post1: PostModel? = null,
    var post2: PostModel? = null,
    var post3: PostModel? = null
) {
    fun isFull() = post1 != null && post2 != null && post3 != null
    fun add(post: PostModel) {
        if(post1 == null) {
            post1 = post
        } else if (post2 == null) {
            post2 = post
        } else if (post3 == null) {
            post3 = post
        }
    }
}

@Composable
fun MyPostsScreen(
    navController: NavController,
    viewModel: IgViewModel
) {
    val colorScheme = MaterialTheme.colorScheme

    val newPostImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val encoded = Uri.encode(it.toString())
                val route = DestinationScreen.NewPost.createRoute(encoded)
                navController.navigate(route)
            }
        }
    )

    val userData = viewModel.userData.value
    val isLoading = viewModel.inProgress.value

    val postsLoading = viewModel.refreshPostsProgress.value
    val posts = viewModel.posts.value

    val followers = viewModel.followers.value

    Column {
        Column(
            Modifier.weight(1f)
        ) {
            Row {
                ProfileImage(
                    userData?.imageUrl,
                    onClick = {
                        newPostImageLauncher.launch("image/*")
                    }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${posts.size}",
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "posts",
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$followers",
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "followers",
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${userData?.following?.size ?: 0}",
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "following",
                        textAlign = TextAlign.Center
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                val userNameDisplay = if(userData?.username == null) {
                    ""
                } else {
                    "@${userData.username}"
                }
                Text(
                    text = userData?.name ?: "",
                    fontWeight = FontWeight.Bold
                )
                Text(text = userNameDisplay)
                Text(text = userData?.bio ?: "")
            }
            OutlinedButton(
                onClick = {
                    navigateTo(
                        navController = navController,
                        dest = DestinationScreen.Profile
                    )
                },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = colorScheme.primary
                ),
                border = BorderStroke(1.dp, colorScheme.primary),
                shape = RoundedCornerShape(10),
                elevation = null
            ) {
                Text("Edit Profile", fontSize = 16.sp)
            }
            PostList(
                isContextLoading = isLoading,
                postsLoading = postsLoading,
                posts = posts,
                modifier = Modifier
                    .weight(1f)
                    .padding(1.dp)
                    .fillMaxSize(),
                onPostClick = { post ->
                    viewModel.selectPost(post)
                    navController.navigate(DestinationScreen.SinglePost.route)
                }
            )
        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.POSTS,
            navController = navController
        )
    }

    if(isLoading) {
        CommonProgressSpinner()
    }

}

@Composable
fun ProfileImage(
    imageUrl: String? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .padding(top = 16.dp)
            .clickable {
                onClick()
            }
    ) {
        UserImageCard(
            userImage = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(100.dp)
        )
        Card(
            shape = CircleShape,
            border = BorderStroke(width = 2.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_add_24),
                contentDescription = null,
                modifier = Modifier.background(MaterialTheme.colorScheme.primary),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}

@Composable
fun PostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<PostModel>,
    modifier: Modifier,
    onPostClick: (PostModel) -> Unit
) {
    if(postsLoading) {
        CommonProgressSpinner()
    } else if(posts.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if(!isContextLoading) Text("No Posts Available")
        }
    } else {
        LazyColumn(modifier = modifier) {
            val rows = arrayListOf<PostRow>()
            var currentRow = PostRow()
            rows.add(currentRow)
            for(post in posts) {
                if(currentRow.isFull()) {
                    currentRow = PostRow()
                    rows.add(currentRow)
                }
                currentRow.add(post)
            }

            items(items = rows) {row ->
                PostsRow(item = row, onPostClick = onPostClick)
            }
        }
    }
}

@Composable
fun PostsRow(item: PostRow, onPostClick: (PostModel) -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
    ) {
        PostImage(
            imageUrl = item.post1?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    item.post1?.let { post ->
                        onPostClick(post)
                    }
                }
        )
        PostImage(
            imageUrl = item.post2?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    item.post2?.let { post ->
                        onPostClick(post)
                    }
                }
        )
        PostImage(
            imageUrl = item.post3?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    item.post3?.let { post ->
                        onPostClick(post)
                    }
                }
        )
    }
}

@Composable
fun PostImage(imageUrl: String?, modifier: Modifier) {
    Box(modifier = modifier) {
        var modifier = Modifier.padding(1.dp).fillMaxSize()

        if(imageUrl == null) {
            modifier = modifier.clickable(enabled = false) {}
        }
        CommonImage(data = imageUrl, modifier = modifier, contentScale = ContentScale.Crop)
    }
}