package com.gabrielbotao.softwarevcv.data.repository

import com.gabrielbotao.softwarevcv.data.mapper.toDomain
import com.gabrielbotao.softwarevcv.data.remote.VcvContentApi
import com.gabrielbotao.softwarevcv.domain.model.AtelierContent
import com.gabrielbotao.softwarevcv.domain.model.ContactContent
import com.gabrielbotao.softwarevcv.domain.repository.BrandRepository

/** [BrandRepository] over the [VcvContentApi] seam. */
internal class BrandRepositoryImpl(private val api: VcvContentApi) : BrandRepository {

    override suspend fun atelier(): Result<AtelierContent> =
        runCatching { api.brand().atelier.toDomain() }

    override suspend fun contact(): Result<ContactContent> =
        runCatching { api.brand().contact.toDomain() }
}
