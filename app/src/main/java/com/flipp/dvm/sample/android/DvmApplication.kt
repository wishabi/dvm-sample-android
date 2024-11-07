package com.flipp.dvm.sample.android

import android.app.Application
import com.flipp.dvm.sdk.android.external.DvmSdk

class DvmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DvmSdk.initialize(
            clientToken = "", // TODO: Client token provided by Flipp goes here
            context = this,
        )
    }
}
