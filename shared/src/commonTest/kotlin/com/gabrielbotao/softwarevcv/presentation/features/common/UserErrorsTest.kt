package com.gabrielbotao.softwarevcv.presentation.features.common

import kotlin.test.Test
import kotlin.test.assertEquals

class UserErrorsTest {

    @Test
    fun notFound_lookup_keeps_specific_message() {
        val e: Throwable = NoSuchElementException("Collection contains no element matching the predicate.")
        assertEquals("Produto não encontrado.", e.toUserMessage("Produto não encontrado."))
    }

    @Test
    fun other_failures_become_generic_and_never_leak_the_raw_text() {
        val e: Throwable = IllegalStateException("Missing resource with path: ./composeResources/…/products.json")
        val msg = e.toUserMessage("Produto não encontrado.")
        assertEquals("Não foi possível carregar. Tente novamente.", msg)
        // The raw exception text must not reach the user.
        check(!msg.contains("composeResources"))
    }

    @Test
    fun default_notFound_is_generic() {
        assertEquals("Não encontrado.", NoSuchElementException().toUserMessage())
    }
}
