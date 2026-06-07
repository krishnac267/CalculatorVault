package com.calculator.vault.security.protection

import android.app.Activity
import android.view.WindowManager
import javax.inject.Inject
import javax.inject.Singleton

/** Applies FLAG_SECURE to prevent screenshots and screen recording in vault screens. */
@Singleton
class ScreenProtectionManager @Inject constructor() {

    fun enableProtection(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
    }

    fun disableProtection(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
