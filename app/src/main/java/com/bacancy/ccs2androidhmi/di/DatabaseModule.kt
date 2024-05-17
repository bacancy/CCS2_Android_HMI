package com.bacancy.ccs2androidhmi.di

import android.content.Context
import androidx.room.Room
import com.bacancy.ccs2androidhmi.db.AppDatabase
import com.bacancy.ccs2androidhmi.db.AppDatabase.Companion.MIGRATION_1_2
import com.bacancy.ccs2androidhmi.db.AppDatabase.Companion.MIGRATION_2_3
import com.bacancy.ccs2androidhmi.db.AppDatabase.Companion.MIGRATION_3_4
import com.bacancy.ccs2androidhmi.db.AppDatabase.Companion.MIGRATION_4_5
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(appContext, AppDatabase::class.java, "CCS2_HMI.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

}