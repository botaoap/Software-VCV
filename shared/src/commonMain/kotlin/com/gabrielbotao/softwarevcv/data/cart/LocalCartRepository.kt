package com.gabrielbotao.softwarevcv.data.cart

import com.gabrielbotao.softwarevcv.domain.model.Cart
import com.gabrielbotao.softwarevcv.domain.model.CartLine
import com.gabrielbotao.softwarevcv.domain.model.Money
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.model.Size
import com.gabrielbotao.softwarevcv.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The app's [CartRepository]. A DI **single** so cart state is shared across screens. When a [json] +
 * [store] are supplied (production), the cart is **persisted** (web: localStorage) and restored on
 * start; with neither (tests) it's a pure in-memory cart with no side effects. A server-synced cart
 * would be a drop-in replacement behind [CartRepository]. See [[VCV-20 — cart & checkout]] · [[VCV-29]].
 */
internal class LocalCartRepository(
    private val json: Json? = null,
    private val store: CartStore? = null,
) : CartRepository {

    private val _cart = MutableStateFlow(restore())
    override val cart: StateFlow<Cart> = _cart.asStateFlow()

    override fun add(product: Product, size: Size, quantity: Int) {
        if (quantity <= 0) return
        val key = "${product.id}:${size.value}"
        _cart.update { current ->
            val existing = current.lines.firstOrNull { it.key == key }
            val lines = if (existing != null) {
                current.lines.map { if (it.key == key) it.copy(quantity = it.quantity + quantity) else it }
            } else {
                current.lines + CartLine(product.id, product.name, product.cover?.url, product.price, size, quantity)
            }
            current.copy(lines = lines)
        }
        persist()
    }

    override fun setQuantity(key: String, quantity: Int) {
        if (quantity <= 0) { remove(key); return }
        _cart.update { c -> c.copy(lines = c.lines.map { if (it.key == key) it.copy(quantity = quantity) else it }) }
        persist()
    }

    override fun remove(key: String) {
        _cart.update { c -> c.copy(lines = c.lines.filterNot { it.key == key }) }
        persist()
    }

    override fun clear() {
        _cart.update { Cart() }
        persist()
    }

    private fun persist() {
        val j = json ?: return
        val s = store ?: return
        runCatching { s.save(j.encodeToString(_cart.value.lines.map { it.toDto() })) }
    }

    private fun restore(): Cart {
        val j = json ?: return Cart()
        val s = store ?: return Cart()
        val raw = s.load() ?: return Cart()
        return runCatching { Cart(j.decodeFromString<List<CartLineDto>>(raw).map { it.toLine() }) }.getOrDefault(Cart())
    }
}

/** Serializable snapshot of a [CartLine] (keeps the domain model free of serialization). */
@Serializable
private data class CartLineDto(
    val productId: String,
    val name: String,
    val imageUrl: String?,
    val unitPriceCents: Long,
    val size: Int,
    val quantity: Int,
)

private fun CartLine.toDto() = CartLineDto(productId, name, imageUrl, unitPrice.amountCents, size.value, quantity)
private fun CartLineDto.toLine() = CartLine(productId, name, imageUrl, Money(unitPriceCents), Size(size), quantity)
