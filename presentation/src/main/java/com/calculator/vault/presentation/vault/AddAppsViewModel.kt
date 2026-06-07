package com.calculator.vault.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculator.vault.domain.model.InstalledApp
import com.calculator.vault.domain.model.VaultApp
import com.calculator.vault.domain.usecase.AddAppToVaultUseCase
import com.calculator.vault.domain.usecase.GetInstalledAppsUseCase
import com.calculator.vault.domain.usecase.ObservePremiumStatusUseCase
import com.calculator.vault.domain.usecase.ObserveVaultAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddAppsUiState(
    val installedApps: List<InstalledApp> = emptyList(),
    val vaultPackageNames: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val addedPackageName: String? = null,
    val isPremium: Boolean = false,
    val limitMessage: String? = null,
)

@HiltViewModel
class AddAppsViewModel @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val addAppToVaultUseCase: AddAppToVaultUseCase,
    private val observeVaultAppsUseCase: ObserveVaultAppsUseCase,
    private val observePremiumStatusUseCase: ObservePremiumStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAppsUiState())
    val uiState: StateFlow<AddAppsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeVaultAppsUseCase(isFakeVault = false).collect { vaultApps ->
                _uiState.update {
                    it.copy(vaultPackageNames = vaultApps.map { app -> app.packageName }.toSet())
                }
            }
        }
        viewModelScope.launch {
            observePremiumStatusUseCase().collect { status ->
                _uiState.update { it.copy(isPremium = status.isPremium) }
            }
        }
        loadApps()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val apps = getInstalledAppsUseCase()
            _uiState.update { it.copy(installedApps = apps, isLoading = false) }
        }
    }

    fun addApp(app: InstalledApp, freeLimit: Int = 3) {
        viewModelScope.launch {
            when (addAppToVaultUseCase(VaultApp(packageName = app.packageName, appName = app.appName), freeLimit)) {
                AddAppToVaultUseCase.Result.Added -> {
                    _uiState.update {
                        it.copy(addedPackageName = app.packageName, limitMessage = null)
                    }
                }
                AddAppToVaultUseCase.Result.LimitReached -> {
                    _uiState.update {
                        it.copy(
                            limitMessage = "Free plan supports up to $freeLimit protected apps. Upgrade to Premium for unlimited storage.",
                        )
                    }
                }
            }
        }
    }

    fun filteredApps(): List<InstalledApp> {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()
        return state.installedApps.filter { app ->
            (query.isEmpty() ||
                app.appName.lowercase().contains(query) ||
                app.packageName.lowercase().contains(query)) &&
                app.packageName !in state.vaultPackageNames
        }
    }

    fun isInVault(packageName: String): Boolean =
        packageName in _uiState.value.vaultPackageNames
}
