package com.gabrielbotao.softwarevcv.domain.usecase

import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.repository.CatalogRepository

/** Returns the featured products for the Home hero / best-seller strip. */
class GetFeaturedProductsUseCase(private val repository: CatalogRepository) {
    suspend operator fun invoke(): Result<List<Product>> = repository.featured()
}
