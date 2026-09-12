package studio.appvero.bikecare.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import studio.appvero.bikecare.core.common.Constants
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreProviderModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {

        return PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile(Constants.PREF_BIKECARE_FILE)
            }
        )
    }
}