package com.gabrielbotao.softwarevcv.domain.repository

import com.gabrielbotao.softwarevcv.domain.model.Cart
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.model.Size
import kotlinx.coroutines.flow.StateFlow

/**
 * Owns the shopping cart's state + mutation. Observable so the header badge and cart screen react to
 * changes. The MVP impl is local (in-memory); persistence or a server-synced cart is a drop-in swap
 * behind this interface. See [[VCV-20 — cart & checkout]].
 */
interface CartRepository {
    val cart: StateFlow<Cart>
    fun add(product: Product, size: Size, quantity: Int = 1)
    fun setQuantity(key: String, quantity: Int)
    fun remove(key: String)
    fun clear()
}
