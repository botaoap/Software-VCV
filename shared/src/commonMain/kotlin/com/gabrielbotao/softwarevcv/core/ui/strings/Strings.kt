package com.gabrielbotao.softwarevcv.core.ui.strings

/**
 * The single place for user-facing pt-BR copy. Centralised for maintenance and to be i18n-ready
 * (swap this object for a locale-aware provider later — screens reference `Strings.*`, never literals).
 * See the stateless-screens standard ([[VCV-28]]).
 */
object Strings {

    object Brand {
        const val name = "VCV — Veste Com Você"
        const val location = "Gaspar · Vale do Itajaí"
        val promo = listOf(
            "Ateliê próprio em Gaspar · Vale do Itajaí",
            "Envio para todo o Brasil",
            "Atendimento direto no WhatsApp",
            "Poucas peças, bem feitas — feitas rolo a rolo",
        )
    }

    object Common {
        const val retry = "Tentar de novo"
        const val seeCatalog = "Ver catálogo"
        const val soon = "Em breve."
        const val loadError = "Não foi possível carregar. Tente novamente."
    }

    object Nav {
        const val collections = "Coleções"
        const val catalog = "Catálogo"
        const val atelier = "Atelier"
        const val contact = "Contato"
        const val cart = "Sacola"
        fun cart(count: Int) = if (count > 0) "Sacola ($count)" else cart
    }

    object Home {
        const val heroTitle = "Veste Com Você"
        const val heroSubtitle = "De Gaspar, Vale do Itajaí — poucas peças, bem feitas."
        const val heroCta = "Ver catálogo"
        const val heroImageAlt = "Peças e ateliê da VCV, moda de Gaspar"
        const val featured = "Peças em destaque"
        const val atelierTitle = "Do ateliê"
        const val atelierSubtitle = "Ateliê próprio em Gaspar · produção rolo a rolo"
        const val atelierCta = "Conheça o ateliê"
        val trust = listOf(
            "Ateliê próprio" to "Produção em Gaspar, SC",
            "Ficha técnica" to "Tecido e origem em cada peça",
            "Envio nacional" to "Para todo o Brasil",
            "Atendimento" to "Direto no WhatsApp",
        )
    }

    object Catalog {
        const val title = "Catálogo"
        const val subtitle = "Todas as peças"
        const val empty = "Em breve novas peças."
        const val notFound = "Coleção não encontrada."
        const val sortPrefix = "Ordenar por: "
    }

    object Collections {
        const val title = "Coleções"
        const val empty = "Em breve novas coleções."
    }

    object Product {
        const val sizes = "Tamanhos"
        const val selectSize = "Selecione um tamanho."
        const val addToCart = "Adicionar à sacola"
        const val added = "Adicionado à sacola ✓"
        const val buyDirect = "Comprar direto"
        const val spec = "Ficha técnica"
        const val specMaterial = "Material"
        const val specOrigin = "Origem"
        const val specCare = "Cuidados"
        const val specNotes = "Notas"
        const val notFoundTitle = "Produto não encontrado"
        const val notFoundBody = "A peça que você procura não está disponível."
    }

    object Contact {
        const val title = "Contato"
        const val subtitle = "Fale com a VCV — atendimento direto, de Gaspar pra você."
        const val whereToBuy = "Onde comprar"
        val whatsapp = "WhatsApp" to "Chame a gente por aqui"
        val instagram = "Instagram" to "Veja as novidades"
        val email = "E-mail" to "Escreva pra gente"
    }

    object Cart {
        const val title = "Sacola"
        const val empty = "Sua sacola está vazia."
        const val subtotal = "Subtotal"
        const val checkout = "Finalizar compra"
        const val remove = "Remover"
        fun size(value: Int) = "Tamanho $value"
    }

    object Checkout {
        const val title = "Checkout"
        const val name = "Nome*"
        const val phone = "WhatsApp / telefone"
        const val note = "Observação (opcional)"
        const val submit = "Finalizar pedido"
        const val submitting = "Enviando…"
        const val subtotal = "Subtotal"
        const val backToCart = "Voltar à sacola"
        const val backToStore = "Voltar à loja"
        const val paymentNote =
            "Pagamento e entrega são combinados no atendimento. O checkout/pagamento real será " +
                "definido com o backend próprio ou a plataforma escolhida."
        const val sentTitle = "Pedido enviado!"
        const val sentBody =
            "Enviamos seu pedido para o nosso WhatsApp. Conclua a conversa por lá para combinar " +
                "pagamento e entrega."
        fun orderRef(ref: String) = "Número do pedido: $ref"
    }
}
