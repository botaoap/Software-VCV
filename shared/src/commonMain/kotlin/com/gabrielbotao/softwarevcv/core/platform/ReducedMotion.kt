package com.gabrielbotao.softwarevcv.core.platform

/**
 * Whether the user's OS/browser asks for **reduced motion** (`prefers-reduced-motion: reduce` on the web).
 * The web targets read the media query; other targets return `false` for now. Read at theme composition
 * and provided as `LocalReducedMotion` so components can drop non-essential animation. See [[VCV-33]].
 */
expect fun prefersReducedMotion(): Boolean
