package com.calculator.vault.domain.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherVisibilityFilterTest {

    @Test
    fun filterVisiblePackages_excludesVaultAppsAndOwnPackage() {
        val installed = listOf("com.a", "com.b", "com.vault", "com.c")
        val hidden = setOf("com.b")
        val own = "com.vault"

        val visible = LauncherVisibilityFilter.filterVisiblePackages(installed, hidden, own)

        assertEquals(listOf("com.a", "com.c"), visible)
    }

    @Test
    fun filterVisiblePackages_250Apps_completesUnder500ms() {
        val installed = (1..250).map { "com.example.app$it" }
        val hidden = setOf("com.example.app1", "com.example.app50", "com.example.app200")
        val own = "com.calculator.vault"

        val start = System.nanoTime()
        repeat(100) {
            LauncherVisibilityFilter.filterVisiblePackages(installed, hidden, own)
        }
        val elapsedMs = (System.nanoTime() - start) / 1_000_000

        assertTrue("Filtering 250 apps x100 took ${elapsedMs}ms", elapsedMs < 500)
    }

    @Test
    fun hiddenAppsBanner_formatsCorrectly() {
        assertEquals("", LauncherModeMessages.hiddenAppsBanner(0))
        assertEquals("1 app hidden in Calculator Vault", LauncherModeMessages.hiddenAppsBanner(1))
        assertEquals("3 apps hidden in Calculator Vault", LauncherModeMessages.hiddenAppsBanner(3))
    }
}
