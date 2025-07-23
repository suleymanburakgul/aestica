package dev.kebab.aestica.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.kebab.aestica.DestinationScreen
import dev.kebab.aestica.IgViewModel
import dev.kebab.aestica.R
import dev.kebab.aestica.data.model.PostModel
import dev.kebab.aestica.ui.theme.LightCardBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: IgViewModel
) {
    val userDataLoading = viewModel.inProgress.value
    val userData = viewModel.userData.value
    val personalizedFeed = viewModel.postsFeed.value
    val personalizedFeedLoading = viewModel.postFeedProgress.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightCardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
        ) {
            UserImageCard(userImage = userData?.imageUrl)
        }
        FeedPostList(
            posts = personalizedFeed,
            modifier = Modifier.weight(1f),
            loading = personalizedFeedLoading or userDataLoading,
            navController = navController,
            viewModel = viewModel,
            currentUserId = userData?.userId ?: ""
        )
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.FEED,
            navController = navController
        )
    }
}

@Composable
fun FeedPostList(
    posts: List<PostModel>,
    modifier: Modifier,
    loading: Boolean,
    navController: NavController,
    viewModel: IgViewModel,
    currentUserId: String
) {
    Box(modifier = modifier) {
        LazyColumn {
            items(items = posts) { post ->
                FeedPost(
                    post = post,
                    currentUserId = currentUserId,
                    viewModel = viewModel,
                    onPostClick = {
                        viewModel.selectPost(post)
                        navigateTo(
                            navController =  navController,
                            dest = DestinationScreen.SinglePost,
                        )
                    }
                )
            }
        }
        if(loading)
            CommonProgressSpinner()
    }
}

@Composable
fun FeedPost(
    post: PostModel,
    currentUserId: String,
    viewModel: IgViewModel,
    onPostClick: () -> Unit
) {
    val likeAnimation = remember { mutableStateOf(false) }
    val dislikeAnimation = remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(corner = CornerSize(4.dp)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(LightCardBackground)
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.background(LightCardBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserImageCard(userImage = post.userImage, modifier = Modifier.padding(4.dp).size(32.dp))
                Text(text = post.username ?: "", modifier = Modifier.padding(4.dp))
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val imageModifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 150.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (post.likes?.contains(currentUserId) == true) {
                                    dislikeAnimation.value = true
                                } else {
                                    likeAnimation.value = true
                                }
                                viewModel.onLikePost(post)
                            },
                            onTap = { onPostClick() }
                        )
                    }
                Column {
                    CommonImage(
                        data = post.postImage,
                        modifier = imageModifier,
                        contentScale = ContentScale.FillWidth
                    )
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
                }

                if (likeAnimation.value) {
                    LaunchedEffect(key1 = "like_${post.postId}") {
                        delay(1000L)
                        likeAnimation.value = false
                    }
                    LikeAnimation()
                }

                if (dislikeAnimation.value) {
                    LaunchedEffect(key1 = "dislike_${post.postId}") {
                        delay(1000L)
                        dislikeAnimation.value = false
                    }
                    LikeAnimation( false)
                }
            }
        }
    }
}
