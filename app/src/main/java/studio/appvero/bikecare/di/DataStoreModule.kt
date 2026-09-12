package studio.appvero.bikecare.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import studio.appvero.bikecare.core.datastore.AppPreferences
import studio.appvero.bikecare.core.datastore.AppPreferencesImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {

    @Binds
    @Singleton
    abstract fun bindAppPreferences(
        impl: AppPreferencesImpl
    ): AppPreferences
}