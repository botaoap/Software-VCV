package com.gabrielbotao.softwarevcv.domain.model

import kotlin.jvm.JvmInline

/**
 * A clothing size in the VCV grade (38–52, step 2). Validated on construction — the grade is a **domain
 * rule**, not a UI string. See [[Content-Model]] §2.
 */
@JvmInline
value class Size(val value: Int) {
    init {
        require(value in 34..60) { "Size out of range (34..60): $value" }
    }
}
