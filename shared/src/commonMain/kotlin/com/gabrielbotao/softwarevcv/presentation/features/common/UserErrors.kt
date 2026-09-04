package com.gabrielbotao.softwarevcv.presentation.features.common

/**
 * Maps a data/load failure to a user-facing PT-BR message. Never surface a raw exception string
 * (e.g. `Missing resource with path …`) to the user — it leaks internals and reads as broken. A genuine
 * "not found" (an empty `first { }` lookup → [NoSuchElementException]) keeps its specific message;
 * every other failure (resource/network/parse) becomes a generic, retryable load error. See VCV-15.
 */
fun Throwable.toUserMessage(notFound: String = "Não encontrado."): String = when (this) {
    is NoSuchElementException -> notFound
    else -> "Não foi possível carregar. Tente novamente."
}
