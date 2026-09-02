package com.gabrielbotao.softwarevcv.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * LAYER 1 — primitives. The **only** place raw hex literals live (design-system rule,
 * [[VCV Design-System]] §1–2). Direction **1C "Corpo"**: a warm, editorial, skin-adjacent system —
 * wine anchor, dusty-rose accent, nude/cream grounds, near-black ink.
 *
 * **TODO (❓ Felipe):** these are best-effort values read from the Claude Design artifact; replace with
 * the exact 1C hex when delivered — a one-file edit that re-themes the whole app.
 */
internal object BrandPalette {
    // Light — the brand's primary look
    val Bordo700 = Color(0xFF6E2B39) // wine — primary
    val Bordo900 = Color(0xFF4A1B26) // deep wine — containers/bands
    val Rose300 = Color(0xFFC98A8A)  // dusty rose — accent
    val Rose100 = Color(0xFFE7CFC8)  // soft rose — chips/soft accent
    val Nude100 = Color(0xFFEFE0D8)  // nude — card/panel surface tint
    val Cream50 = Color(0xFFF6F0EA)  // cream — page ground
    val Ink900 = Color(0xFF211A18)   // ink — primary text
    val Ink500 = Color(0xFF6B5F5A)   // muted — captions/outline
    val White = Color(0xFFFFFFFF)
    val Error500 = Color(0xFFB23A48) // error — kept in the wine family

    // Dark — a warm near-black variant (not pure grey)
    val InkBg = Color(0xFF1A1513)          // dark warm background
    val InkSurface = Color(0xFF241D1B)     // dark warm surface/card
    val InkSurfaceVariant = Color(0xFF3A2E2B)
    val MutedOnDark = Color(0xFFB8A9A2)
}
