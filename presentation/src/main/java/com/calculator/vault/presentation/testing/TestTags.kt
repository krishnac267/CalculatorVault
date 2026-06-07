package com.calculator.vault.presentation.testing

object TestTags {
    const val CALC_DISPLAY = "calc_display"
    const val CALC_BIOMETRIC = "calc_biometric"
    fun calcKey(label: String): String = "calc_key_${label.toTestTagKey()}"

    const val SETUP_TITLE = "setup_title"
    const val SETUP_PIN = "setup_pin"
    const val SETUP_CONFIRM_PIN = "setup_confirm_pin"
    const val SETUP_QUESTION = "setup_question"
    const val SETUP_ANSWER = "setup_answer"
    const val SETUP_FAKE_PIN = "setup_fake_pin"
    const val SETUP_NEXT = "setup_next"
    const val SETUP_FINISH = "setup_finish"

    const val VAULT_TITLE = "vault_title"
    const val VAULT_LOCK = "vault_lock"
    const val VAULT_SEARCH = "vault_search"
    const val VAULT_EMPTY = "vault_empty"
    const val VAULT_TAB_HIDDEN = "vault_tab_hidden"
    const val VAULT_TAB_FAVORITES = "vault_tab_favorites"
    const val VAULT_TAB_RECENT = "vault_tab_recent"
    const val VAULT_TAB_SETTINGS = "vault_tab_settings"
    const val VAULT_ADD_APPS = "vault_add_apps"
    const val VAULT_SECURITY_SETTINGS = "vault_security_settings"
    const val SETTINGS_TITLE = "settings_title"
    const val SETTINGS_CHANGE_PIN = "settings_change_pin"
    const val SETTINGS_SESSION_TIMEOUT = "settings_session_timeout"
    const val SETTINGS_INTRUDER = "settings_intruder"
}

private fun String.toTestTagKey(): String = when (this) {
    "+" -> "plus"
    "-" -> "minus"
    "×" -> "mul"
    "÷" -> "div"
    "=" -> "equals"
    "%" -> "percent"
    "√" -> "sqrt"
    "^" -> "power"
    "±" -> "sign"
    "⌫" -> "backspace"
    "." -> "dot"
    else -> this
}
