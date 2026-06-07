package com.calculator.vault.presentation.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.calculator.vault.presentation.components.GlassCard
import com.calculator.vault.presentation.components.SecureScreenEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    onPurchaseMonthly: () -> Unit = {},
    onPurchaseYearly: () -> Unit = {},
    onPurchaseLifetime: () -> Unit = {},
) {
    SecureScreenEffect()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator Vault Premium") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Premium Privacy Space", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Apps added to the vault are protected behind a PIN and private vault experience. Premium unlocks unlimited protected apps, biometric unlock, decoy vault, intruder detection, and custom themes.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Button(onClick = onPurchaseMonthly, modifier = Modifier.fillMaxWidth()) {
                Text("Monthly subscription")
            }
            Button(onClick = onPurchaseYearly, modifier = Modifier.fillMaxWidth()) {
                Text("Yearly subscription")
            }
            OutlinedButton(onClick = onPurchaseLifetime, modifier = Modifier.fillMaxWidth()) {
                Text("Lifetime purchase")
            }
            Text(
                text = "Subscriptions renew automatically until canceled in Google Play. No misleading hiding claims — your apps stay installed and are accessed through your secure vault.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
