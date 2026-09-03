package com.gabrielbotao.softwarevcv.presentation.features.catalog

import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.domain.model.FabricSpec
import com.gabrielbotao.softwarevcv.domain.model.ImageRef
import com.gabrielbotao.softwarevcv.domain.model.Money
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.repository.CatalogRepository

internal fun testProduct(id: String, collectionSlug: String? = null) = Product(
    id = id, name = id, collectionSlug = collectionSlug, price = Money(1000), images = emptyList(),
    shortDescription = "", fabric = FabricSpec("m", "o", emptyList()), variants = emptyList(),
    badges = emptyList(), buyUrl = null,
)

internal fun testCollection(slug: String) =
    Collection(slug = slug, title = slug, subtitle = null, cover = ImageRef("u", "a"), productIds = emptyList(), story = null)

internal class FakeCatalogRepository(
    private val productList: List<Product> = emptyList(),
    private val collectionList: List<Collection> = emptyList(),
) : CatalogRepository {
    override suspend fun collections() = Result.success(collectionList)
    override suspend fun collection(slug: String) =
        collectionList.firstOrNull { it.slug == slug }?.let { Result.success(it) } ?: Result.failure(NoSuchElementException())

    override suspend fun products(collectionSlug: String?) =
        Result.success(if (collectionSlug == null) productList else productList.filter { it.collectionSlug == collectionSlug })

    override suspend fun product(id: String) =
        productList.firstOrNull { it.id == id }?.let { Result.success(it) } ?: Result.failure(NoSuchElementException())

    override suspend fun featured() = Result.success(productList)
}
