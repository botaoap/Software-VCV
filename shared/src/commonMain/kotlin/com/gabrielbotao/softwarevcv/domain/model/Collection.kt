package com.gabrielbotao.softwarevcv.domain.model

/** A collection / lookbook: a cover, an ordered set of product ids, and optional editorial copy. §1. */
data class Collection(
    val slug: String,
    val title: String,
    val subtitle: String?,
    val cover: ImageRef,
    val productIds: List<String>,
    val story: String? = null,
)
