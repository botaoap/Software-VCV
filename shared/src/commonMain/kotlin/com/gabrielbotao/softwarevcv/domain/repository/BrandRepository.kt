package com.gabrielbotao.softwarevcv.domain.repository

import com.gabrielbotao.softwarevcv.domain.model.AtelierContent
import com.gabrielbotao.softwarevcv.domain.model.ContactContent

/** Static brand content (Atelier story, Contact links). Implemented in `data/`. */
interface BrandRepository {
    suspend fun atelier(): Result<AtelierContent>
    suspend fun contact(): Result<ContactContent>
}
