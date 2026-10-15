package com.app.market.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

actual object LocalAppLocale {
    private var defaultLocale: Locale? = null
    private val LocalAppLocaleInternal = staticCompositionLocalOf { Locale.getDefault().toLanguageTag() }

    actual val current: String
        @Composable get() = LocalAppLocaleInternal.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        if (defaultLocale == null) {
            defaultLocale = Locale.getDefault()
        }
        val targetLocale = value?.takeIf(String::isNotBlank)
            ?.let(Locale::forLanguageTag)
            ?: defaultLocale!!
        Locale.setDefault(targetLocale)
        return LocalAppLocaleInternal.provides(targetLocale.toLanguageTag())
    }
}
