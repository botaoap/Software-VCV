package com.gabrielbotao.softwarevcv.domain.repository

import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.domain.model.Product

/**
 * Catalog reads. Implemented in `data/` over the `VcvContentApi` seam (bundled JSON for the MVP; a
 * remote API later). All calls return [Result] so callers render an error state without try/catch.
 */
interface CatalogRepository {
    suspend fun collections(): Result<List<Collection>>
    suspend fun collection(slug: String): Result<Collection>
    suspend fun products(collectionSlug: String? = null): Result<List<Product>>
    suspend fun product(id: String): Result<Product>
    suspend fun featured(): Result<List<Product>>
}
