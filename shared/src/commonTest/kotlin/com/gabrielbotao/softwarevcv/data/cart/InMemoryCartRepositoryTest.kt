package com.gabrielbotao.softwarevcv.data.cart

import com.gabrielbotao.softwarevcv.domain.model.FabricSpec
import com.gabrielbotao.softwarevcv.domain.model.Money
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.model.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryCartRepositoryTest {

    private fun product(id: String, cents: Long) = Product(
        id = id,
        name = "Peça $id",
        collectionSlug = null,
        price = Money(cents),
        images = emptyList(),
        shortDescription = "",
        fabric = FabricSpec("viscose", "Gaspar", emptyList(), null),
        variants = emptyList(),
        badges = emptyList(),
        buyUrl = null,
    )

    @Test
    fun adding_same_product_and_size_merges_quantity() {
        val repo = InMemoryCartRepository()
        val p = product("a", 10000)
        repo.add(p, Size(40))
        repo.add(p, Size(40), 2)
        assertEquals(1, repo.cart.value.lines.size)
        assertEquals(3, repo.cart.value.lines.first().quantity)
        assertEquals(3, repo.cart.value.itemCount)
    }

    @Test
    fun same_product_different_size_is_a_separate_line() {
        val repo = InMemoryCartRepository()
        val p = product("a", 10000)
        repo.add(p, Size(40))
        repo.add(p, Size(42))
        assertEquals(2, repo.cart.value.lines.size)
    }

    @Test
    fun subtotal_sums_line_totals() {
        val repo = InMemoryCartRepository()
        repo.add(product("a", 10000), Size(40), 2) // 20000
        repo.add(product("b", 5000), Size(38))      // 5000
        assertEquals(25000, repo.cart.value.subtotalCents)
    }

    @Test
    fun setQuantity_zero_removes_the_line_and_clear_empties() {
        val repo = InMemoryCartRepository()
        val p = product("a", 10000)
        repo.add(p, Size(40))
        val key = repo.cart.value.lines.first().key
        repo.setQuantity(key, 0)
        assertTrue(repo.cart.value.isEmpty)

        repo.add(p, Size(40))
        repo.clear()
        assertTrue(repo.cart.value.isEmpty)
    }
}
