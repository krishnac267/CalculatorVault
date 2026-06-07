package com.calculator.vault.domain.launcher

object LauncherModeMessages {
    fun hiddenAppsBanner(hiddenCount: Int): String = when (hiddenCount) {
        0 -> ""
        1 -> "1 app hidden in Calculator Vault"
        else -> "$hiddenCount apps hidden in Calculator Vault"
    }
}
