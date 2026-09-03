package com.gabrielbotao.softwarevcv.domain.usecase

import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.repository.CatalogRepository

/** Returns products — all of them, or one collection's when [collectionSlug] is given. */
class GetProductsUseCase(private val repository: CatalogRepository) {
    suspend operator fun invoke(collectionSlug: String? = null): Result<List<Product>> =
        repository.products(collectionSlug)
}
