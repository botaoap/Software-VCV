package com.gabrielbotao.softwarevcv.core.platform

/**
 * Thin platform seam. The *only* thing that varies per target is the platform's name/identity;
 * business logic never lives behind `expect`/`actual`. See [[KMP-Clean-Architecture]] §4.
 */
interface Platform {
    val name: String
}

/** Returns the current [Platform]. Implemented per source set (`actual`). */
expect fun getPlatform(): Platform
