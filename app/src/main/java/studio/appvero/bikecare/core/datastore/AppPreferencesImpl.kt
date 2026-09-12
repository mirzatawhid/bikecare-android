package studio.appvero.bikecare.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import studio.appvero.bikecare.core.localization.AppLanguage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppPreferences {

    override suspend fun saveLanguage(
        language: AppLanguage
    ) {
        dataStore.edit {
            it[PreferenceKeys.LANGUAGE] = language.tag
        }
    }

    override suspend fun getLanguage(): AppLanguage {

        val tag = dataStore.data.first()[PreferenceKeys.LANGUAGE] ?: AppLanguage.ENGLISH.tag

        return AppLanguage.fromTag(tag)
    }

}