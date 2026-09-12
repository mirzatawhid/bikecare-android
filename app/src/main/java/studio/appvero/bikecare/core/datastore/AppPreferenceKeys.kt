package studio.appvero.bikecare.core.datastore

import androidx.datastore.preferences.core.stringPreferencesKey
import studio.appvero.bikecare.core.common.Constants

internal object PreferenceKeys {
    val USER_ID = stringPreferencesKey(Constants.PREF_USER_ID)

    val LANGUAGE = stringPreferencesKey(Constants.PREF_LANGUAGE)

}