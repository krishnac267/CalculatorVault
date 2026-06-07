package com.calculator.vault.e2e

import android.Manifest
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.rule.GrantPermissionRule

object E2ETestHelpers {
    val targetPackage: String
        get() = InstrumentationRegistry.getInstrumentation().targetContext.packageName

    fun grantRuntimePermissions(): GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.CAMERA)

    fun grantCameraViaShell() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "pm grant $targetPackage android.permission.CAMERA",
        ).close()
    }

    /** Syncs virtual fingerprint sensor state when supported. */
    fun syncEmulatorFingerprint() {
        runCatching {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand("cmd fingerprint sync")
                .close()
        }
    }

    fun getResumedActivity(): FragmentActivity? {
        var activity: FragmentActivity? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activity = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .firstOrNull() as? FragmentActivity
        }
        return activity
    }

    fun isBiometricAuthAvailable(activity: FragmentActivity): Boolean =
        BiometricManager.from(activity).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS

    /** Simulates finger down on virtualized fingerprint sensors (API 28+ emulator). */
    fun simulateFingerprintTouch(): Boolean =
        runCatching {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand("cmd fingerprint fingerdown")
                .close()
            true
        }.getOrDefault(false)
}
