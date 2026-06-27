package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ── 1. "SLEEK INTERFACE" LIGHT-THEME PREMIUM GRAPHICS SYSTEM ──

val SleekBg = Color(0xFFF7F9FF)         // Soft slate-white canvas background (#F7F9FF)
val SleekPrimary = Color(0xFF1A1C1E)    // High-efficiency slate charcoal active text (#1A1C1E)
val SleekSecondary = Color(0xFF2563EB)  // Vibrant BitVault Blue 600 brand accent (#2563EB)
val SleekTertiary = Color(0xFF7E22CE)   // Purple 700 hardware connection accent (#7E22CE)
val SleekWhite = Color(0xFFFFFFFF)      // Solid white sleek cards (#FFFFFF)
val SleekGrayLight = Color(0xFFEFF6FF)  // Soft light blue (Blue 50) action background (#EFF6FF)
val SleekTextMuted = Color(0xFF6B7280)  // Balanced mid-contrast description text (#6B7280)
val SleekTextDark = Color(0xFF1A1C1E)   // Strong dark charcoal text for buttons/headings (#1A1C1E)
val SleekBorder = Color(0xFFE2E8F0)     // Super-thin cool slate-grey outline details (#E2E8F0)

// Canonical Custom Layout Grids
val EcoBlueBg = Color(0xFFE3EFFF)       // Eco mode beautiful gradient helper (#E3EFFF)
val ColdPurpleBg = Color(0xFFF2E7FF)    // Cold vault purple protection card (#F2E7FF)

// Status indications
val SuccessGreen = Color(0xFF16A34A)    // Stable hashrate green indication
val ErrorRed = Color(0xFFDC2626)        // Alerts, overheating, and powerouts

// ── 2. SEAMLESS COMPATIBILITY ALIASES for flawless backward & forward layout matches ──

val SlateBlack = SleekBg
val SlateDark = SleekWhite
val SlateMedium = SleekGrayLight
val SlateLight = SleekBorder
val AmberGold = SleekSecondary
val AmberLight = Color(0xFF1D4ED8)
val TextLight = SleekTextDark
val TextMuted = SleekTextMuted
val AccentBlue = SleekTertiary
