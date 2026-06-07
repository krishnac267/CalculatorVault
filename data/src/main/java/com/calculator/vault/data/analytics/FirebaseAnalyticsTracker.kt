package com.calculator.vault.data.analytics

import android.content.Context
import android.os.Bundle
import com.calculator.vault.domain.repository.AnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    @ApplicationContext context: Context,
) : AnalyticsTracker {
    private val firebaseAnalytics = runCatching { FirebaseAnalytics.getInstance(context) }.getOrNull()

    override fun logEvent(name: String, params: Map<String, String>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) -> putString(key, value) }
        }
        firebaseAnalytics?.logEvent(name, bundle)
    }

    override fun setUserProperty(name: String, value: String) {
        firebaseAnalytics?.setUserProperty(name, value)
    }
}
