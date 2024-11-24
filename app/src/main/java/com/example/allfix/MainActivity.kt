package com.example.allfix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allfix.ui.theme.AllFixTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllFixTheme {
                App()
            }
        }
    }
}

class BottomAppBarItem(
    val label: String,
    val icon: ImageVector
)

class TopAppBarItem(
    val title: String,
    val icons: List<ImageVector> = emptyList()
)

sealed class ScreenItem(
    val topAppItem: TopAppBarItem,
    val bottomAppItem: BottomAppBarItem

) {
    data object Home : ScreenItem(
        topAppItem = TopAppBarItem(title = "FixAll", icons = listOf(Icons.AutoMirrored.Default.ExitToApp)),
        bottomAppItem = BottomAppBarItem(
            icon = Icons.Default.House,
            label = "Home"
        )

    )

    data object History : ScreenItem(
        topAppItem = TopAppBarItem(title = "FixAll", icons = listOf(Icons.AutoMirrored.Default.ExitToApp)),
        bottomAppItem = BottomAppBarItem(
            icon = Icons.Default.AccessTime,
            label = "History"
        )

    )

    data object Profile : ScreenItem(
        topAppItem = TopAppBarItem(title = "FixAll", icons = listOf(Icons.AutoMirrored.Default.ExitToApp)),
        bottomAppItem = BottomAppBarItem(
            icon = Icons.Default.Person,
            label = "Profile"
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun App(){
    val screens = remember {
        listOf(
//            NavItem(icon = Icons.Default.Add, label = "Add"),
            ScreenItem.History,
            ScreenItem.Home,
            ScreenItem.Profile)
    }

    var currentScreen by remember {
        mutableStateOf(screens[1])
    }

    val pagerState = rememberPagerState {
        screens.size
    }
    
    LaunchedEffect(currentScreen) {
        pagerState.animateScrollToPage(screens.indexOf(currentScreen))
    }

    LaunchedEffect(pagerState.targetPage) {
        currentScreen = screens[pagerState.targetPage]
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(currentScreen.topAppItem.title) }, actions = {
                Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    currentScreen.topAppItem.icons.forEach{ icon ->
                        Icon(icon, contentDescription = null)
                    }
                }
            })
        },
        bottomBar = {
            BottomAppBar {
                screens.forEach{ screen ->
                    with(screen.bottomAppItem) {
                        NavigationBarItem(
                            selected = screen == currentScreen,
                            onClick = {
                                currentScreen = screen
                            },
                            icon = {
                                Icon(icon, contentDescription = null)
                            },
                            label = {
                                Text(label)
                            }
                        )
                    }

                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(pagerState, Modifier.padding(innerPadding)) { page ->
            val item = screens[page]
            when (item){
                ScreenItem.Home -> FixList()
                ScreenItem.History -> History()
                ScreenItem.Profile -> Profile()
            }

        }

    }
}

@Composable
fun FixList(modifier: Modifier = Modifier){
    Box(modifier.fillMaxSize()){
        Text("Fix List", Modifier.align(Alignment.Center), style = TextStyle.Default.copy(
            fontSize = 32.sp
        ))
    }
}

@Composable
fun Profile(modifier: Modifier = Modifier){
    Box(modifier.fillMaxSize()){
        Text("Profile", Modifier.align(Alignment.Center), style = TextStyle.Default.copy(
            fontSize = 32.sp
        ))
    }
}

@Composable
fun History(modifier: Modifier = Modifier){
    Box(modifier.fillMaxSize()){
        Text("History", Modifier.align(Alignment.Center), style = TextStyle.Default.copy(
            fontSize = 32.sp
        ))
    }
}

@Preview
@Composable
private fun AppPreview() {
    AllFixTheme {
        App()
    }
}