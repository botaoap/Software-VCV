package com.gabrielbotao.softwarevcv.domain.usecase

import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.domain.repository.CatalogRepository

/** Returns one collection by slug (`/colecao/{slug}`). */
class GetCollectionUseCase(private val repository: CatalogRepository) {
    suspend operator fun invoke(slug: String): Result<Collection> = repository.collection(slug)
}
