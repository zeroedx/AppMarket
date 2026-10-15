package com.app.market.ui.util

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object LocalAppLocale {
    private var defaultLocale: Locale? = null

    actual val current: String
        @Composable get() = Locale.getDefault().toLanguageTag()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = LocalConfiguration.current
        val context = LocalContext.current

        if (defaultLocale == null) {
            defaultLocale = Locale.getDefault()
        }

        val targetLocale = value?.takeIf(String::isNotBlank)
            ?.let(Locale::forLanguageTag)
            ?: defaultLocale!!

        Locale.setDefault(targetLocale)
        val updated = Configuration(configuration).apply { setLocale(targetLocale) }
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(updated, context.resources.displayMetrics)

        return LocalConfiguration.provides(updated)
    }
}
