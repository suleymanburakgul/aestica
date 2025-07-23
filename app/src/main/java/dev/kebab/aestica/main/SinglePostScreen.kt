package dev.kebab.aestica.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import dev.kebab.aestica.DestinationScreen
import dev.kebab.aestica.IgViewModel
import dev.kebab.aestica.R
import dev.kebab.aestica.data.model.PostModel

@Composable
fun SinglePostScreen(
    navController: NavController,
    viewModel: IgViewModel,
    post: PostModel,
) {

    val comments = viewModel.comments.value

    LaunchedEffect(key1 = Unit) {
        viewModel.getComments(post.postId)
    }

    post.userId?.let {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
        ) {
            Text(
                "Back",
                modifier = Modifier
                    .clickable {
                        navController.popBackStack()
                    },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            CommonDivider()
            SinglePostDisplay(
                navController = navController,
                viewModel = viewModel,
                post = post,
                numberOfComments = comments.size
            )
        }
    }
}

@Composable
fun SinglePostDisplay(
    navController: NavController,
    viewModel: IgViewModel,
    post: PostModel,
    numberOfComments: Int
) {
    val userData = viewModel.userData.value
    Box(
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserImageCard(userImage = post.userImage, modifier = Modifier.padding(4.dp).size(32.dp))
            Text(post.username ?: "")
            Text(text = ".", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)

            if(userData?.userId == post.userId) {
                // Current Users Post dont show anything
            } else if(userData?.following?.contains(post.userId) == true){
                Text(
                    text = "Following",
                    color = Color.Gray,
                    modifier = Modifier.clickable {
                        viewModel.onFollowClick(post.userId!!)
                    }
                )
            } else {
                Text(
                    text = "Following",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        viewModel.onFollowClick(post.userId!!)
                    }
                )
            }
        }
    }
    Box {
        val modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)

        CommonImage(
            data = post.postImage,
            modifier = modifier,
            contentScale = ContentScale.FillWidth
        )
    }

    Row(
        modifier = Modifier
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.baseline_like_24),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(Color.Red)
        )
        Text(
            " ${post.likes?.size ?: 0} likes",
            modifier = Modifier.padding(start = 0.dp)
        )
    }
    Row(modifier = Modifier.padding(8.dp)) {
        Text(text = post.username ?: "", fontWeight = FontWeight.Bold)
        Text(text = post.postDescription ?: "", modifier = Modifier.padding(start = 8.dp))
    }
    Row(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "$numberOfComments comments",
            color = Color.Gray,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable {
                    post.postId?.let {
                        navController.navigate(DestinationScreen.CommentScreen.createRoute(it))
                    }
                }
        )
    }
}