package com.gabrielbotao.softwarevcv.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Serialization shapes for the content JSON. **DTOs never leave `data/`** — repositories map them to
 * domain models. Grouped here because they're one cohesive wire contract. See [[Content-Model]] §5.
 */
@Serializable
data class ImageRefDto(val url: String, val alt: String, val aspectRatio: Float = 0.75f)

@Serializable
data class FabricSpecDto(
    val material: String,
    val origin: String,
    val care: List<String> = emptyList(),
    val notes: String? = null,
)

@Serializable
data class ProductVariantDto(val size: Int, val colorName: String? = null, val available: Boolean = true)

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val collectionSlug: String? = null,
    val priceCents: Long,
    val images: List<ImageRefDto> = emptyList(),
    val shortDescription: String = "",
    val fabric: FabricSpecDto,
    val variants: List<ProductVariantDto> = emptyList(),
    val badges: List<String> = emptyList(),
    val buyUrl: String? = null,
)

@Serializable
data class CollectionDto(
    val slug: String,
    val title: String,
    val subtitle: String? = null,
    val cover: ImageRefDto,
    val productIds: List<String> = emptyList(),
    val story: String? = null,
)

@Serializable
data class AtelierSectionDto(val title: String, val body: String, val image: ImageRefDto? = null)

@Serializable
data class AtelierContentDto(val headline: String, val sections: List<AtelierSectionDto> = emptyList())

@Serializable
data class ContactContentDto(
    val whatsapp: String? = null,
    val instagram: String? = null,
    val email: String? = null,
    val whereToBuy: List<String> = emptyList(),
)

@Serializable
data class BrandDto(val atelier: AtelierContentDto, val contact: ContactContentDto)
