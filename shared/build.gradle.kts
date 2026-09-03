import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    android {
       namespace = "com.gabrielbotao.softwarevcv.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.okhttp) // VCV-6: Coil network engine
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // VCV-1 foundation: DI + coroutines (cross-cutting core).
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            // VCV-6 presentation: Koin Compose + koinViewModel().
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewModel)
            // VCV-3 components: Coil 3 for images. VCV-6: network loader (Ktor fetcher + per-platform engines below).
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            // VCV-4 navigation: Jetpack Navigation 3 (JetBrains multiplatform build).
            implementation(libs.navigation3.ui)
            // VCV-5 content: kotlinx.serialization for DTOs (bundled-JSON ContentSource).
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            // VCV-1 foundation: multiplatform test stack.
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
            implementation(libs.ktor.client.js) // VCV-6: Coil network engine
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp) // VCV-6: Coil network engine (Desktop)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin) // VCV-6: Coil network engine
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js) // VCV-6: Coil network engine (primary web target)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}