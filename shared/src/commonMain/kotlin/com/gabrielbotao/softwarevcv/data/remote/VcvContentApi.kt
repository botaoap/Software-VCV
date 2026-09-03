package com.gabrielbotao.softwarevcv.data.remote

import com.gabrielbotao.softwarevcv.data.remote.dto.BrandDto
import com.gabrielbotao.softwarevcv.data.remote.dto.CollectionDto
import com.gabrielbotao.softwarevcv.data.remote.dto.ProductDto

/**
 * The content anti-corruption seam. The MVP implementation reads bundled JSON (no backend); a Ktor
 * implementation can replace it later touching only `data/`. Returns DTOs (they never leave `data/`).
 * See [[Content-Model]] §5, [[SoftwareVCV_MOC]] §3a.
 */
internal interface VcvContentApi {
    suspend fun products(): List<ProductDto>
    suspend fun collections(): List<CollectionDto>
    suspend fun brand(): BrandDto
}
