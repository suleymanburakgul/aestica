package dev.kebab.aestica.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.kebab.aestica.DestinationScreen
import dev.kebab.aestica.R

enum class BottomNavigationItem(
    val icon: Int,
    val navDestination: DestinationScreen
) {
    FEED(R.drawable.baseline_home_24, DestinationScreen.Feed),
    SEARCH(R.drawable.baseline_search_24, DestinationScreen.Search),
    POSTS(R.drawable.baseline_posts_24, DestinationScreen.MyPosts)
}

@Composable
fun BottomNavigationMenu(
    selectedItem: BottomNavigationItem,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 4.dp)
            .background(Color.White)
    ) {
        for(item in BottomNavigationItem.values()) {
            Image(
                painter = painterResource(id = item.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(5.dp)
                    .weight(1f)
                    .clickable {
                        navController.navigate(item.navDestination.route) {

                        }
                    },
                colorFilter = if(item == selectedItem) {
                    ColorFilter.tint(MaterialTheme.colorScheme.primary)
                } else {
                    ColorFilter.tint(Color.Gray)
                }
            )
        }
    }
}