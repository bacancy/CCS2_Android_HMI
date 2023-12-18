package com.bacancy.ccs2androidhmi.di

import com.bacancy.ccs2androidhmi.db.AppDatabase
import com.bacancy.ccs2androidhmi.repository.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMainRepository(appDatabase: AppDatabase): MainRepository {
        return MainRepository(appDatabase)
    }

}