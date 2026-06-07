package com.calculator.vault.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.calculator.vault.domain.model.VaultSessionState
import com.calculator.vault.presentation.calculator.CalculatorScreen
import com.calculator.vault.presentation.intruder.IntruderLogScreen
import com.calculator.vault.presentation.notes.SecureNotesScreen
import com.calculator.vault.presentation.premium.PremiumScreen
import com.calculator.vault.presentation.settings.SettingsScreen
import com.calculator.vault.presentation.setup.SetupScreen
import com.calculator.vault.presentation.vault.AddAppsScreen
import com.calculator.vault.presentation.vault.VaultDashboardScreen
import com.calculator.vault.presentation.vault.VaultSessionViewModel

@Composable
fun VaultNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = NavRoutes.CALCULATOR,
) {
    val navController = rememberNavController()
    VaultNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
    )
}

@Composable
fun VaultNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = NavRoutes.CALCULATOR,
    sessionViewModel: VaultSessionViewModel = hiltViewModel(),
) {
    val sessionState by sessionViewModel.sessionState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun navigateToCalculator() {
        navController.navigate(NavRoutes.CALCULATOR) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    LaunchedEffect(sessionState) {
        if (sessionState == VaultSessionState.LOCKED &&
            currentRoute != null &&
            currentRoute != NavRoutes.CALCULATOR &&
            currentRoute != NavRoutes.SETUP
        ) {
            navigateToCalculator()
        }
    }

    LaunchedEffect(Unit) {
        sessionViewModel.lockRequests.collect {
            navigateToCalculator()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(NavRoutes.CALCULATOR) {
            CalculatorScreen(
                onNavigateToSetup = {
                    navController.navigate(NavRoutes.SETUP) {
                        popUpTo(NavRoutes.CALCULATOR) { inclusive = true }
                    }
                },
                onNavigateToVault = { isFakeVault ->
                    navController.navigate(NavRoutes.vault(isFakeVault)) {
                        popUpTo(NavRoutes.CALCULATOR) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoutes.SETUP) {
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(NavRoutes.CALCULATOR) {
                        popUpTo(NavRoutes.SETUP) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = NavRoutes.VAULT,
            arguments = listOf(
                navArgument("isFakeVault") { type = NavType.BoolType },
            ),
        ) { backStackEntry ->
            val isFakeVault = backStackEntry.arguments?.getBoolean("isFakeVault") ?: false
            VaultDashboardScreen(
                isFakeVault = isFakeVault,
                onNavigateToAddApps = {
                    if (!isFakeVault) navController.navigate(NavRoutes.ADD_APPS)
                },
                onNavigateToSettings = {
                    if (!isFakeVault) navController.navigate(NavRoutes.SETTINGS)
                },
                onNavigateToIntruderLog = {
                    if (!isFakeVault) navController.navigate(NavRoutes.INTRUDER_LOG)
                },
                onNavigateToNotes = {
                    if (!isFakeVault) navController.navigate(NavRoutes.SECURE_NOTES)
                },
                onNavigateToPremium = {
                    if (!isFakeVault) navController.navigate(NavRoutes.PREMIUM)
                },
                onLockVault = { navigateToCalculator() },
            )
        }

        composable(NavRoutes.ADD_APPS) {
            AddAppsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.INTRUDER_LOG) {
            IntruderLogScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.SECURE_NOTES) {
            SecureNotesScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(NavRoutes.PREMIUM) {
            PremiumScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
