package com.geecee.escapelauncher.feature.shortcuts

import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.geecee.escapelauncher.core.di.ApplicationScope
import com.geecee.escapelauncher.core.domain.repository.shortcuts.PinnedShortcutsRepository
import com.geecee.escapelauncher.core.model.PinnedShortcut
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles [LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT] when another app (e.g. Chrome's "Add to
 * Home screen", or a dedicated shortcut-creating app) asks to pin a shortcut to this launcher.
 * Acceptance is silent - no confirmation UI - matching how favoriting an app already works here.
 * The request is validated exclusively via [LauncherApps.getPinItemRequest]; raw intent extras
 * are never trusted directly.
 */
@AndroidEntryPoint
class PinShortcutActivity : ComponentActivity() {

    @Inject
    lateinit var pinnedShortcutsRepository: PinnedShortcutsRepository

    // Application-scoped: this activity is torn down as soon as it finishes (often within
    // milliseconds, since it's translucent and immediately navigates home), which would cancel
    // an in-flight DB write if it were tied to lifecycleScope instead - the persisted shortcut
    // must survive regardless of what happens to this activity afterward.
    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launcherApps = getSystemService(LauncherApps::class.java)
        val request = launcherApps?.getPinItemRequest(intent)

        if (request == null ||
            !request.isValid ||
            request.requestType != LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT
        ) {
            goHomeAndFinish()
            return
        }

        val shortcutInfo = request.shortcutInfo
        if (shortcutInfo == null) {
            goHomeAndFinish()
            return
        }

        val accepted = try {
            request.accept()
        } catch (e: Exception) {
            Log.e("PinShortcutActivity", "Failed to accept pin shortcut request", e)
            false
        }

        if (!accepted) {
            goHomeAndFinish()
            return
        }

        appScope.launch {
            pinnedShortcutsRepository.upsert(
                PinnedShortcut(
                    packageName = shortcutInfo.`package`,
                    shortcutId = shortcutInfo.id,
                    // longLabel is the full descriptive name; shortLabel is meant to be heavily
                    // truncated (~10 chars, for space-constrained UI like icon badges) so it's
                    // only a fallback here, not the primary choice.
                    label = shortcutInfo.longLabel?.toString()?.takeIf { it.isNotBlank() }
                        ?: shortcutInfo.shortLabel?.toString()
                        ?: shortcutInfo.id
                )
            )
        }
        goHomeAndFinish()
    }

    /**
     * This activity has no natural task of its own to fall back to when it finishes (the OS
     * starts it fresh on behalf of whichever app requested the pin), so without this, Android
     * would surface whatever task happens to be next in Recents' z-order - which can be a stale
     * task left by some other launcher the user tried previously, not this one. Explicitly
     * sending a HOME intent guarantees we land back on the actual default launcher instead.
     */
    private fun goHomeAndFinish() {
        try {
            startActivity(
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        } catch (e: Exception) {
            Log.e("PinShortcutActivity", "Failed to return home", e)
        }
        finish()
    }
}
