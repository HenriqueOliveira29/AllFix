package com.example.allfix.features.fixdetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class FixDetailScreenState {
    data object Loading : FixDetailScreenState()
    data class Success(
        val fix: Fixes,
        val currentUser: User
    ) : FixDetailScreenState()
    data class Error(
        val message: String
    ): FixDetailScreenState()
}

class FixDetailViewModelFactory(private val fixId: String, private val currentUser: FirebaseUser) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FixDetailViewModel(fixId, currentUser) as T
    }
}

class FixDetailViewModel(private var fixId: String, currentUser: FirebaseUser)  : ViewModel() {
    private val _state = MutableStateFlow<FixDetailScreenState>(FixDetailScreenState.Loading)
    val state = _state.asStateFlow()
    val db = Firebase.firestore
    var fix = Fixes()
    var currentUserDb = User()


    init {
        viewModelScope.launch {
            try {
                fix = fetchFix(fixId)
                currentUserDb = fetchUser(currentUser)
                _state.update {
                    FixDetailScreenState.Success(
                        currentUser = currentUserDb,
                        fix = fix,
                    )
                }
            } catch (e: Exception) {
                _state.update { FixDetailScreenState.Error("Error initializing fix details: ${e.message}") }
            }
        }
    }

    private suspend fun fetchFix(fixId: String): Fixes {
        try {
            val document = db.collection("fixes").document(fixId).get().await()

            // If document does not exist, throw an error
            if (!document.exists()) {
                throw Exception("Fix with id $fixId not found")
            }

            val userReference = document.getDocumentReference("creator")
            var user: User = User() // Default empty user object

            // If user reference is not null, fetch the user data
            userReference?.let {
                val userSnapshot = it.get().await()
                if (userSnapshot.exists()) {
                    user = User(
                        id = userSnapshot.reference.id,
                        Uid = userSnapshot.getString("UID") ?: "",
                        avatar = userSnapshot.getString("avatar") ?: "",
                        name = userSnapshot.getString("name") ?: "",
                        type = if (userSnapshot.getString("type") == "FIXER") Type.FIXER else Type.USER,
                    )
                }
            }

            val fixerReference = document.getDocumentReference("fixer")
            var fixer: User? = null // Default empty user object

            // If fixer reference is not null, fetch the fixer data
            fixerReference?.let {
                val fixerSnapshot = it.get().await()
                if (fixerSnapshot.exists()) {
                    fixer = User(
                        id = fixerSnapshot.reference.id,
                        Uid = fixerSnapshot.getString("UID") ?: "",
                        avatar = fixerSnapshot.getString("avatar") ?: "",
                        name = fixerSnapshot.getString("name") ?: "",
                        type = if (fixerSnapshot.getString("type") == "FIXER") Type.FIXER else Type.USER,
                    )
                }
            }

            // Create and return Fixes object
            Log.d("Warning", "teste user ${user.name}")
            return Fixes(
                id = document.id,
                location = document.getString("location").orEmpty(),
                price = document.getDouble("price")?.toFloat() ?: 0.0F,
                creator = user,
                desc = document.getString("desc").orEmpty(),
                problem = document.getString("problem").orEmpty(),
                date = document.getTimestamp("date")?.toDate()?.toGMTString() ?: "",
                fixer = fixer,
                state = State.valueOf(document.getString("state") ?: State.NEWER.name)
            )

        } catch (e: Exception) {
            throw Exception("Error fetching fix details: ${e.message}")
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
                        type = if (document.getString("type") == "FIXER") Type.FIXER else Type.USER,
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

    fun updateFix(updatedFix: Fixes, newFixer: User?) {
        // Set state to loading before updating
        _state.update { FixDetailScreenState.Loading }

        viewModelScope.launch {
            try {
                // Get reference to the current fix document
                val fixRef = db.collection("fixes").document(fixId)

                // Update the fixer field by referencing the new fixer document
                fixRef.update(
                    "location", updatedFix.location,
                    "price", updatedFix.price,
                    "description", updatedFix.desc,
                    "problem", updatedFix.problem,
                    "state", updatedFix.state.name,
                    "fixer", if (newFixer != null) db.collection("users").document(newFixer.id) else null  // Assuming 'newFixer' is a User object
                ).await()

                // Fetch the updated fix data after the update
                fix = fetchFix(fixId)

                _state.update {
                    FixDetailScreenState.Success(
                        currentUser = currentUserDb,
                        fix = fix,
                    )
                }

            } catch (e: Exception) {
                // Handle error during update and update state accordingly
                _state.update { FixDetailScreenState.Error("Error updating fix: ${e.message}") }
            }
        }
    }
}
