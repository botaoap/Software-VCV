package com.gabrielbotao.softwarevcv.presentation.features.brand

import com.gabrielbotao.softwarevcv.domain.model.AtelierContent
import com.gabrielbotao.softwarevcv.domain.model.ContactContent
import com.gabrielbotao.softwarevcv.domain.repository.BrandRepository

/** Shared brand-repository fake for the Atelier / Contato ViewModel tests. */
internal class FakeBrandRepository(
    private val atelierContent: AtelierContent = AtelierContent("h", emptyList()),
    private val contactContent: ContactContent = ContactContent(),
    private val fail: Boolean = false,
) : BrandRepository {
    override suspend fun atelier() =
        if (fail) Result.failure(RuntimeException("boom")) else Result.success(atelierContent)

    override suspend fun contact() =
        if (fail) Result.failure(RuntimeException("boom")) else Result.success(contactContent)
}
