package com.example.allfix.features.fixdetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class FixDetailScreenState {
    data object Loading : FixDetailScreenState()
    data class Success(
        val fix: Fixes,
    ) : FixDetailScreenState()
    data class Error(
        val message: String
    ): FixDetailScreenState()
}

class FixDetailViewModelFactory(private val fixId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FixDetailViewModel(fixId) as T
    }
}

class FixDetailViewModel(private var fixId: String)  : ViewModel() {
    private val _state = MutableStateFlow<FixDetailScreenState>(FixDetailScreenState.Loading)
    val state = _state.asStateFlow()
    val db = Firebase.firestore


    init {
        fetchFix(fixId)
    }

    private fun fetchFix(fixId: String) {
        Log.d("FixDetail", "Fetching details for fixId: $fixId")

        // Set state to loading before fetching data
        _state.update { FixDetailScreenState.Loading }

        viewModelScope.launch {
            try {
                val document = db.collection("fixes").document(fixId).get().await()

                // If document does not exist, return an error state
                if (!document.exists()) {
                    _state.update { FixDetailScreenState.Error("Fix with id $fixId not found") }
                    return@launch
                }

                val userReference = document.getDocumentReference("creator")
                var user: User = User() // Default empty user object

                // If user reference is not null, fetch the user data
                userReference?.let {
                    try {
                        val userSnapshot = it.get().await()
                        if (userSnapshot.exists()) {
                            user = userSnapshot.toObject(User::class.java) ?: User()
                        } else {

                        }
                    } catch (e: Exception) {
                        Log.e("FixDetail", "Error fetching user data: ${e.message}")
                    }
                }

                val fixerReference = document.getDocumentReference("fixer")
                var fixer: User? = null // Default empty user object

                // If fixer reference is not null, fetch the fixer data
                fixerReference?.let {
                    try {
                        val fixerSnapshot = it.get().await()
                        if (fixerSnapshot.exists()) {
                            fixer = fixerSnapshot.toObject(User::class.java) ?: null
                        } else {

                        }
                    } catch (e: Exception) {
                        Log.e("FixDetail", "Error fetching fixer data: ${e.message}")
                    }
                }

                Log.d("Warn", document.getString("date").orEmpty())

                // Create and return Fixes object
                val fix = Fixes(
                    id = document.id,
                    location = document.getString("location").orEmpty(),
                    price = document.getDouble("price")?.toFloat() ?: 0.0F,
                    creator = user,
                    desc = document.getString("description").orEmpty(),
                    problem = document.getString("problem").orEmpty(),
                    date = document.getTimestamp("date")?.toDate().toString() ?: "",
                    fixer = fixer
                )

                // Update the state with the fetched data
                _state.update {
                    FixDetailScreenState.Success(
                        fix = fix,
                    )
                }

            } catch (e: Exception) {
                // Handle any error that occurred while fetching data
                _state.update { FixDetailScreenState.Error("Error fetching data: ${e.message}") }
            }

        }
    }
    fun updateFixId(newFixId: String) {
        if (this.fixId != newFixId) {
            this.fixId = newFixId
            fetchFix(newFixId) // Trigger data fetch for the new fixId
        }
    }
}
