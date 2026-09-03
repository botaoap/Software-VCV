package com.gabrielbotao.softwarevcv.data.content

import com.gabrielbotao.softwarevcv.data.remote.VcvContentApi
import com.gabrielbotao.softwarevcv.data.remote.dto.BrandDto
import com.gabrielbotao.softwarevcv.data.remote.dto.CollectionDto
import com.gabrielbotao.softwarevcv.data.remote.dto.ProductDto
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import softwarevcv.shared.generated.resources.Res

/**
 * MVP [VcvContentApi]: reads the catalog from **bundled JSON** in `commonMain/composeResources/files/`
 * (works on every target, incl. Wasm, via `Res.readBytes`). No backend. Swapping to a live API later is
 * a new `VcvContentApi` impl — nothing else changes. See [[Content-Model]] §5.
 */
@OptIn(ExperimentalResourceApi::class)
internal class BundledContentSource(private val json: Json) : VcvContentApi {

    override suspend fun products(): List<ProductDto> = json.decodeFromString(readText("files/products.json"))

    override suspend fun collections(): List<CollectionDto> = json.decodeFromString(readText("files/collections.json"))

    override suspend fun brand(): BrandDto = json.decodeFromString(readText("files/brand.json"))

    private suspend fun readText(path: String): String = Res.readBytes(path).decodeToString()
}
