package com.gabrielbotao.softwarevcv.domain.usecase

import com.gabrielbotao.softwarevcv.domain.model.AtelierContent
import com.gabrielbotao.softwarevcv.domain.repository.BrandRepository

/** Returns the Atelier / Sobre story content. */
class GetAtelierUseCase(private val repository: BrandRepository) {
    suspend operator fun invoke(): Result<AtelierContent> = repository.atelier()
}
