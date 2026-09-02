package com.gabrielbotao.softwarevcv.core.logger

/**
 * Cross-cutting logging abstraction. Code logs through this interface — never a platform log API
 * directly — so the sink can be swapped per platform or in tests. See [[KMP-Clean-Architecture]] §2.
 */
interface AppLogger {
    fun d(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * Default multiplatform logger backed by [println]. Adequate for the MVP on every target
 * (Wasm/JS/JVM/Android/iOS); a platform-native sink can replace it later via Koin.
 */
class PrintAppLogger : AppLogger {
    override fun d(tag: String, message: String) {
        println("D/$tag: $message")
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        println("E/$tag: $message${throwable?.let { " — ${it.message}" } ?: ""}")
    }
}
