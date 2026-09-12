package studio.appvero.bikecare.core.localization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import studio.appvero.bikecare.core.datastore.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    private val appPreferences: AppPreferences
) {

    private val _language =
        MutableStateFlow(AppLanguage.ENGLISH)

    val language = _language.asStateFlow()

    suspend fun initialize() {
        _language.value = appPreferences.getLanguage()
    }

    suspend fun setLanguage(language: AppLanguage) {
        appPreferences.saveLanguage(language)
        _language.value = language
    }
}