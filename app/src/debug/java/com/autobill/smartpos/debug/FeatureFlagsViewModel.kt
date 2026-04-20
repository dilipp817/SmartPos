package com.autobill.smartpos.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.featureflag.FeatureFlag
import com.autobill.smartpos.domain.repository.FeatureFlagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for a single row in the debug panel. */
data class FlagRowState(
    val flag: FeatureFlag,
    val resolvedValue: Boolean,
    val override: Boolean?,     // null = no override set; value shown in the "Source" chip
    val hasRemoteValue: Boolean,
)

@HiltViewModel
class FeatureFlagsViewModel @Inject constructor(
    private val repository: FeatureFlagRepository,
) : ViewModel() {

    val rows: StateFlow<List<FlagRowState>> = combine(
        repository.observeAll(),
        repository.observeOverrides(),
    ) { all, overrides ->
        FeatureFlag.entries.map { flag ->
            val override = overrides[flag]
            FlagRowState(
                flag           = flag,
                resolvedValue  = all[flag] ?: flag.defaultValue,
                override       = override,
                // True when the resolved value comes from the remote config (present in
                // the merged map) rather than a local override or a hardcoded default.
                hasRemoteValue = override == null && all.containsKey(flag),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeatureFlag.entries.map { flag ->
            FlagRowState(flag, flag.defaultValue, null, false)
        },
    )

    fun toggle(flag: FeatureFlag, enabled: Boolean) {
        viewModelScope.launch { repository.setOverride(flag, enabled) }
    }

    fun clearOverride(flag: FeatureFlag) {
        viewModelScope.launch { repository.clearOverride(flag) }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clearAllOverrides() }
    }
}

