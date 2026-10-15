package com.app.market.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.key

/**
 * 应用内语言切换：把语言标签下发给平台实现，Android 通过 [android.content.res.Configuration]
 * 生效，Desktop 通过 JVM 默认 [java.util.Locale] 生效。
 */
expect object LocalAppLocale {
    val current: String @Composable get
    @Composable infix fun provides(value: String?): ProvidedValue<*>
}

@Composable
fun AppLocaleProvider(
    localeTag: String?,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppLocale provides localeTag,
    ) {
        key(localeTag) {
            content()
        }
    }
}
