package com.example.allfix.features.fixdetails

import Routes
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.allfix.ui.theme.AllFixTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixDetailScreen(
    state: FixDetailScreenState,
    navController: NavController,
    modifier: Modifier = Modifier,
){
    when(state)
    {
        is FixDetailScreenState.Loading -> {
            Box(modifier.fillMaxSize()){
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
        is FixDetailScreenState.Success -> {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(title = { Text("FixALL") },
                        navigationIcon = {

                            // Arrow Back Icon on the left side of the top bar
                            IconButton(onClick = {
                                navController.navigate(Routes.Home.route)
                            }) {
                                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                            }
                        },
                        actions = {
                            Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                        }
                    })
                }) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(bottom = 8.dp).align(alignment = Alignment.CenterHorizontally)
                    )

                    Text(
                        text = "Location: ${state.fix.location}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Price: ${state.fix.price}€",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Description: ${state.fix.desc}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Problem: ${state.fix.problem}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Date: ${state.fix.date}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    state.fix.creator?.let { creator ->
                        Text(
                            text = "Creator: ${creator.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    state.fix.fixer?.let { fixer ->
                        Text(
                            text = "Fixer: ${fixer.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }


        }

        is FixDetailScreenState.Error -> {
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
            FixDetailScreen(state = FixDetailScreenState.Success(
                fix = Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
            ), navController = rememberNavController())
        }
    }
}

@Preview
@Composable
private fun FixListScreenPreviewIfLoadingState(){
    AllFixTheme {
        Surface {
            FixDetailScreen(state = FixDetailScreenState.Loading, navController = rememberNavController())
        }
    }
}