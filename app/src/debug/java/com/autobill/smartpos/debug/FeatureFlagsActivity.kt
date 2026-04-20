package com.autobill.smartpos.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint

/**
 * DEBUG-only Activity that hosts the feature flag toggle panel.
 * Launched via the persistent "🚩 Feature Flags" notification shown by [DebugTools].
 *
 * Registered only in app/src/debug/AndroidManifest.xml — absent from release builds.
 */
@AndroidEntryPoint
class FeatureFlagsActivity : ComponentActivity() {

    private val viewModel: FeatureFlagsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DebugFeatureFlagsScreen(
                viewModel = viewModel,
                onBack    = { finish() },
            )
        }
    }
}

