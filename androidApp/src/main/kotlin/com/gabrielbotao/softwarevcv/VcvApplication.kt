package com.gabrielbotao.softwarevcv

import android.app.Application
import com.gabrielbotao.softwarevcv.core.di.initKoin

/** Android entrypoint — boots the shared Koin graph once at process start. */
class VcvApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
