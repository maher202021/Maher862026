package com.example.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.data.local.AdminConfig

// --- Centralized Yemeni Themes & Palettes ---
val PrimaryGreen = Color(0xFF0E6F4B) // Royal Emerald
val AccentYemeniGold = Color(0xFFD4AF37) // Yemeni Gold
val AccentRed = Color(0xFFEF5350)
val SoftGrayBackground = Color(0xFFF1F5FE)

// Theme Presets
val EmeraldPrimary = Color(0xFF0E6F4B)
val EmeraldBg = Color(0xFF071C13)
val EmeraldCard = Color(0xFF103324)

val GoldPrimary = Color(0xFFD4AF37)
val GoldBg = Color(0xFF121214)
val GoldCard = Color(0xFF222226)

val SlatePrimary = Color(0xFF90A4AE)
val SlateBg = Color(0xFF0F172A)
val SlateCard = Color(0xFF1E293B)

data class ThemeColors(
    val primary: Color,
    val background: Color,
    val cardBackground: Color,
    val textOnBg: Color,
    val secondary: Color
)

fun resolveTheme(config: AdminConfig): ThemeColors {
    updateGlobalFont(config.fontName)
    return when (config.themeIndex) {
        0 -> ThemeColors(SlatePrimary, SlateBg, SlateCard, Color.White, AccentYemeniGold)
        1 -> ThemeColors(GoldPrimary, GoldBg, GoldCard, Color.White, GoldPrimary)
        2 -> ThemeColors(EmeraldPrimary, EmeraldBg, EmeraldCard, Color.White, AccentYemeniGold)
        else -> {
            // Custom Parseable Hexes, safe boundary checks
            val primaryParsed = try {
                Color(android.graphics.Color.parseColor(config.themePrimaryColor))
            } catch (e: Exception) {
                EmeraldPrimary
            }
            val secondaryParsed = try {
                Color(android.graphics.Color.parseColor(config.themeSecondaryColor))
            } catch (e: Exception) {
                AccentYemeniGold
            }
            ThemeColors(primaryParsed, EmeraldBg, EmeraldCard, Color.White, secondaryParsed)
        }
    }
}

var AppMainFont: FontFamily = FontFamily.SansSerif

fun updateGlobalFont(fontName: String?) {
    AppMainFont = when (fontName?.lowercase()) {
        "sansserif", "sans-serif" -> FontFamily.SansSerif
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> FontFamily.Default
    }
}

// Yemeni formula validator
fun isValidYemeniMobile(num: String): Boolean {
    val clean = num.trim()
    return clean.length == 9 && (clean.startsWith("77") || clean.startsWith("73") || clean.startsWith("71") || clean.startsWith("70"))
}
