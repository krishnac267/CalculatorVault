package com.calculator.vault.presentation.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculator.vault.domain.usecase.ObserveVaultAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LauncherAppUi(
    val packageName: String,
    val label: String,
    val icon: Drawable,
)

data class VaultLauncherUiState(
    val visibleApps: List<LauncherAppUi> = emptyList(),
)

@HiltViewModel
class VaultLauncherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    observeVaultAppsUseCase: ObserveVaultAppsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultLauncherUiState())
    val uiState: StateFlow<VaultLauncherUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeVaultAppsUseCase(isFakeVault = false),
                kotlinx.coroutines.flow.flow { emit(loadInstalledApps()) },
            ) { vaultApps, installed ->
                val hidden = vaultApps.map { it.packageName }.toSet()
                installed.filter { it.packageName !in hidden }
            }.collect { visible ->
                _uiState.update { it.copy(visibleApps = visible) }
            }
        }
    }

    private fun loadInstalledApps(): List<LauncherAppUi> {
        val pm = context.packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val ourPackage = context.packageName
        return pm.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }
            .distinct()
            .filter { it != ourPackage }
            .mapNotNull { pkg ->
                try {
                    val info = pm.getApplicationInfo(pkg, 0)
                    LauncherAppUi(
                        packageName = pkg,
                        label = pm.getApplicationLabel(info).toString(),
                        icon = pm.getApplicationIcon(info),
                    )
                } catch (_: PackageManager.NameNotFoundException) {
                    null
                }
            }
            .sortedBy { it.label.lowercase() }
    }
}
