package com.example.allfix.features.Profile

import com.example.allfix.features.fixdetails.User
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.allfix.features.fixdetails.Type
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class ProfileScreenState {
    data object Loading : ProfileScreenState()
    data class Success(
        val currentUser: User
    ) : ProfileScreenState()
    data class Error(
        val message: String
    ): ProfileScreenState()
}

class ProfileViewModelFactory(private val currentUser: FirebaseUser) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(currentUser) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProfileViewModel(private var currentUser: FirebaseUser)  : ViewModel() {
    private val _state = MutableStateFlow<ProfileScreenState>(ProfileScreenState.Loading)
    val state = _state.asStateFlow()
    val db = Firebase.firestore


    init {
        viewModelScope.launch{
            val currentUserDb = fetchUser(currentUser)
            _state.update {
                ProfileScreenState.Success(
                    currentUser = currentUserDb,
                )
            }
        }
    }

    private suspend fun fetchUser(currentUser: FirebaseUser): User {

        val uid = currentUser.uid
        val userDocRef = db.collection("users").whereEqualTo("UID", uid)

        return suspendCoroutine { continuation ->
            userDocRef.get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    Log.d("Warning", "entrou")
                    // Assuming that there will be only one document matching the Uid
                    val document = querySnapshot.documents.first()
                    val user = User(
                        id = document.reference.id,
                        Uid = document.getString("UID") ?: uid,
                        avatar = document.getString("avatar") ?: "",
                        name = document.getString("name") ?: "",
                        type = if (document.getString("type") == "FIXER") Type.FIXER else Type.USER
                    )
                    continuation.resume(user)
                } else {
                    // If no document matches the Uid, return a default User
                    val user = User(
                        id = uid,
                        Uid = uid,
                        avatar = "",
                        name = "",
                        type = Type.USER
                    )
                    continuation.resume(user)
                }
            }.addOnFailureListener { exception ->
                // Handle any errors and resume with a default User
                Log.e("ProfileViewModel", "Error fetching user", exception)
                val user = User(
                    id = uid,
                    Uid = uid,
                    avatar = "",
                    name = "",
                    type = Type.USER
                )
                continuation.resume(user)
            }
        }
    }
}
