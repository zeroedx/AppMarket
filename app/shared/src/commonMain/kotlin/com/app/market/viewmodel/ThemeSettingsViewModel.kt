package com.app.market.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.market.domain.model.theme.ThemeColorMode
import com.app.market.domain.model.theme.ThemeColorSource
import com.app.market.domain.model.theme.ThemeColorSpec
import com.app.market.domain.model.theme.ThemePaletteStyle
import com.app.market.domain.repository.ThemePreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class ThemeSettingsUiState(
    val enableBlur: Boolean = false,
    val enableFloatingBottomBar: Boolean = false,
    val enableFloatingBottomBarBlur: Boolean = false,
    val enableNavigationBadge: Boolean = true,
    val enablePredictiveBack: Boolean = false,
    val pageScale: Float = 1f,
    val colorMode: ThemeColorMode = ThemeColorMode.SYSTEM,
    val colorSource: ThemeColorSource = ThemeColorSource.DEFAULT,
    val seedColor: Long? = null,
    val paletteStyle: ThemePaletteStyle = ThemePaletteStyle.TONAL_SPOT,
    val colorSpec: ThemeColorSpec = ThemeColorSpec.SPEC_2021,
    val amoledDark: Boolean = false,
)

class ThemeSettingsViewModel(
    private val preferences: ThemePreferencesRepository,
) : ViewModel() {
    private val appearance = combine(preferences.enableBlur, preferences.enableFloatingBottomBar) { blur, floating ->
        blur to floating
    }

    private val baseState: StateFlow<ThemeSettingsUiState> = combine(
        appearance,
        preferences.enableFloatingBottomBarBlur,
        preferences.enableNavigationBadge,
        preferences.enablePredictiveBack,
        preferences.pageScale,
    ) { appearance, glass, badge, predictiveBack, scale ->
        ThemeSettingsUiState(
            enableBlur = appearance.first,
            enableFloatingBottomBar = appearance.second,
            enableFloatingBottomBarBlur = glass,
            enableNavigationBadge = badge,
            enablePredictiveBack = predictiveBack,
            pageScale = scale,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeSettingsUiState())

    private val colorState: StateFlow<ThemeSettingsUiState> = combine(
        preferences.colorMode,
        preferences.colorSource,
        preferences.seedColor,
        preferences.paletteStyle,
        preferences.colorSpec,
    ) { mode, source, seed, style, spec ->
        ThemeSettingsUiState(
            colorMode = mode,
            colorSource = source,
            seedColor = seed,
            paletteStyle = style,
            colorSpec = spec,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeSettingsUiState())

    val uiState: StateFlow<ThemeSettingsUiState> = combine(
        baseState,
        colorState,
        preferences.amoledDark,
    ) { base, color, amoled ->
        base.copy(
            colorMode = color.colorMode,
            colorSource = color.colorSource,
            seedColor = color.seedColor,
            paletteStyle = color.paletteStyle,
            colorSpec = color.colorSpec,
            amoledDark = amoled,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ThemeSettingsUiState())

    fun setEnableBlur(value: Boolean) = persist { preferences.setEnableBlur(value) }
    fun setEnableFloatingBottomBar(value: Boolean) = persist { preferences.setEnableFloatingBottomBar(value) }
    fun setEnableFloatingBottomBarBlur(value: Boolean) = persist { preferences.setEnableFloatingBottomBarBlur(value) }
    fun setEnableNavigationBadge(value: Boolean) = persist { preferences.setEnableNavigationBadge(value) }
    fun setEnablePredictiveBack(value: Boolean) = persist { preferences.setEnablePredictiveBack(value) }
    fun setPageScale(value: Float) = persist { preferences.setPageScale(value) }
    fun setColorMode(value: ThemeColorMode) = persist { preferences.setColorMode(value) }
    fun setColorSource(value: ThemeColorSource) = persist { preferences.setColorSource(value) }
    fun setSeedColor(value: Long?) = persist { preferences.setSeedColor(value) }
    fun setPaletteStyle(value: ThemePaletteStyle) = persist { preferences.setPaletteStyle(value) }
    fun setColorSpec(value: ThemeColorSpec) = persist { preferences.setColorSpec(value) }
    fun setAmoledDark(value: Boolean) = persist { preferences.setAmoledDark(value) }

    private fun persist(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
