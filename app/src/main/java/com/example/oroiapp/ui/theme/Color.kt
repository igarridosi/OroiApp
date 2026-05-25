package com.example.oroiapp.ui.theme

import androidx.compose.ui.graphics.Color

// ╔══════════════════════════════════════════════════════════════╗
// ║  OROI — Color System                                        ║
// ║  Elite dark mode: near-black foundation + purple accents    ║
// ╚══════════════════════════════════════════════════════════════╝

// ── Brand Purples ────────────────────────────────────────────
val PurpleAccent      = Color(0xFF8B5CF6)   // Primary action — vibrant & modern
val PurpleBright      = Color(0xFF7A40F2)   // Light-mode primary (slightly deeper)
val PurpleLight       = Color(0xFFD0C8FF)   // On-container text, labels
val PurpleMuted       = Color(0xFF2A1B52)   // Dark container fill
val PurpleGlow        = Color(0xFFBB86FC)   // Secondary accent / highlight
val PurpleFuchsia     = Color(0xFFDC59FF)   // Tertiary pop

// ── Dark Foundation (near-black, cool-tinted) ────────────────
val DarkBackground    = Color(0xFF0C0C14)   // Deepest layer
val DarkSurface       = Color(0xFF161622)   // Cards — elevated
val DarkSurfaceHigh   = Color(0xFF1E1E2E)   // Chips, toggles, secondary surfaces
val DarkOutline       = Color(0xFF2C2C40)   // Subtle borders
val DarkOutlineVar    = Color(0xFF1E1E2C)   // Even subtler separator

// ── Light Foundation ─────────────────────────────────────────
val LightBackground   = Color(0xFFF8F7FC)   // Slightly purple-tinted white
val LightSurface      = Color(0xFFFFFFFF)   // Pure white cards
val LightSurfaceVar   = Color(0xFFEDE8F5)   // Chip backgrounds
val LightOutline      = Color(0xFFCAC4D0)   // Borders

// ── Text ─────────────────────────────────────────────────────
val TextPrimaryDark   = Color(0xFFEAE6F2)   // Primary text on dark bg
val TextSecondaryDark = Color(0xFF9A96AC)   // Muted secondary text on dark bg
val TextPrimaryLight  = Color(0xFF1C1B1F)   // Primary text on light bg
val TextSecondaryLight= Color(0xFF49454F)   // Muted text on light bg

// ── Semantic ─────────────────────────────────────────────────
val ErrorRed          = Color(0xFFFF6B6B)   // Error — softer in dark
val ErrorRedBright    = Color(0xFFF80F36)   // Error — vivid in light
val White             = Color(0xFFFFFFFF)
val Black             = Color(0xFF000000)

// ── Chart Palette (16 curated colors for donut chart) ────────
val ChartPalette = listOf(
    Color(0xFF8B5CF6),  // Purple
    Color(0xFF06B6D4),  // Cyan
    Color(0xFFF59E0B),  // Amber
    Color(0xFFEF4444),  // Red
    Color(0xFF10B981),  // Emerald
    Color(0xFFF97316),  // Orange
    Color(0xFF3B82F6),  // Blue
    Color(0xFFEC4899),  // Pink
    Color(0xFF14B8A6),  // Teal
    Color(0xFFA855F7),  // Violet
    Color(0xFF84CC16),  // Lime
    Color(0xFFE11D48),  // Rose
    Color(0xFF6366F1),  // Indigo
    Color(0xFF0EA5E9),  // Sky
    Color(0xFFD946EF),  // Fuchsia
    Color(0xFF22C55E),  // Green
)
