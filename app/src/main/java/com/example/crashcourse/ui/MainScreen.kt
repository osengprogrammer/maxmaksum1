package com.example.crashcourse.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.crashcourse.db.FaceEntity
import com.example.crashcourse.navigation.Screen
import com.example.crashcourse.ui.add.AddUserScreen
import com.example.crashcourse.ui.add.BulkRegistrationScreen
import com.example.crashcourse.ui.checkin.CheckInScreen
import com.example.crashcourse.ui.edit.EditUserScreen
import com.example.crashcourse.ui.face_list.FaceListScreen
import com.example.crashcourse.viewmodel.FaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var useBackCamera by remember { mutableStateOf(false) }

    // Shared ViewModel (used where needed)
    val sharedFaceViewModel: FaceViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNav(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.RegistrationMenu.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable(Screen.CheckIn.route) {
                CheckInScreen(useBackCamera = useBackCamera)
            }

            composable(Screen.RegistrationMenu.route) {
                RegistrationMenuScreen(
                    onNavigateToBulkRegister = {
                        navController.navigate(Screen.BulkRegister.route)
                    },
                    onNavigateToAddUser = {
                        navController.navigate(Screen.AddUser.route)
                    }
                )
            }

            composable(Screen.AddUser.route) {
                AddUserScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onUserAdded = {}
                )
            }

            // ✅ FIXED: Bulk Registration (NO parameters)
            composable(Screen.BulkRegister.route) {
                BulkRegistrationScreen()
            }

            // Face Management screen
            composable(Screen.Manage.route) {
                FaceListScreen(
                    onEditUser = { face: FaceEntity ->
                        navController.navigate(
                            Screen.EditUser.createRoute(face.studentId)
                        )
                    }
                )
            }

            // Edit user screen
            composable(
                route = Screen.EditUser.route,
                arguments = listOf(
                    navArgument("studentId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val studentId =
                    backStackEntry.arguments?.getString("studentId") ?: return@composable

                EditUserScreen(
                    studentId = studentId,
                    onNavigateBack = { navController.popBackStack() },
                    onUserUpdated = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Options.route) {
                OptionsManagementScreen(
                    onNavigateToForm = { type ->
                        navController.navigate(
                            Screen.OptionForm.createRoute(type)
                        )
                    }
                )
            }

            composable(
                route = Screen.OptionForm.route,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val type =
                    backStackEntry.arguments?.getString("type") ?: return@composable

                OptionFormScreen(
                    type = type,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CheckInRecord.route) {
                CheckInRecordScreen()
            }

            composable(Screen.Debug.route) {
                DebugScreen(viewModel = sharedFaceViewModel)
            }
        }
    }
}

@Composable
fun BottomNav(navController: NavHostController) {
    val items = listOf(
        Screen.CheckIn to "Check In",
        Screen.RegistrationMenu to "Register",
        Screen.Manage to "Manage",
        Screen.Options to "Options"
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { (screen, label) ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.CheckIn -> Icons.Default.Person
                            Screen.RegistrationMenu -> Icons.Default.PersonAdd
                            Screen.Manage -> Icons.AutoMirrored.Filled.List
                            Screen.Options -> Icons.Default.Settings
                            else -> Icons.Default.Person
                        },
                        contentDescription = label
                    )
                },
                label = { Text(label) }
            )
        }
    }
}
