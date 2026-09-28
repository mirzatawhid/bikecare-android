package studio.appvero.bikecare.features.auth.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.createSupabaseClient
import studio.appvero.bikecare.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton
import io.ktor.client.engine.android.Android


@Singleton
class RecoverySupabaseClientFactory @Inject constructor() {

    fun create(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            httpEngine = Android.create()

            install(Auth.Companion) {
                minimalConfig()
            }
        }
    }
}