package com.gabrielbotao.softwarevcv.data.mapper

import com.gabrielbotao.softwarevcv.data.remote.dto.FabricSpecDto
import com.gabrielbotao.softwarevcv.data.remote.dto.ImageRefDto
import com.gabrielbotao.softwarevcv.data.remote.dto.ProductDto
import com.gabrielbotao.softwarevcv.data.remote.dto.ProductVariantDto
import com.gabrielbotao.softwarevcv.domain.model.ProductBadge
import com.gabrielbotao.softwarevcv.domain.model.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentMapperTest {

    @Test
    fun productDto_maps_to_domain_and_parses_known_badges_only() {
        val dto = ProductDto(
            id = "p1",
            name = "Test",
            collectionSlug = "c1",
            priceCents = 18_900,
            images = listOf(ImageRefDto("u", "alt")),
            shortDescription = "d",
            fabric = FabricSpecDto("Viscolinho", "Gaspar"),
            variants = listOf(ProductVariantDto(38)),
            badges = listOf("BEST_SELLER", "UNKNOWN"),
            buyUrl = null,
        )

        val product = dto.toDomain()

        assertEquals("p1", product.id)
        assertEquals(18_900, product.price.amountCents)
        assertEquals(Size(38), product.variants.first().size)
        assertEquals(listOf(ProductBadge.BEST_SELLER), product.badges) // UNKNOWN dropped
        assertEquals("u", product.cover?.url)
    }
}
