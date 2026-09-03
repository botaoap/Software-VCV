package com.gabrielbotao.softwarevcv.data.repository

import com.gabrielbotao.softwarevcv.data.mapper.toDomain
import com.gabrielbotao.softwarevcv.data.remote.VcvContentApi
import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.repository.CatalogRepository

/**
 * [CatalogRepository] over the [VcvContentApi] seam. Reads DTOs, maps to domain, wraps in [Result] so a
 * missing id/slug surfaces as a failure the UI can render (not an exception). See [[KMP-Clean-Architecture]] §6.
 */
internal class CatalogRepositoryImpl(private val api: VcvContentApi) : CatalogRepository {

    override suspend fun collections(): Result<List<Collection>> =
        runCatching { api.collections().map { it.toDomain() } }

    override suspend fun collection(slug: String): Result<Collection> =
        runCatching { api.collections().first { it.slug == slug }.toDomain() }

    override suspend fun products(collectionSlug: String?): Result<List<Product>> = runCatching {
        val all = api.products().map { it.toDomain() }
        if (collectionSlug == null) all else all.filter { it.collectionSlug == collectionSlug }
    }

    override suspend fun product(id: String): Result<Product> =
        runCatching { api.products().first { it.id == id }.toDomain() }

    override suspend fun featured(): Result<List<Product>> = runCatching {
        val all = api.products().map { it.toDomain() }
        all.filter { it.badges.isNotEmpty() }.ifEmpty { all.take(4) }
    }
}
