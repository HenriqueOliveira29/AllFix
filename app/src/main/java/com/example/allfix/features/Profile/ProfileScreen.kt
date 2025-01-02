package com.example.allfix.features.Profile

import Routes
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.allfix.ui.theme.AllFixTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.allfix.features.fixdetails.User
import com.example.allfix.features.fixdetails.Type

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileScreenState,
    navController: NavController,
    modifier: Modifier = Modifier,
){
    when(state)
    {
        is ProfileScreenState.Loading -> {
            Box(modifier.fillMaxSize()){
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
        is ProfileScreenState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(bottom = 8.dp).align(alignment = Alignment.CenterHorizontally)
                    )

                    Text(
                        text = "Name: ${state.currentUser.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Avatar: ${state.currentUser.avatar}€",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Description: ${state.currentUser.type}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

        is ProfileScreenState.Error -> {
            Log.d("Warn", state.message)
            Text("ERROR: ${state.message.toString()}")
        }
    }
}

@Preview
@Composable
private fun FixListScreenPreview(){
    AllFixTheme {
        Surface {
            ProfileScreen(state = ProfileScreenState.Success(
                currentUser = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste")
            ), navController = rememberNavController())
        }
    }
}

@Preview
@Composable
private fun FixListScreenPreviewIfLoadingState(){
    AllFixTheme {
        Surface {
            ProfileScreen(state = ProfileScreenState.Loading, navController = rememberNavController())
        }
    }
}