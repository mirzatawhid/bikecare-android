package studio.appvero.bikecare.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import studio.appvero.bikecare.features.garage.data.repository.BikeRepository
import studio.appvero.bikecare.features.garage.data.repository.FirebaseBikeRepository
import studio.appvero.bikecare.features.maintenance.data.repository.FirebaseMaintenanceRepository
import studio.appvero.bikecare.features.maintenance.data.repository.MaintenanceRepository
import studio.appvero.bikecare.features.fuel.data.repository.FuelLogRepository
import studio.appvero.bikecare.features.fuel.data.repository.FirebaseFuelLogRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import studio.appvero.bikecare.features.auth.data.repository.AuthRepository
import studio.appvero.bikecare.features.auth.data.repository.FirebaseAuthRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(repository: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindBikeRepository(repository: FirebaseBikeRepository): BikeRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(repository: FirebaseMaintenanceRepository): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindFuelLogRepository(repository: FirebaseFuelLogRepository): FuelLogRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
        }
    }
}
