package com.example.allfix

import AppNavigation
import Routes
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.allfix.ui.theme.AllFixTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        enableEdgeToEdge()
        setContent {
            AllFixTheme {
                App()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun App(){
    var selectedFixId by remember { mutableStateOf<String?>(null) }
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate(Routes.Home.route) {
                popUpTo(0)
            }
        } else {
            navController.navigate(Routes.Login.route) {
                popUpTo(0)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("FixAll") },navigationIcon = {
                // Show back arrow only if a FixDetailScreen is selected
                if (selectedFixId != null) {
                    IconButton(
                        onClick = {
                            selectedFixId = null // Go back to the list
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            }, actions = {

                Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (currentUser != null) {
                        Button(onClick = {
                            auth.signOut()
                            currentUser = null
                            navController.navigate(Routes.Login.route) {
                                popUpTo(0)
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                        }
                    }
                }
            })
        },
        bottomBar = {
            if (currentUser != null){
                BottomNavigationBar(navController, Routes.toList)
            }

        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                AppNavigation(navController = navController, currentUser = currentUser)
            }
        }
    )
}

@Composable
fun BottomNavigationBar(navController: NavController, appItems: List<Routes>) {
    BottomAppBar(
    ) {
        appItems.forEach { item ->
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(text = item.title) },
                    selected = false,
                    onClick = {
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route ->
                                popUpTo(route) { saveState = true }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
        }
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