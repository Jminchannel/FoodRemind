package com.jmin.foodremind.ui.navigation

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.jmin.foodremind.R
import com.jmin.foodremind.ui.components.LanguageDialog
import com.jmin.foodremind.ui.screens.*
import com.jmin.foodremind.ui.viewmodel.FoodViewModel
import com.jmin.foodremind.utils.LocaleManager
import android.content.Intent
import com.jmin.foodremind.MainActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(viewModel: FoodViewModel) {
    val navController = rememberNavController()
    var showRecordDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val navItems = listOf(
        NavigationItem(Screen.Home.route, R.string.nav_home, Icons.Filled.Home),
        NavigationItem(Screen.FoodPicker.route, R.string.nav_food_picker, Icons.Filled.Refresh),
        NavigationItem(Screen.History.route, R.string.nav_history, Icons.Filled.List),
        NavigationItem(Screen.Seasonal.route, R.string.nav_seasonal, Icons.Filled.Star),
        NavigationItem(Screen.Settings.route, R.string.nav_settings, Icons.Filled.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (showLanguageDialog) {
        LanguageDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { languageCode ->
                LocaleManager.setLocale(context, languageCode)
                showLanguageDialog = false
                // Restart the activity to apply the new locale
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleResId = navItems.find { it.route == currentRoute }?.titleResId ?: R.string.app_name
                    Text(stringResource(id = titleResId))
                },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.top_bar_back_button)
                            )
                        }
                    }
                },
                actions = {
                    if (currentRoute != Screen.Settings.route) {
                        IconButton(onClick = { showLanguageDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = stringResource(R.string.language)
                            )
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.nav_settings)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val currentDestination = navBackStackEntry?.destination
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = {
                            Text(
                                text = stringResource(id = item.titleResId),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onFoodPickerClick = {
                        navController.navigate(Screen.FoodPicker.route)
                    },
                    onManualRecordClick = {
                        showRecordDialog = !showRecordDialog
                    },
                    showRecordDialog = showRecordDialog
                )
            }
            composable(Screen.FoodPicker.route) { FoodPickerScreen(viewModel = viewModel) }
            composable(Screen.History.route) { HistoryScreen(viewModel = viewModel) }
            composable(Screen.Seasonal.route) { SeasonalScreen(viewModel = viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel = viewModel, navController = navController) }
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FoodPicker : Screen("food_picker")
    object History : Screen("history")
    object Seasonal : Screen("seasonal")
    object Settings : Screen("settings")
}

data class NavigationItem(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) 