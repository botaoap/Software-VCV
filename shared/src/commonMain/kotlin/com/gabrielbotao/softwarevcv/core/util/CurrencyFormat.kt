package com.gabrielbotao.softwarevcv.core.util

import kotlin.math.abs

/**
 * Format integer cents as a pt-BR BRL string, e.g. `18900` → `"R$ 189,00"`, `123456` → `"R$ 1.234,56"`.
 * Money is never floating point; this is the single place BRL is formatted (VCV-5's `Money` delegates here).
 */
fun formatBrl(amountCents: Long): String {
    val cents = abs(amountCents)
    val reais = cents / 100
    val centavos = (cents % 100).toString().padStart(2, '0')
    val grouped = reais.toString().reversed().chunked(3).joinToString(".").reversed()
    val sign = if (amountCents < 0) "-" else ""
    return "R$ $sign$grouped,$centavos"
}
