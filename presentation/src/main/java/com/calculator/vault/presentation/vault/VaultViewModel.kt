package com.calculator.vault.presentation.vault

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calculator.vault.domain.model.VaultApp
import com.calculator.vault.domain.usecase.LaunchVaultAppUseCase
import com.calculator.vault.domain.usecase.LockSessionUseCase
import com.calculator.vault.domain.usecase.ObserveFavoritesUseCase
import com.calculator.vault.domain.usecase.ObserveRecentAppsUseCase
import com.calculator.vault.domain.usecase.ObserveVaultAppsUseCase
import com.calculator.vault.domain.usecase.RefreshSessionUseCase
import com.calculator.vault.domain.usecase.RemoveAppFromVaultUseCase
import com.calculator.vault.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class VaultTab {
    HiddenApps,
    Favorites,
    Recent,
    Settings,
}

data class VaultUiState(
    val isFakeVault: Boolean = false,
    val selectedTab: VaultTab = VaultTab.HiddenApps,
    val searchQuery: String = "",
    val hiddenApps: List<VaultApp> = emptyList(),
    val favorites: List<VaultApp> = emptyList(),
    val recentApps: List<VaultApp> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class VaultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeVaultAppsUseCase: ObserveVaultAppsUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val observeRecentAppsUseCase: ObserveRecentAppsUseCase,
    private val launchVaultAppUseCase: LaunchVaultAppUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val removeAppFromVaultUseCase: RemoveAppFromVaultUseCase,
    private val lockSessionUseCase: LockSessionUseCase,
    private val refreshSessionUseCase: RefreshSessionUseCase,
) : ViewModel() {

    private val isFakeVault: Boolean = savedStateHandle.get<Boolean>("isFakeVault") ?: false

    private val _uiState = MutableStateFlow(VaultUiState(isFakeVault = isFakeVault))
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val favoritesFlow = if (isFakeVault) flowOf(emptyList()) else observeFavoritesUseCase()
            val recentFlow = if (isFakeVault) flowOf(emptyList()) else observeRecentAppsUseCase()
            combine(
                observeVaultAppsUseCase(isFakeVault),
                favoritesFlow,
                recentFlow,
            ) { hidden, favorites, recent ->
                Triple(hidden, favorites, recent)
            }.collect { (hidden, favorites, recent) ->
                _uiState.update {
                    it.copy(
                        hiddenApps = hidden,
                        favorites = favorites,
                        recentApps = recent,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onTabSelected(tab: VaultTab) {
        refreshActivity()
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onSearchQueryChange(query: String) {
        refreshActivity()
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun refreshActivity() {
        viewModelScope.launch { refreshSessionUseCase() }
    }

    fun launchApp(packageName: String) {
        if (isFakeVault) return
        viewModelScope.launch {
            launchVaultAppUseCase(packageName)
        }
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(packageName)
        }
    }

    fun removeApp(packageName: String) {
        viewModelScope.launch {
            removeAppFromVaultUseCase(packageName)
        }
    }

    fun lockVault(onLocked: () -> Unit) {
        viewModelScope.launch {
            lockSessionUseCase()
            onLocked()
        }
    }

    fun appsForCurrentTab(): List<VaultApp> {
        val state = _uiState.value
        val source = when (state.selectedTab) {
            VaultTab.HiddenApps -> state.hiddenApps
            VaultTab.Favorites -> state.favorites
            VaultTab.Recent -> state.recentApps
            VaultTab.Settings -> emptyList()
        }
        val query = state.searchQuery.trim().lowercase()
        if (query.isEmpty()) return source
        return source.filter {
            it.appName.lowercase().contains(query) ||
                it.packageName.lowercase().contains(query)
        }
    }
}
