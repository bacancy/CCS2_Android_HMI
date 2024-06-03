package com.bacancy.ccs2androidhmi.di

import android.content.Context
import com.bacancy.ccs2androidhmi.db.AppDatabase
import com.bacancy.ccs2androidhmi.mqtt.MQTTClient
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants.MQTT_CLIENT_ID
import com.bacancy.ccs2androidhmi.mqtt.ServerConstants.MQTT_SERVER_URI
import com.bacancy.ccs2androidhmi.repository.MainRepository
import com.bacancy.ccs2androidhmi.util.PrefHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun providePrefHelper(@ApplicationContext context: Context): PrefHelper {
        return PrefHelper(context)
    }

    @Provides
    @Singleton
    fun provideMqttClient(@ApplicationContext context: Context): MQTTClient {
        return MQTTClient(
            context,
            MQTT_SERVER_URI,
            MQTT_CLIENT_ID
        )
    }
}