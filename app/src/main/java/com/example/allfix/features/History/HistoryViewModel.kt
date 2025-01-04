package com.example.allfix.features.History

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.allfix.features.fixdetails.Fixes
import com.example.allfix.features.fixdetails.State
import com.example.allfix.features.fixdetails.Type
import com.example.allfix.features.fixdetails.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random
import com.google.firebase.auth.FirebaseUser
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class HistoryScreenState {
    data object Loading : HistoryScreenState()
    data class Success(
        val currentUser: User,
        val fixes: List<Fixes> = emptyList(),
    ) : HistoryScreenState()
}

class HistoryViewModelFactory(private val currentUser: FirebaseUser) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoryViewModel(currentUser) as T
    }
}

class HistoryViewModel(currentUser: FirebaseUser) : ViewModel(){
    private val _state = MutableStateFlow<HistoryScreenState>(HistoryScreenState.Loading)
    val state = _state.asStateFlow()
    val db = Firebase.firestore


    init{
        viewModelScope.launch{
            val currentUserDb = fetchUser(currentUser)
            val fixes = fetchFixes(currentUserDb)

            _state.update {
                HistoryScreenState.Success(
                    currentUser = currentUserDb,
                    fixes = fixes,
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

    private suspend fun fetchFixes(currentUser: User): List<Fixes> {
        val userRef = db.collection("users").document(currentUser.id)

        val query = if (currentUser.type == Type.FIXER) {
            // If the user is a fixer, fetch the fixes where the 'fixer' field matches the currentUser
            db.collection("fixes")
                .whereEqualTo("fixer", userRef).whereEqualTo("state", State.DONE)
                .get()
                .await()
        } else {
            // If the user is a regular user, fetch the fixes where the 'creator' field matches the currentUser
            db.collection("fixes")
                .whereEqualTo("creator", userRef).whereEqualTo("state", State.DONE)
                .get()
                .await()
        }

        val fixesList = query.documents.map { document ->
            // Assuming you also have a nested "user" field that is a map of User data
            val userReference = document.getDocumentReference("creator")
            var user : User = User()  // Default empty user object
            Log.d("Warning","nada")
            // If user reference is not null, fetch the user data
            if (userReference != null) {
                try {
                    val userSnapshot = userReference.get().await()
                    if (userSnapshot.exists()) {
                        // Extract user data from the user document
                        user = User(
                            id = document.reference.id,
                            Uid = document.getString("UID") ?: "",
                            avatar = document.getString("avatar") ?: "",
                            name = document.getString("name") ?: "",
                            type = Type.USER)
                    }
                } catch (e: Exception) {
                    // Handle the error if fetching the user data fails
                    println("Error fetching user data: ${e.message}")
                }
            }


            val fixerReference = document.getDocumentReference("fixer")
            var fixer : User? = null  // Default empty user object

            // If user reference is not null, fetch the user data
            if (fixerReference != null) {
                try {
                    val userSnapshot = fixerReference.get().await()
                    if (userSnapshot.exists()) {
                        // Extract user data from the user document
                        fixer = User(
                            id = document.reference.id,
                            Uid = document.getString("UID") ?: "",
                            avatar = document.getString("avatar") ?: "",
                            name = document.getString("name") ?: "",
                            type = Type.FIXER)
                    }
                } catch (e: Exception) {
                    // Handle the error if fetching the user data fails
                    println("Error fetching user data: ${e.message}")
                }
            }

            // Create the Fixes object
            Fixes(
                id = document.id,
                location = document.getString("location").toString() ?: "",
                price = document.getDouble("price")?.toFloat() ?: 0.0F,
                creator = user,
                desc = document.getString("desc").toString() ?: "",
                problem = document.getString("problem").toString() ?: "",
                date = document.getTimestamp("Date")?.toDate().toString() ?: "",
                fixer = fixer,
                state = State.valueOf(document.getString("state").toString()) ?: State.NEWER
            )
        }
        // Return the list of Fixes objects
        return fixesList
    }
}