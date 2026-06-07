package com.calculator.vault.presentation.components

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/** Prevents screenshots and screen recording while vault screens are visible. */
@Composable
fun SecureScreenEffect() {
    if (isRunningInstrumentedTest()) return

    val context = LocalContext.current
    val activity = context as? ComponentActivity ?: return
    DisposableEffect(Unit) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

private fun isRunningInstrumentedTest(): Boolean =
    runCatching {
        Class.forName("androidx.test.platform.app.InstrumentationRegistry")
            .getMethod("getInstrumentation")
            .invoke(null) != null
    }.getOrDefault(false)
