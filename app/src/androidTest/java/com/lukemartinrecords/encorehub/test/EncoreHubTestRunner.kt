package com.lukemartinrecords.encorehub.test

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class EncoreHubTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application = super.newApplication(cl, EncoreHubTestApplication::class.java.name, context)
}

class EncoreHubTestApplication : Application()
