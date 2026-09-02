package com.gabrielbotao.softwarevcv.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyFormatTest {

    @Test
    fun formats_reais_and_centavos_ptBR() {
        assertEquals("R$ 189,00", formatBrl(18_900))
        assertEquals("R$ 1.234,56", formatBrl(123_456))
        assertEquals("R$ 0,09", formatBrl(9))
        assertEquals("R$ 1.000.000,00", formatBrl(100_000_000))
    }
}
