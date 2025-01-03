package com.geecee.escape.utils

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

// Function to enable or disable analytics
fun configureAnalytics(enabled: Boolean) {
    val analytics = Firebase.analytics

    analytics.setConsent(
        mapOf(
            FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to if (enabled) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED,
        )
    )

    analytics.setAnalyticsCollectionEnabled(enabled)
}