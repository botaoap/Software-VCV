package com.gabrielbotao.softwarevcv.domain.model

/** A product image with required alt text and a fixed aspect ratio (so grids never reflow). §1. */
data class ImageRef(
    val url: String,
    val alt: String,
    val aspectRatio: Float = 3f / 4f,
)

/** The atelier "ficha técnica" — fabric, origin, and care. A brand differentiator. §1. */
data class FabricSpec(
    val material: String,
    val origin: String,
    val care: List<String>,
    val notes: String? = null,
)

/** A size/color option of a product. `available` is informational in the MVP (no stock sync). §1. */
data class ProductVariant(
    val size: Size,
    val colorName: String? = null,
    val available: Boolean = true,
)

/**
 * A catalog product. Read-only in the MVP. `buyUrl == null` ⇒ the UI shows "em breve"; a present
 * `buyUrl` opens externally. [cover] is the first image (the card hero). See [[Content-Model]] §1.
 */
data class Product(
    val id: String,
    val name: String,
    val collectionSlug: String?,
    val price: Money,
    val images: List<ImageRef>,
    val shortDescription: String,
    val fabric: FabricSpec,
    val variants: List<ProductVariant>,
    val badges: List<ProductBadge>,
    val buyUrl: String?,
) {
    val cover: ImageRef? get() = images.firstOrNull()
}
