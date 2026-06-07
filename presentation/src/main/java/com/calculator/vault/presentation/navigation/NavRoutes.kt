package com.calculator.vault.presentation.navigation

object NavRoutes {
    const val CALCULATOR = "calculator"
    const val SETUP = "setup"
    const val VAULT = "vault/{isFakeVault}"
    const val SETTINGS = "settings"
    const val INTRUDER_LOG = "intruder_log"
    const val ADD_APPS = "add_apps"

    fun vault(isFakeVault: Boolean): String = "vault/$isFakeVault"
}
