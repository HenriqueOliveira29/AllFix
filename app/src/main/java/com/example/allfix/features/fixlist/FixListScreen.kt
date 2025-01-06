package com.example.allfix.features.fixdetails

import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.allfix.ui.theme.AllFixTheme
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import coil.compose.rememberImagePainter

@Composable
fun FixListScreen(state: FixListScreenState, navController: NavController, modifier: Modifier = Modifier){
    var isCreatingFix by remember { mutableStateOf<Boolean>(false) }
    var createFix by remember { mutableStateOf<Fixes?>(null) }

    when(state)
    {
        FixListScreenState.Loading -> {
            Box(modifier.fillMaxSize()){
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
        is FixListScreenState.Success -> {
            Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)){
                // search text field
                item {
                    Row(Modifier
                        .clip(CircleShape)
                        .fillMaxWidth()
                        .background(Color.Gray)
                        .padding(16.dp)) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Search...")
                    }
                }
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
                            .background(Color.Gray)){
                            Image(
                                painter = rememberImagePainter(
                                    data = fix.creator.avatar,
                                    builder = {
                                        crossfade(true)
                                    }
                                ),
                                contentDescription = "Fix Avatar",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
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
                if (state.currentUser.type.name == "USER"){
                    FloatingActionButton(
                        onClick = {
                            // Handle action when button is clicked
                            isCreatingFix = true
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp) // Optional padding for spacing
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "New Conversation") // Plus icon
                    }
                }

                if (isCreatingFix) {
                    createFix = Fixes()
                    CreateFixScreen(
                        onDismiss = {
                            isCreatingFix = false
                            createFix = null},
                        fix = createFix!!,
                        currentUser = state.currentUser
                    )
                }
            }
        }

        else -> {}
    }
}

@Composable
private fun CreateFixScreen(onDismiss: () -> Unit, fix: Fixes, currentUser: User){
    var problem by remember { mutableStateOf(fix.problem) }
    var location by remember { mutableStateOf(fix.location) }
    var desc by remember { mutableStateOf(fix.desc) }
    var price by remember { mutableStateOf(fix.price.toString()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // Background dimmed
            .clickable { onDismiss() } // Dismiss when clicked outside
    ) {
        // Inner content (Form for creating a new fix)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Create a New Fix",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )

                TextField(
                    value = problem,
                    onValueChange = { problem = it },
                    label = { Text("Problem") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Price TextField
                TextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal // This will bring up the number keypad
                    )
                )

                Spacer(Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        val priceValue = price.toFloatOrNull() ?: 0f
                        if (desc.isNotEmpty() && location.isNotEmpty() && priceValue > 0 && problem.isNotEmpty()
                        ) {
                            fix.desc = desc
                            fix.price = priceValue
                            fix.problem = problem
                            fix.location = location
                            createFixDb(fix, currentUser, { success ->
                                if (success) {
                                    onDismiss() // Close the screen
                                } else {
                                    // Handle failure (e.g., show a toast or snackbar)
                                    println("Failed to create the fix")
                                }})
                        } else {
                            // Handle validation error if needed
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Fix")
                }
            }
        }
    }
}

private fun createFixDb(fix: Fixes, currentUser: User, onComplete: (Boolean) -> Unit){
    val db = FirebaseFirestore.getInstance()

    // Create a new document reference or use an existing one
    val fixRef = db.collection("fixes").document()

    // Create a map of the Fix object to save
    val fixMap = mapOf(
        "problem" to fix.problem,
        "location" to fix.location,
        "price" to fix.price,
        "creator" to db.document("/users/${currentUser.id}"),
        "desc" to fix.desc,
        "images" to fix.images,
        "date" to Timestamp.now(),
        "fixer" to null,
        "state" to fix.state.name
    )

    // Save the Fix object to Firestore
    fixRef.set(fixMap)
        .addOnSuccessListener {
            onComplete(true) // Success
        }
        .addOnFailureListener { e ->
            onComplete(false) // Failure
        }
}

@Preview
@Composable
private fun FixListScreenPreview(){
    AllFixTheme {
        Surface {
            FixListScreen(state = FixListScreenState.Success(
                currentUser = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"),
                fixes = listOf(Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc= "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null),
                    Fixes(location = "valongo", price = 22.00F, creator = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), desc = "teste teste teste test", problem = "Problema de juntas", date = "teste", fixer = null))
            ), navController = rememberNavController()
            )
        }
    }
}

@Preview
@Composable
private fun FixListScreenPreviewIfLoadingState(){
    AllFixTheme {
        Surface {
            FixListScreen(state = FixListScreenState.Loading, navController = rememberNavController())
        }
    }
}