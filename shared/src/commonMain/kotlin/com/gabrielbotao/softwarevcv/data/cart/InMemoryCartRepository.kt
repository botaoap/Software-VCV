package com.gabrielbotao.softwarevcv.data.cart

import com.gabrielbotao.softwarevcv.domain.model.Cart
import com.gabrielbotao.softwarevcv.domain.model.CartLine
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.model.Size
import com.gabrielbotao.softwarevcv.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory [CartRepository] — a process-lifetime cart, enough for the UI-only prototype (no backend).
 * A DI **single** so state is shared across screens. Persistence (multiplatform-settings) or a
 * server-synced cart is a drop-in replacement behind [CartRepository]. See [[VCV-20 — cart & checkout]].
 */
internal class InMemoryCartRepository : CartRepository {

    private val _cart = MutableStateFlow(Cart())
    override val cart: StateFlow<Cart> = _cart.asStateFlow()

    override fun add(product: Product, size: Size, quantity: Int) {
        if (quantity <= 0) return
        val key = "${product.id}:${size.value}"
        _cart.update { current ->
            val existing = current.lines.firstOrNull { it.key == key }
            val lines = if (existing != null) {
                current.lines.map { if (it.key == key) it.copy(quantity = it.quantity + quantity) else it }
            } else {
                current.lines + CartLine(
                    productId = product.id,
                    name = product.name,
                    imageUrl = product.cover?.url,
                    unitPrice = product.price,
                    size = size,
                    quantity = quantity,
                )
            }
            current.copy(lines = lines)
        }
    }

    override fun setQuantity(key: String, quantity: Int) {
        if (quantity <= 0) { remove(key); return }
        _cart.update { current ->
            current.copy(lines = current.lines.map { if (it.key == key) it.copy(quantity = quantity) else it })
        }
    }

    override fun remove(key: String) {
        _cart.update { current -> current.copy(lines = current.lines.filterNot { it.key == key }) }
    }

    override fun clear() {
        _cart.update { Cart() }
    }
}
