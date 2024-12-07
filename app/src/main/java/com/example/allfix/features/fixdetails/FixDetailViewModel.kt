package com.example.allfix.features.fixdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

sealed class FixDetailScreenState {
    data object Loading : FixDetailScreenState()
    data class Success(
        val fix: Fixes,
    ) : FixDetailScreenState()
}

class FixDetailViewModelFactory(private val fixId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FixDetailViewModel(fixId) as T
    }
}

class FixDetailViewModel(fixId: String) : ViewModel(){
    private val _state = MutableStateFlow<FixDetailScreenState>(FixDetailScreenState.Loading)
    val state = _state.asStateFlow()
    val db = Firebase.firestore


    init{
        viewModelScope.launch{
            val fix = fetchFix(fixId = fixId)
            delay(Random.nextLong(1000,3000))
            _state.update {
                FixDetailScreenState.Success(
                    fix = fix
                )
            }
        }
    }

    private suspend fun fetchFix(fixId: String): Fixes {
        val document = db.collection("fixes").document(fixId).get().await()

        // If document does not exist, return an empty Fixes object or handle the error
        if (!document.exists()) {
            throw Exception("Fix with id $fixId not found")
        }

        val userReference = document.getDocumentReference("creator")
        var user: User = User() // Default empty user object

        // If user reference is not null, fetch the user data
        if (userReference != null) {
            try {
                val userSnapshot = userReference.get().await()
                if (userSnapshot.exists()) {
                    user = userSnapshot.toObject(User::class.java) ?: User()
                }
            } catch (e: Exception) {
                // Handle error fetching user data
                println("Error fetching user data: ${e.message}")
            }
        }

        val fixerReference = document.getDocumentReference("fixer")
        var fixer: User? = null // Default empty user object

        // If fixer reference is not null, fetch the fixer data
        if (fixerReference != null) {
            try {
                val fixerSnapshot = fixerReference.get().await()
                if (fixerSnapshot.exists()) {
                    fixer = fixerSnapshot.toObject(User::class.java) ?: null
                }
            } catch (e: Exception) {
                // Handle error fetching fixer data
                println("Error fetching fixer data: ${e.message}")
            }
        }

        // Create and return Fixes object
        return Fixes(
            location = document.getString("location").orEmpty(),
            price = document.getDouble("price")?.toFloat() ?: 0.0F,
            creator = user,
            desc = document.getString("description").orEmpty(),
            problem = document.getString("problem").orEmpty(),
            date = document.getString("date").orEmpty(),
            fixer = fixer
        )
        // Return the list of Fixes object
    }
}