package com.gabrielbotao.softwarevcv.domain.model

/**
 * One line in the [Cart]: a product at a chosen [size] and quantity. Identity is product + size (adding
 * the same product/size again bumps the quantity). Prices are captured as integer cents ([Money]).
 * See [[Content-Model]].
 */
data class CartLine(
    val productId: String,
    val name: String,
    val imageUrl: String?,
    val unitPrice: Money,
    val size: Size,
    val quantity: Int,
) {
    /** Stable per product+size, used as the line key in the cart and UI lists. */
    val key: String get() = "$productId:${size.value}"
    val lineTotalCents: Long get() = unitPrice.amountCents * quantity
}

/** The shopping cart — a pure value type; the repository owns mutation + persistence. */
data class Cart(val lines: List<CartLine> = emptyList()) {
    val itemCount: Int get() = lines.sumOf { it.quantity }
    val subtotalCents: Long get() = lines.sumOf { it.lineTotalCents }
    val isEmpty: Boolean get() = lines.isEmpty()
}
