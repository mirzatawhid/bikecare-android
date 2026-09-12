package studio.appvero.bikecare.core.datastore

import studio.appvero.bikecare.core.localization.AppLanguage

interface AppPreferences {

    suspend fun saveLanguage(language: AppLanguage)

    suspend fun getLanguage(): AppLanguage
}