package com.diary.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diary.DiaryApplication
import com.diary.MainActivity
import com.diary.data.DiaryEntry
import com.diary.ui.screen.*
import com.diary.viewmodel.DiaryViewModel
import com.diary.viewmodel.DiaryViewModelFactory

@Composable
fun DiaryNavHost(activity: MainActivity) {
    val navController = rememberNavController()

    val startDestination = if (DiaryApplication.activeProvider != null) "entries" else "provider"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("provider") {
            ProviderScreen(
                activity = activity,
                onAuthenticated = {
                    navController.navigate("entries") {
                        popUpTo("provider") { inclusive = true }
                    }
                }
            )
        }

        composable("entries") {
            val repo = DiaryApplication.repository ?: run {
                navController.navigate("provider") { popUpTo("entries") { inclusive = true } }
                return@composable
            }
            val vm: DiaryViewModel = viewModel(factory = DiaryViewModelFactory(repo))
            EntryListScreen(
                vm = vm,
                onEntryClick = { entry ->
                    navController.navigate("view/${entry.year}/${entry.month}/${entry.filename}")
                },
                onNewEntry = { navController.navigate("new") },
                onLogout = {
                    DiaryApplication.activeProvider = null
                    DiaryApplication.repository = null
                    DiaryApplication.instance.clearDropboxToken()
                    navController.navigate("provider") {
                        popUpTo("entries") { inclusive = true }
                    }
                }
            )
        }

        composable(
            "view/{year}/{month}/{filename}",
            arguments = listOf(
                navArgument("year") { type = NavType.StringType },
                navArgument("month") { type = NavType.StringType },
                navArgument("filename") { type = NavType.StringType },
            )
        ) { backStack ->
            val repo = DiaryApplication.repository ?: return@composable
            val vm: DiaryViewModel = viewModel(factory = DiaryViewModelFactory(repo))
            val year = backStack.arguments?.getString("year") ?: return@composable
            val month = backStack.arguments?.getString("month") ?: return@composable
            val filename = backStack.arguments?.getString("filename") ?: return@composable
            val day = filename.removePrefix("entry-").substringBefore("-")
            EntryViewScreen(
                vm = vm,
                entry = DiaryEntry(year, month, day, filename),
                onBack = { navController.popBackStack() }
            )
        }

        composable("new") {
            val repo = DiaryApplication.repository ?: return@composable
            val vm: DiaryViewModel = viewModel(factory = DiaryViewModelFactory(repo))
            NewEntryScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}
