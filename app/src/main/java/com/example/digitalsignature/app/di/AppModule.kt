package com.example.digitalsignature.app.di

import android.content.Context
import com.example.digitalsignature.app.App
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun provideApplication(app: App): App = app

    @Provides
    fun provideApplicationContext(app: App): Context = app.applicationContext
}