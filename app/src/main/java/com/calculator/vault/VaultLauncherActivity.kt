package com.calculator.vault

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.calculator.vault.presentation.launcher.LauncherAppItem
import com.calculator.vault.presentation.launcher.LauncherPackageChangeMonitor
import com.calculator.vault.presentation.launcher.VaultLauncherViewModel
import com.calculator.vault.presentation.testing.TestTags
import com.calculator.vault.presentation.theme.CalculatorVaultTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VaultLauncherActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorVaultTheme {
                VaultLauncherRoute(
                    onAppClick = { packageName ->
                        packageManager.getLaunchIntentForPackage(packageName)?.let { startActivity(it) }
                    },
                    onOpenCalculator = {
                        startActivity(
                            Intent(this, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                putExtra(MainActivity.EXTRA_FROM_LAUNCHER, true)
                            },
                        )
                    },
                )
            }
        }
    }

    companion object {
        /** Launches the home/launcher experience for tests or deep links. */
        fun createIntent(context: android.content.Context): Intent =
            Intent(context, VaultLauncherActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VaultLauncherRoute(
    onAppClick: (String) -> Unit,
    onOpenCalculator: () -> Unit,
    viewModel: VaultLauncherViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val packageMonitor = remember {
        LauncherPackageChangeMonitor { removed ->
            viewModel.onPackageChanged(removed)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    packageMonitor.register(context)
                    viewModel.refreshInstalledApps()
                }
                Lifecycle.Event.ON_PAUSE -> packageMonitor.unregister(context)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            packageMonitor.unregister(context)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    IconButton(
                        onClick = onOpenCalculator,
                        modifier = Modifier.testTag(TestTags.LAUNCHER_VAULT_LOCK),
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Open calculator vault")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.hiddenAppCount > 0) {
                Text(
                    text = uiState.hiddenAppsBanner,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag(TestTags.LAUNCHER_HIDDEN_BANNER),
                )
            }
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(80.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.testTag(TestTags.LAUNCHER_APP_GRID),
                ) {
                    items(uiState.visibleApps, key = { it.packageName }) { app ->
                        LauncherAppGridItem(app = app, onClick = { onAppClick(app.packageName) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LauncherAppGridItem(
    app: LauncherAppItem,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val icon = remember(app.packageName) {
        runCatching { context.packageManager.getApplicationIcon(app.packageName) }.getOrNull()
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
            .testTag(TestTags.launcherApp(app.packageName)),
    ) {
        if (icon != null) {
            Image(
                bitmap = icon.toBitmap(96, 96).asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier
                    .size(56.dp)
                    .padding(bottom = 4.dp),
            )
        } else {
            Box(modifier = Modifier.size(56.dp))
        }
        Text(
            text = app.label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}
