package com.autobill.smartpos

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobill.smartpos.app.navigation.AppNavHost
import com.autobill.smartpos.app.navigation.Screen
import com.autobill.smartpos.ui.theme.PrimaryBrand
import com.autobill.smartpos.ui.theme.SmartPosTheme
import com.autobill.smartpos.ui.theme.SurfaceSecondary
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity — the sole entry point for all Compose screens.
 *
 * Responsibilities:
 *  1. Enforce landscape mode (tablet-first app)
 *  2. Resolve session state via [MainViewModel]
 *  3. Show a splash indicator while the session is being resolved
 *  4. Hand off to [AppNavHost] with the correct start destination
 *
 * Auth state changes (login / logout) are handled inside [AppNavHost].
 * [key()] around [AppNavHost] fully resets the nav-stack when the
 * auth state flips so staff can never back-navigate past the login screen.
 *
 * Navigation chrome (PermanentNavigationDrawer) lives in [AppNavHost],
 * not here — MainActivity stays intentionally thin.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Android 13+ notification permission launcher (no-op on earlier versions). */
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or denied — app still works */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tablet-first: always landscape (also declared in manifest for process-death safety)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // Request POST_NOTIFICATIONS on Android 13+ — silently accepted/rejected by staff
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val sessionState by mainViewModel.sessionState.collectAsStateWithLifecycle()
            val isDarkTheme  by mainViewModel.isDarkTheme.collectAsStateWithLifecycle()

            SmartPosTheme(darkTheme = isDarkTheme) {

                when (val state = sessionState) {

                    // ── Splash — session not yet resolved ──────────────────
                    SessionResult.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SurfaceSecondary),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = PrimaryBrand)
                        }
                    }

                    // ── Resolved — navigate to Login or Home ────────────────
                    is SessionResult.Resolved -> {
                        // key() recreates AppNavHost (and its PermanentNavigationDrawer)
                        // whenever login state toggles, resetting the entire back-stack.
                        key(state.user != null) {
                            AppNavHost(
                                startDestination = if (state.user != null) {
                                    Screen.FoodList.route
                                } else {
                                    Screen.Login.route
                                },
                                onLogout = mainViewModel::logout,
                            )
                        }
                    }
                }
            }
        }
    }
}
