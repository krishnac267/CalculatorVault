package com.calculator.vault.presentation.launcher

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build

object LauncherHomeRole {
    fun createRequestIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }
        return Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    }

    fun isDefaultHomeApp(context: Context): Boolean {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = context.packageManager.resolveActivity(
            homeIntent,
            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY,
        )
        return resolveInfo?.activityInfo?.packageName == context.packageName
    }
}
