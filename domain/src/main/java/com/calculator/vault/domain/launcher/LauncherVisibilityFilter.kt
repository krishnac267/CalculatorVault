package com.calculator.vault.domain.launcher

/**
 * Filters which installed packages appear on Calculator Vault's optional home screen.
 * Does not hide apps from the system launcher or PackageManager.
 */
object LauncherVisibilityFilter {
    fun filterVisiblePackages(
        installedPackages: List<String>,
        hiddenVaultPackages: Set<String>,
        ownPackage: String,
    ): List<String> =
        installedPackages
            .filter { packageName ->
                packageName != ownPackage && packageName !in hiddenVaultPackages
            }
}
