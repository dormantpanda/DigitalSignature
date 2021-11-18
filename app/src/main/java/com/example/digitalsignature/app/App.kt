package com.example.digitalsignature.app

import android.app.Application
import com.example.digitalsignature.app.services.FilesManager
import com.example.digitalsignature.data.Pref
import com.example.digitalsignature.data.Store
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App: Application() {
    @Inject
    lateinit var prefs: Pref

    @Inject
    lateinit var keyStore: Store

    @Inject
    lateinit var filesManager: FilesManager
}