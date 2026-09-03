package com.gabrielbotao.softwarevcv.domain.model

/** One editorial section of the Atelier page (origin, atelier, durability). */
data class AtelierSection(
    val title: String,
    val body: String,
    val image: ImageRef? = null,
)

/** The Atelier / Sobre story content. See [[VCV Screens-and-UX]] §5. */
data class AtelierContent(
    val headline: String,
    val sections: List<AtelierSection>,
)

/** Contact + where-to-buy links (no form in the MVP — direct links only). §6. */
data class ContactContent(
    val whatsapp: String? = null,
    val instagram: String? = null,
    val email: String? = null,
    val whereToBuy: List<String> = emptyList(),
)
