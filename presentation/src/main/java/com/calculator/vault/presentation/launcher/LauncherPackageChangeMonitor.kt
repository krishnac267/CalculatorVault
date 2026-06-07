package com.calculator.vault.presentation.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

/**
 * Listens for install/uninstall events so the home grid refreshes without restarting.
 */
class LauncherPackageChangeMonitor(
    private val onPackageChanged: (removedPackage: String?) -> Unit,
) {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val removed = when (intent.action) {
                Intent.ACTION_PACKAGE_REMOVED -> intent.data?.schemeSpecificPart
                else -> null
            }
            onPackageChanged(removed)
        }
    }

    private var registered = false

    fun register(context: Context) {
        if (registered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        registered = true
    }

    fun unregister(context: Context) {
        if (!registered) return
        context.unregisterReceiver(receiver)
        registered = false
    }
}
