package com.gabrielbotao.softwarevcv.data.mapper

import com.gabrielbotao.softwarevcv.data.remote.dto.AtelierContentDto
import com.gabrielbotao.softwarevcv.data.remote.dto.AtelierSectionDto
import com.gabrielbotao.softwarevcv.data.remote.dto.CollectionDto
import com.gabrielbotao.softwarevcv.data.remote.dto.ContactContentDto
import com.gabrielbotao.softwarevcv.data.remote.dto.FabricSpecDto
import com.gabrielbotao.softwarevcv.data.remote.dto.ImageRefDto
import com.gabrielbotao.softwarevcv.data.remote.dto.ProductDto
import com.gabrielbotao.softwarevcv.data.remote.dto.ProductVariantDto
import com.gabrielbotao.softwarevcv.domain.model.AtelierContent
import com.gabrielbotao.softwarevcv.domain.model.AtelierSection
import com.gabrielbotao.softwarevcv.domain.model.Collection
import com.gabrielbotao.softwarevcv.domain.model.ContactContent
import com.gabrielbotao.softwarevcv.domain.model.FabricSpec
import com.gabrielbotao.softwarevcv.domain.model.ImageRef
import com.gabrielbotao.softwarevcv.domain.model.Money
import com.gabrielbotao.softwarevcv.domain.model.Product
import com.gabrielbotao.softwarevcv.domain.model.ProductBadge
import com.gabrielbotao.softwarevcv.domain.model.ProductVariant
import com.gabrielbotao.softwarevcv.domain.model.Size

/** DTO → domain mapping (mandatory before anything leaves `data/`). Unknown badges are dropped. */

internal fun ImageRefDto.toDomain() = ImageRef(url = url, alt = alt, aspectRatio = aspectRatio)

internal fun FabricSpecDto.toDomain() = FabricSpec(material = material, origin = origin, care = care, notes = notes)

internal fun ProductVariantDto.toDomain() = ProductVariant(size = Size(size), colorName = colorName, available = available)

internal fun ProductDto.toDomain() = Product(
    id = id,
    name = name,
    collectionSlug = collectionSlug,
    price = Money(priceCents),
    images = images.map { it.toDomain() },
    shortDescription = shortDescription,
    fabric = fabric.toDomain(),
    variants = variants.map { it.toDomain() },
    badges = badges.mapNotNull { it.toBadgeOrNull() },
    buyUrl = buyUrl,
)

internal fun CollectionDto.toDomain() = Collection(
    slug = slug,
    title = title,
    subtitle = subtitle,
    cover = cover.toDomain(),
    productIds = productIds,
    story = story,
)

internal fun AtelierSectionDto.toDomain() = AtelierSection(title = title, body = body, image = image?.toDomain())

internal fun AtelierContentDto.toDomain() = AtelierContent(headline = headline, sections = sections.map { it.toDomain() })

internal fun ContactContentDto.toDomain() = ContactContent(
    whatsapp = whatsapp,
    instagram = instagram,
    email = email,
    whereToBuy = whereToBuy,
)

private fun String.toBadgeOrNull(): ProductBadge? =
    ProductBadge.entries.firstOrNull { it.name == this.trim().uppercase() }
