package com.gabrielbotao.softwarevcv.domain.usecase

import com.gabrielbotao.softwarevcv.domain.commerce.CheckoutGateway
import com.gabrielbotao.softwarevcv.domain.commerce.CheckoutOutcome
import com.gabrielbotao.softwarevcv.domain.commerce.CustomerInfo
import com.gabrielbotao.softwarevcv.domain.model.Cart
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.model.Size
import com.gabrielbotao.softwarevcv.domain.repository.CartRepository
import kotlinx.coroutines.flow.StateFlow

/** Observe the cart (header badge, cart screen). */
class ObserveCartUseCase(private val repository: CartRepository) {
    operator fun invoke(): StateFlow<Cart> = repository.cart
}

/** Add a product (at a size) to the cart. */
class AddToCartUseCase(private val repository: CartRepository) {
    operator fun invoke(product: Product, size: Size, quantity: Int = 1) =
        repository.add(product, size, quantity)
}

/** Set a line's quantity (0 removes it). */
class UpdateCartQuantityUseCase(private val repository: CartRepository) {
    operator fun invoke(key: String, quantity: Int) =
        if (quantity <= 0) repository.remove(key) else repository.setQuantity(key, quantity)
}

/** Remove a line. */
class RemoveCartLineUseCase(private val repository: CartRepository) {
    operator fun invoke(key: String) = repository.remove(key)
}

/**
 * Run checkout through the agnostic [CheckoutGateway] and clear the cart on a successful, order-creating
 * outcome ([CheckoutOutcome.Confirmed]). Redirect/handoff leave the cart intact until the buyer returns.
 */
class CheckoutUseCase(
    private val repository: CartRepository,
    private val gateway: CheckoutGateway,
) {
    suspend operator fun invoke(customer: CustomerInfo): CheckoutOutcome {
        val outcome = gateway.checkout(repository.cart.value, customer)
        if (outcome is CheckoutOutcome.Confirmed) repository.clear()
        return outcome
    }
}
