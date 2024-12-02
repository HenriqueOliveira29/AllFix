package com.example.allfix.features.fixlist

import android.R
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allfix.ui.theme.Typography
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

sealed class FixListScreenState {
    data object Loading : FixListScreenState()
    data class Success(
        val currentUser: User,
        val searchText: String = "",
        val fixes: List<Fixes> = emptyList(),
    ) : FixListScreenState()
}

class Fixes(
    val problem: String = "",
    val location: String = "",
    val price: Float = 0F,
    val creator: User = User(),
    val desc: String = "",
    val images: List<String> = emptyList(),
    val date: String = "",
    val fixer: User? = null,
)

class User(
    val id: String = "",
    val avatar: String = "",
    val name: String = "",
    val type: Type = Type.USER,
)

enum class Type{ FIXER, USER }

class FixListViewModel : ViewModel(){
    private val _state = MutableStateFlow<FixListScreenState>(FixListScreenState.Loading)
    val state = _state.asStateFlow()
    val db = Firebase.firestore


    init{
        viewModelScope.launch{
            val user = fetchUser()
            val fixes = fetchFixes()
            delay(Random.nextLong(1000,3000))
            _state.update {
                FixListScreenState.Success(
                    currentUser = user,
                    fixes = fixes,
                    searchText = ""
                )
            }
        }
    }

    private suspend fun fetchFixes(): List<Fixes> {
        var data =  db.collection("fixes").get().await()

        val fixesList = data.documents.map { document ->
            // Assuming you also have a nested "user" field that is a map of User data
            val userReference = document.getDocumentReference("creator")
            var user : User = User()  // Default empty user object

            // If user reference is not null, fetch the user data
            if (userReference != null) {
                try {
                    val userSnapshot = userReference.get().await()
                    if (userSnapshot.exists()) {
                        // Extract user data from the user document
                        user = userSnapshot.toObject(User::class.java) ?: User()
                    }
                } catch (e: Exception) {
                    // Handle the error if fetching the user data fails
                    println("Error fetching user data: ${e.message}")
                }
            }


            val fixerReference = document.getDocumentReference("creator")
            var fixer : User? = null  // Default empty user object

            // If user reference is not null, fetch the user data
            if (fixerReference != null) {
                try {
                    val userSnapshot = fixerReference.get().await()
                    if (userSnapshot.exists()) {
                        // Extract user data from the user document
                        fixer = userSnapshot.toObject(User::class.java) ?: null
                    }
                } catch (e: Exception) {
                    // Handle the error if fetching the user data fails
                    println("Error fetching user data: ${e.message}")
                }
            }

            // Create the Fixes object
            Fixes(
                location = document.getString("location").toString() ?: "",
                price = document.getDouble("price")?.toFloat() ?: 0.0F,
                creator = user,
                desc = document.getString("descricao").toString() ?: "",
                problem = document.getString("Problem").toString() ?: "",
                date = document.get("Date").toString() ?: "",
                fixer = fixer

            )
        }
        // Return the list of Fixes objects
        return fixesList
    }

    private fun fetchUser(): User {
        return User("1234", "test", "test", Type.USER)
    }
}