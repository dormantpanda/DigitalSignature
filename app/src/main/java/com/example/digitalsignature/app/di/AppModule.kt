package com.example.digitalsignature.app.di

import android.content.Context
import com.example.digitalsignature.app.App
import com.example.digitalsignature.app.services.BiometricService
import com.example.digitalsignature.app.services.FilesManager
import com.example.digitalsignature.data.Pref
import com.example.digitalsignature.data.Store
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideApplication(app: App): App = app

    @Provides
    fun provideApplicationContext(app: App): Context = app.applicationContext

    @Provides
    @Singleton
    fun providePreferences(
        @ApplicationContext context: Context,
        gson: Gson
    ): Pref = Pref(context, gson)

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun provideFilesManager(
        @ApplicationContext context: Context
    ) = FilesManager(context)

    @Provides
    @Singleton
    fun provideBiometricService(
        @ApplicationContext context: Context
    ) = BiometricService(context)

    @Provides
    @Singleton
    fun provideKeyStore() = Store()
}