package com.example.allfix.features.fixlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val problem: String,
    val location: String,
    val price: Float,
    val user: User,
    val descricao: String,
    val images: List<String> = emptyList()
)

class User(
    val id: String,
    val avatar: String,
    val name: String,
    val type: Type,
)

enum class Type{ FIXER, USER }

class FixListViewModel : ViewModel(){
    private val _state = MutableStateFlow<FixListScreenState>(FixListScreenState.Loading)
    val state = _state.asStateFlow()



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

    private fun fetchFixes(): List<Fixes> {
        return listOf(Fixes(location = "valongo", price = 22.00F, user = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), descricao = "teste teste teste test", problem = "Problema de juntas"),
            Fixes(location = "valongo", price = 22.00F, user = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), descricao = "teste teste teste test", problem = "Problema de juntas"),
            Fixes(location = "valongo", price = 22.00F, user = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), descricao = "teste teste teste test", problem = "Problema de juntas"),
            Fixes(location = "valongo", price = 22.00F, user = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), descricao = "teste teste teste test", problem = "Problema de juntas"),
            Fixes(location = "valongo", price = 22.00F, user = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), descricao = "teste teste teste test", problem = "Problema de juntas"),
            Fixes(location = "valongo", price = 22.00F, user = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), descricao = "teste teste teste test", problem = "Problema de juntas"),
            Fixes(location = "valongo", price = 22.00F, user = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), descricao = "teste teste teste test", problem = "Problema de juntas"),
            Fixes(location = "valongo", price = 22.00F, user = User(id = "123", name = "henrique", type = Type.USER, avatar = "teste"), descricao = "teste teste teste test", problem = "Problema de juntas"))
    }

    private fun fetchUser(): User {
        return User("1234", "test", "test", Type.USER)
    }


}