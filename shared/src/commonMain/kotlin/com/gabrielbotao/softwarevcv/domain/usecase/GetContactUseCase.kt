package com.gabrielbotao.softwarevcv.domain.usecase

import com.gabrielbotao.softwarevcv.domain.model.ContactContent
import com.gabrielbotao.softwarevcv.domain.repository.BrandRepository

/** Returns the Contact / where-to-buy content. */
class GetContactUseCase(private val repository: BrandRepository) {
    suspend operator fun invoke(): Result<ContactContent> = repository.contact()
}
