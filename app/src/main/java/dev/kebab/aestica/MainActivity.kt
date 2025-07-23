package dev.kebab.aestica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.kebab.aestica.auth.LoginScreen
import dev.kebab.aestica.auth.ProfileScreen
import dev.kebab.aestica.auth.SignupScreen
import dev.kebab.aestica.data.model.PostModel
import dev.kebab.aestica.main.CommentScreen
import dev.kebab.aestica.main.FeedScreen
import dev.kebab.aestica.main.MyPostsScreen
import dev.kebab.aestica.main.NewPostScreen
import dev.kebab.aestica.main.NotificationMessage
import dev.kebab.aestica.main.SearchScreen
import dev.kebab.aestica.main.SinglePostScreen
import dev.kebab.aestica.ui.theme.AesticaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AesticaTheme(dynamicColor = false) {
                Scaffold { innerPadding ->
                    AesticaApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

sealed class DestinationScreen(val route: String) {
    object Signup: DestinationScreen("signup")
    object Login: DestinationScreen("login")
    object Feed: DestinationScreen("feed")
    object Search: DestinationScreen("search")
    object MyPosts: DestinationScreen("myposts")
    object Profile: DestinationScreen("profile")
    object NewPost: DestinationScreen("newpost/{imageUri}") {
        fun createRoute(uri: String) = "newpost/$uri"
    }
    object SinglePost: DestinationScreen("singlepost")
    object CommentScreen: DestinationScreen("comments/{postId}") {
        fun createRoute(postId: String) = "comments/$postId"
    }
}

@Composable
fun AesticaApp(modifier: Modifier = Modifier) {
    val viewModel = hiltViewModel<IgViewModel>()
    val navController = rememberNavController()

    NotificationMessage(viewModel = viewModel)

    NavHost(
        navController = navController,
        startDestination = DestinationScreen.Signup.route,
        modifier = modifier
    ) {
        composable(DestinationScreen.Signup.route) {
            SignupScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(DestinationScreen.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(DestinationScreen.Feed.route) {
            FeedScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(DestinationScreen.Search.route) {
            SearchScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(DestinationScreen.MyPosts.route) {
            MyPostsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(DestinationScreen.Profile.route) {
            ProfileScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(DestinationScreen.NewPost.route) { navBackStackEntry ->
            val imageUri = navBackStackEntry.arguments?.getString("imageUri")
            imageUri?.let {
                NewPostScreen(
                    navController = navController,
                    viewModel = viewModel,
                    encodedUri = it
                )
            }
        }
        composable(DestinationScreen.SinglePost.route) {
            val post = viewModel.selectedPost.value
            post?.let {
                SinglePostScreen(
                    navController = navController,
                    viewModel = viewModel,
                    post = it
                )
            }
        }
        composable(DestinationScreen.CommentScreen.route) { navBackStackEntry ->
            val postId = navBackStackEntry.arguments?.getString("postId")
            postId?.let {
                CommentScreen(
                    navController = navController,
                    viewModel = viewModel,
                    postId = it
                )
            }
        }
    }
}