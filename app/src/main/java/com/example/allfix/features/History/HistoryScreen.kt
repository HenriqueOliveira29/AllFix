package com.example.allfix.features.History

import com.example.allfix.features.fixdetails.Fixes
import com.example.allfix.features.fixdetails.Type
import com.example.allfix.features.fixdetails.User
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.allfix.ui.theme.AllFixTheme
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun HistoryScreen(state: HistoryScreenState, navController: NavController, modifier: Modifier = Modifier){
    when(state)
    {
        HistoryScreenState.Loading -> {
            Box(modifier.fillMaxSize()){
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
        is HistoryScreenState.Success -> {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)){
                    items(state.fixes){fix ->
                        Row(Modifier
                            .fillMaxSize()
                            .height(86.dp)
                            .border(border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(12.dp)).clickable {
                                navController.navigate("Details/${fix.id}")
                            },
                            verticalAlignment = Alignment.CenterVertically,
                        ){
                            Spacer(Modifier.size(8.dp))
                            Box(Modifier
                                .clip(CircleShape)
                                .size(56.dp)
                                .background(Color.Gray))
                            Spacer(Modifier.size(8.dp))
                            Column(
                                Modifier.heightIn(64.dp),
                                verticalArrangement = Arrangement.Center){
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(fix.problem, style = TextStyle.Default.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ))
                                }
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(fix.location)
                                    Box(Modifier
                                        .clip(CircleShape)
                                        .padding(4.dp))
                                    {
                                        Text(fix.price.toString() + "€")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun HistoryPreview(){
    AllFixTheme {
        Surface {
            HistoryScreen(state = HistoryScreenState.Success(
                currentUser = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"),
                fixes = listOf(
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc= "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null)
                )
            ), navController = rememberNavController()
            )
        }
    }
}

@Preview
@Composable
private fun HistoryScreenPreviewIfLoadingState(){
    AllFixTheme {
        Surface {
            HistoryScreen(state = HistoryScreenState.Loading, navController = rememberNavController())
        }
    }
}