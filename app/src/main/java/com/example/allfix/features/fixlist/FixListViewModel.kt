package com.example.allfix.features.fixdetails

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
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

sealed class FixListScreenState {
    data object Loading : FixListScreenState()
    data class Success(
        val currentUser: User,
        val searchText: String = "",
        val fixes: List<Fixes> = emptyList(),
    ) : FixListScreenState()
}

class Fixes(
    var id: String = "",
    var problem: String = "",
    var location: String = "",
    var price: Float = 0F,
    var creator: User = User(),
    var desc: String = "",
    var images: List<String> = emptyList(),
    var date: String = "",
    var fixer: User? = null,
    var state: State = State.NEWER
)

class User(
    val id: String = "",
    val Uid: String = "",
    val avatar: String = "",
    val name: String = "",
    val type: Type = Type.USER,
)

enum class Type{ FIXER, USER }

enum class State{NEWER, TODO, INPROGRESS, DONE}

class FixListViewModelFactory(private val currentUser: FirebaseUser) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FixListViewModel(currentUser) as T
    }
}

class FixListViewModel(currentUser: FirebaseUser) : ViewModel(){
    private val _state = MutableStateFlow<FixListScreenState>(FixListScreenState.Loading)
    val state = _state.asStateFlow()
    val db = Firebase.firestore


    init{
        viewModelScope.launch{
            val fixes = fetchFixes()
            val currentUserDb = fetchUser(currentUser)
            _state.update {
                FixListScreenState.Success(
                    currentUser = currentUserDb,
                    fixes = fixes,
                    searchText = ""
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

    private suspend fun fetchFixes(): List<Fixes> {
        var data =  db.collection("fixes").whereEqualTo("state", State.NEWER).get().await()

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


            val fixerReference = document.getDocumentReference("fixer")
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