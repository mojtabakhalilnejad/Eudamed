package com.openregulatory.eudamedsearch.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openregulatory.eudamedsearch.ui.detail.DeviceDetailScreen
import com.openregulatory.eudamedsearch.ui.search.SearchScreen
import com.openregulatory.eudamedsearch.ui.search.SearchViewModel
import com.openregulatory.eudamedsearch.ui.search.SettingsScreen

private object Routes {
    const val SEARCH = "search"
    const val DETAIL = "detail"
    const val SETTINGS = "settings"
}

@Composable
fun EudamedNavGraph(navController: NavHostController = rememberNavController()) {
    // A single SearchViewModel instance shared across all destinations (Compose's default
    // viewModel() resolves to the hosting Activity's ViewModelStore, so every call below returns
    // the same instance), which keeps filters, results and the selected device in sync as the
    // user moves between screens without re-fetching anything.
    NavHost(navController = navController, startDestination = Routes.SEARCH) {
        composable(Routes.SEARCH) {
            val sharedViewModel: SearchViewModel = viewModel()
            SearchScreen(
                viewModel = sharedViewModel,
                onDeviceClick = { device ->
                    sharedViewModel.selectDevice(device)
                    navController.navigate(Routes.DETAIL)
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.DETAIL) {
            val sharedViewModel: SearchViewModel = viewModel()
            val selected by sharedViewModel.selectedDevice.collectAsState()
            selected?.let { device ->
                DeviceDetailScreen(
                    device = device,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.SETTINGS) {
            val sharedViewModel: SearchViewModel = viewModel()
            SettingsScreen(
                viewModel = sharedViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
