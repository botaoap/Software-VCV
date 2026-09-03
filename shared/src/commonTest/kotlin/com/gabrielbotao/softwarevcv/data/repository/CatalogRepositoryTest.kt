package com.gabrielbotao.softwarevcv.data.repository

import com.gabrielbotao.softwarevcv.data.remote.VcvContentApi
import com.gabrielbotao.softwarevcv.data.remote.dto.AtelierContentDto
import com.gabrielbotao.softwarevcv.data.remote.dto.BrandDto
import com.gabrielbotao.softwarevcv.data.remote.dto.CollectionDto
import com.gabrielbotao.softwarevcv.data.remote.dto.ContactContentDto
import com.gabrielbotao.softwarevcv.data.remote.dto.FabricSpecDto
import com.gabrielbotao.softwarevcv.data.remote.dto.ProductDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeContentApi(
    private val productList: List<ProductDto> = emptyList(),
    private val collectionList: List<CollectionDto> = emptyList(),
    private val brandDto: BrandDto = BrandDto(AtelierContentDto("h"), ContactContentDto()),
) : VcvContentApi {
    override suspend fun products() = productList
    override suspend fun collections() = collectionList
    override suspend fun brand() = brandDto
}

private fun product(id: String, collection: String?, badges: List<String>) =
    ProductDto(id = id, name = id, collectionSlug = collection, priceCents = 1000, fabric = FabricSpecDto("m", "o"), badges = badges)

class CatalogRepositoryTest {

    @Test
    fun featured_returns_only_badged_products() = runTest {
        val repo = CatalogRepositoryImpl(
            FakeContentApi(listOf(product("a", "c1", listOf("BEST_SELLER")), product("b", "c1", emptyList()))),
        )
        assertEquals(listOf("a"), repo.featured().getOrThrow().map { it.id })
    }

    @Test
    fun product_by_id_hits_and_misses() = runTest {
        val repo = CatalogRepositoryImpl(FakeContentApi(listOf(product("a", null, emptyList()))))
        assertEquals("a", repo.product("a").getOrThrow().id)
        assertTrue(repo.product("nope").isFailure)
    }

    @Test
    fun products_filter_by_collection() = runTest {
        val repo = CatalogRepositoryImpl(
            FakeContentApi(listOf(product("a", "c1", emptyList()), product("b", "c2", emptyList()))),
        )
        assertEquals(listOf("a"), repo.products("c1").getOrThrow().map { it.id })
        assertEquals(2, repo.products(null).getOrThrow().size)
    }
}
