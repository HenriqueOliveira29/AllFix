import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.allfix.features.Login.LoginScreen
import com.example.allfix.features.fixdetails.FixDetailScreen
import com.example.allfix.features.fixdetails.FixDetailViewModel
import com.example.allfix.features.fixdetails.FixDetailViewModelFactory
import com.example.allfix.features.fixdetails.FixListScreen
import com.example.allfix.features.fixdetails.FixListViewModel
import com.example.allfix.features.fixdetails.FixListViewModelFactory
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.auth.User

sealed class Routes(val route: String, val icon: ImageVector, val title: String, bottomBar: Boolean) {
    object Home : Routes(route = "Home", icon = Icons.Default.Home, title = "Home", bottomBar = true)
    object Details : Routes(route = "Details", icon = Icons.Default.Details,title = "Details", bottomBar = true)
    object Create : Routes(route = "Create", icon = Icons.Default.Add, title = "Create", bottomBar = true)
    object History : Routes(route = "History", icon = Icons.Default.History, title = "History", bottomBar = true)
    object Profile : Routes(route = "Profile", icon = Icons.Default.Person, title = "Profile", bottomBar = true)
    object Login : Routes(route = "Login", icon = Icons.Default.Login, title = "Login", bottomBar = false)

    companion object {
        val toList = listOf(Home,History, Profile)
    }
}

@Composable
fun AppNavigation(navController: NavHostController, currentUser: FirebaseUser?) {
    NavHost(navController, startDestination = Routes.Login.route) {
        composable(Routes.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Routes.Home.route) {
            val fixListViewModel: FixListViewModel = viewModel(
                factory = FixListViewModelFactory(currentUser!!)
            )
            val state by fixListViewModel.state.collectAsState()

            FixListScreen(state = state, navController)
        }
        //composable(Routes.Create.route) {
          //  Ecra01(registros = registros, navController = navController)
        //}
        //composable(Routes.Details.route) {
            //Ecra02(registros = registros, navController = navController)
        //}
        composable(
            "Details/{selectedFix}",
            arguments = listOf(navArgument("selectedFix") { type = NavType.StringType })
        ) { backStackEntry ->
            val selectedFix = backStackEntry.arguments?.getString("selectedFix") ?: ""

            val viewModel: FixDetailViewModel = viewModel(
                factory = FixDetailViewModelFactory(fixId = selectedFix)
            )
            val state by viewModel.state.collectAsState()

            // Pass the state to the FixDetailScreen Composable
            FixDetailScreen(state = state, navController)
        }
    }
}
