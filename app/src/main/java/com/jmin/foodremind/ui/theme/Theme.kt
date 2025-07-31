package com.jmin.foodremind.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = SuccessGreen,
    background = BgColor,
    surface = CardBg,
    onPrimary = CardBg,
    onSecondary = TextColor,
    onBackground = TextColor,
    onSurface = TextColor
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = SuccessGreen,
    background = BgColor,
    surface = CardBg,
    onPrimary = CardBg,
    onSecondary = TextColor,
    onBackground = TextColor,
    onSurface = TextColor
)

@Composable
fun FoodRemindTheme(
    darkTheme: Boolean = false, // 默认使用浅色主题
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // 禁用动态颜色以保持一致的品牌形象
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
//            // 设置状态栏颜色与应用主题一致
//            window.statusBarColor = colorScheme.primary.toArgb()
            // 确保系统栏是可见的，取消沉浸式模式
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}