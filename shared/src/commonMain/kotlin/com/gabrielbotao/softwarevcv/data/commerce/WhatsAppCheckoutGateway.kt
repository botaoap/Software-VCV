package com.gabrielbotao.softwarevcv.data.commerce

import com.gabrielbotao.softwarevcv.core.util.formatBrl
import com.gabrielbotao.softwarevcv.domain.commerce.CheckoutGateway
import com.gabrielbotao.softwarevcv.domain.commerce.CheckoutOutcome
import com.gabrielbotao.softwarevcv.domain.commerce.CustomerInfo
import com.gabrielbotao.softwarevcv.domain.model.Cart
import com.gabrielbotao.softwarevcv.domain.repository.BrandRepository

/**
 * Prototype [CheckoutGateway] (the MVP default): builds a pre-filled **WhatsApp** order message and
 * hands off to it — a real, working "buy" flow with **no backend/payment**. Swapping to an own-backend
 * or 3rd-party gateway is a one-line change in `dataModule`; the cart/checkout UI is untouched.
 * See [[VCV-20 — cart & checkout]] · [[VCV-13]].
 */
internal class WhatsAppCheckoutGateway(private val brand: BrandRepository) : CheckoutGateway {

    override suspend fun checkout(cart: Cart, customer: CustomerInfo): CheckoutOutcome {
        if (cart.isEmpty) return CheckoutOutcome.Failed("Sua sacola está vazia.")
        val base = brand.contact().getOrNull()?.whatsapp
            ?: return CheckoutOutcome.Failed("Canal de atendimento indisponível no momento.")

        val message = buildString {
            appendLine("Olá! Gostaria de finalizar meu pedido na VCV:")
            appendLine()
            cart.lines.forEach { line ->
                appendLine("• ${line.name} — tam ${line.size.value} — x${line.quantity} — ${formatBrl(line.lineTotalCents)}")
            }
            appendLine()
            appendLine("Subtotal: ${formatBrl(cart.subtotalCents)}")
            if (customer.name.isNotBlank()) appendLine("Nome: ${customer.name}")
            customer.note?.takeIf { it.isNotBlank() }?.let { appendLine("Obs: $it") }
        }.trimEnd()

        val separator = if (base.contains('?')) '&' else '?'
        return CheckoutOutcome.ExternalHandoff("$base${separator}text=${percentEncode(message)}")
    }
}

/** Minimal percent-encoding for a URL query value (multiplatform; no java.net). */
private fun percentEncode(value: String): String {
    val unreserved = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.~"
    val out = StringBuilder()
    for (byte in value.encodeToByteArray()) {
        val c = byte.toInt() and 0xFF
        if (c.toChar() in unreserved) out.append(c.toChar())
        else out.append('%').append(c.toString(16).uppercase().padStart(2, '0'))
    }
    return out.toString()
}
