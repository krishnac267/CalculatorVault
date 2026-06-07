package com.calculator.vault.presentation.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.domain.model.VaultApp
import com.calculator.vault.presentation.components.AppIcon
import com.calculator.vault.presentation.components.GlassCard
import com.calculator.vault.presentation.components.SecureScreenEffect
import com.calculator.vault.presentation.theme.VaultBackgroundGradientEnd
import com.calculator.vault.presentation.theme.VaultBackgroundGradientStart
import com.calculator.vault.presentation.theme.VaultTextSecondary
import com.calculator.vault.presentation.testing.TestTags
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultDashboardScreen(
    isFakeVault: Boolean,
    onNavigateToAddApps: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToIntruderLog: () -> Unit,
    onLockVault: () -> Unit,
    viewModel: VaultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchExpanded by rememberSaveable { mutableStateOf(false) }

    SecureScreenEffect()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(VaultBackgroundGradientStart, VaultBackgroundGradientEnd),
                ),
            ),
    ) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isFakeVault) "My Apps" else "Vault",
                            modifier = Modifier.testTag(TestTags.VAULT_TITLE),
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.lockVault(onLockVault) },
                            modifier = Modifier.testTag(TestTags.VAULT_LOCK),
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock vault")
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            ) {
                val visibleTabs = if (isFakeVault) {
                    listOf(VaultTab.HiddenApps, VaultTab.Settings)
                } else {
                    VaultTab.entries.toList()
                }
                TabRow(selectedTabIndex = visibleTabs.indexOf(uiState.selectedTab).coerceAtLeast(0)) {
                    visibleTabs.forEach { tab ->
                        Tab(
                            selected = uiState.selectedTab == tab,
                            onClick = { viewModel.onTabSelected(tab) },
                            modifier = Modifier.testTag(tab.testTag()),
                            text = { Text(text = tab.label()) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (uiState.selectedTab) {
                    VaultTab.Settings -> VaultSettingsTab(
                        isFakeVault = isFakeVault,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToAddApps = onNavigateToAddApps,
                        onNavigateToIntruderLog = onNavigateToIntruderLog,
                        onLockVault = { viewModel.lockVault(onLockVault) },
                    )
                    else -> {
                        SearchBar(
                            expanded = searchExpanded,
                            onExpandedChange = { searchExpanded = it },
                            inputField = {
                                androidx.compose.material3.SearchBarDefaults.InputField(
                                    query = uiState.searchQuery,
                                    onQueryChange = viewModel::onSearchQueryChange,
                                    onSearch = { searchExpanded = false },
                                    expanded = searchExpanded,
                                    onExpandedChange = { searchExpanded = it },
                                    placeholder = { Text("Search apps") },
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {}

                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            val apps = viewModel.appsForCurrentTab()
                            if (apps.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("No apps yet", color = VaultTextSecondary, modifier = Modifier.testTag(TestTags.VAULT_EMPTY))
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(apps, key = { it.packageName }) { app ->
                                        VaultAppCard(
                                            app = app,
                                            onLaunch = { viewModel.launchApp(app.packageName) },
                                            onToggleFavorite = {
                                                viewModel.toggleFavorite(app.packageName)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultAppCard(
    app: VaultApp,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppIcon(packageName = app.packageName, size = 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.appName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = formatLastOpened(app.lastOpenedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = VaultTextSecondary,
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (app.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle favorite",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onLaunch) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Launch app")
            }
        }
    }
}

@Composable
private fun VaultSettingsTab(
    isFakeVault: Boolean,
    onNavigateToSettings: () -> Unit,
    onNavigateToAddApps: () -> Unit,
    onNavigateToIntruderLog: () -> Unit,
    onLockVault: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (isFakeVault) "Decoy vault mode" else "Real vault mode",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isFakeVault) {
                    "Showing decoy content. Wrong PIN entries won't reveal hidden apps."
                } else {
                    "Your hidden apps are stored securely behind the calculator PIN."
                },
                color = VaultTextSecondary,
            )
        }

        Button(
            onClick = onNavigateToAddApps,
            modifier = Modifier.fillMaxWidth().testTag(TestTags.VAULT_ADD_APPS),
            enabled = !isFakeVault,
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add apps to vault", modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.fillMaxWidth().testTag(TestTags.VAULT_SECURITY_SETTINGS),
            enabled = !isFakeVault,
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Text("Security settings", modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedButton(onClick = onNavigateToIntruderLog, modifier = Modifier.fillMaxWidth(), enabled = !isFakeVault) {
            Text("Intruder log")
        }
        OutlinedButton(onClick = onLockVault, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Text("Lock vault", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

private fun VaultTab.label(): String = when (this) {
    VaultTab.HiddenApps -> "Hidden"
    VaultTab.Favorites -> "Favorites"
    VaultTab.Recent -> "Recent"
    VaultTab.Settings -> "Settings"
}

private fun VaultTab.testTag(): String = when (this) {
    VaultTab.HiddenApps -> TestTags.VAULT_TAB_HIDDEN
    VaultTab.Favorites -> TestTags.VAULT_TAB_FAVORITES
    VaultTab.Recent -> TestTags.VAULT_TAB_RECENT
    VaultTab.Settings -> TestTags.VAULT_TAB_SETTINGS
}

private fun formatLastOpened(timestamp: Long?): String {
    if (timestamp == null) return "Never opened"
    return "Last opened: ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        .format(Date(timestamp))}"
}
