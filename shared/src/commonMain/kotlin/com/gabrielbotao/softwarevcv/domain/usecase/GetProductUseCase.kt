package com.gabrielbotao.softwarevcv.domain.usecase

import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.repository.CatalogRepository

/** Returns one product by id (`/produto/{id}`). */
class GetProductUseCase(private val repository: CatalogRepository) {
    suspend operator fun invoke(id: String): Result<Product> = repository.product(id)
}
