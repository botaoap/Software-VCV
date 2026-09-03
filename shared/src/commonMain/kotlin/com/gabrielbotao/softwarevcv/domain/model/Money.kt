package com.gabrielbotao.softwarevcv.domain.model

/**
 * A price as **integer cents** (never floating-point money). Display formatting is a UI concern —
 * `core/util/formatBrl` renders pt-BR `R$`. See [[Content-Model]] §2.
 */
data class Money(val amountCents: Long, val currency: String = "BRL")
