package com.autobill.smartpos

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 *  3. Show a splash indicator while DataStore emits the first value
 *  4. Hand off to [AppNavHost] with the correct start destination once resolved
 *
 * Auth state changes (login success / logout) are handled inside [AppNavHost].
 * [key()] around [AppNavHost] ensures the nav-stack is fully reset when the
 * auth state flips (logged-in ↔ logged-out) so the user can never back-navigate
 * past the login screen.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tablet-first: always landscape
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        enableEdgeToEdge()

        setContent {
            SmartPosTheme {
                val mainViewModel: MainViewModel = hiltViewModel()
                val sessionState by mainViewModel.sessionState.collectAsStateWithLifecycle()

                when (val state = sessionState) {

                    // ── Splash — DataStore hasn't emitted yet ───────────────
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
                        // key() recreates AppNavHost (and resets the back-stack)
                        // whenever login state toggles, preventing back-navigation
                        // past the Login screen after logout.
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
