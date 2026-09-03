package com.gabrielbotao.softwarevcv.domain.usecase

import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.domain.repository.CatalogRepository

/** Returns all collections (for the `/colecoes` list). */
class GetCollectionsUseCase(private val repository: CatalogRepository) {
    suspend operator fun invoke(): Result<List<Collection>> = repository.collections()
}
