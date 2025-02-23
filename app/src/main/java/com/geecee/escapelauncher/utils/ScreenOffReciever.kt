package com.geecee.escapelauncher.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenOffReceiver(private val onScreenOff: () -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_OFF) {
            // When the screen is off, stop screen time tracking
            onScreenOff()
        }
    }
}
