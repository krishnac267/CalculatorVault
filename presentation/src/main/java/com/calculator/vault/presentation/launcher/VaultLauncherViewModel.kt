package com.calculator.vault.presentation.launcher

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculator.vault.domain.launcher.LauncherModeMessages
import com.calculator.vault.domain.launcher.LauncherVisibilityFilter
import com.calculator.vault.domain.usecase.GetInstalledAppsUseCase
import com.calculator.vault.domain.usecase.ObserveVaultAppsUseCase
import com.calculator.vault.domain.usecase.RemoveAppFromVaultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LauncherAppItem(
    val packageName: String,
    val label: String,
)

data class VaultLauncherUiState(
    val visibleApps: List<LauncherAppItem> = emptyList(),
    val hiddenAppCount: Int = 0,
    val hiddenAppsBanner: String = "",
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VaultLauncherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeVaultAppsUseCase: ObserveVaultAppsUseCase,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val removeAppFromVaultUseCase: RemoveAppFromVaultUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultLauncherUiState())
    val uiState: StateFlow<VaultLauncherUiState> = _uiState.asStateFlow()

    private val installedAppsRefreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    init {
        viewModelScope.launch {
            combine(
                observeVaultAppsUseCase(isFakeVault = false),
                installedAppsRefreshTrigger.flatMapLatest {
                    flow {
                        emit(loadInstalledApps())
                    }
                },
            ) { vaultApps, installed ->
                val hidden = vaultApps.map { it.packageName }.toSet()
                val visiblePackages = LauncherVisibilityFilter.filterVisiblePackages(
                    installedPackages = installed.map { it.packageName },
                    hiddenVaultPackages = hidden,
                    ownPackage = context.packageName,
                )
                val visibleByPackage = installed.associateBy { it.packageName }
                val count = hidden.size
                VaultLauncherUiState(
                    visibleApps = visiblePackages.mapNotNull { visibleByPackage[it] },
                    hiddenAppCount = count,
                    hiddenAppsBanner = LauncherModeMessages.hiddenAppsBanner(count),
                    isLoading = false,
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            installedAppsRefreshTrigger.emit(Unit)
        }
    }

    fun onPackageChanged(removedPackage: String?) {
        viewModelScope.launch {
            if (removedPackage != null) {
                removeAppFromVaultUseCase(removedPackage)
            }
            installedAppsRefreshTrigger.emit(Unit)
        }
    }

    private suspend fun loadInstalledApps(): List<LauncherAppItem> =
        withContext(Dispatchers.Default) {
            getInstalledAppsUseCase().map { app ->
                LauncherAppItem(packageName = app.packageName, label = app.appName)
            }
        }
}
