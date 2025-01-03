package com.geecee.escape.utils

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.core.splashscreen.SplashScreen


//Splash Animation
fun animateSplashScreen(splashScreen: SplashScreen) {
    splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
        // Create a scale animation (zoom effect)
        val scaleAnimation = ScaleAnimation(
            1f, 100f, // From normal size to 5x size
            1f, 100f,
            Animation.RELATIVE_TO_SELF, 0.5f, // Pivot at the center
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1200 // Duration of the zoom animation in ms
            fillAfter = true // Retain the final state
        }

        // Create a fade-out animation
        val fadeOutAnimation = AlphaAnimation(1f, 0f).apply {
            duration = 300 // Duration of the fade-out in ms
            startOffset = 500 // Delay to start after zoom finishes
            fillAfter = true // Retain the final state
        }

        // Combine the animations
        val animationSet = AnimationSet(true).apply {
            addAnimation(scaleAnimation)
            addAnimation(fadeOutAnimation)
        }

        // Start the combined animation
        splashScreenViewProvider.view.startAnimation(animationSet)

        // Remove the splash screen view after the animation ends
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                splashScreenViewProvider.remove()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}