package studio.appvero.bikecare.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import studio.appvero.bikecare.features.garage.data.repository.BikeRepository
import studio.appvero.bikecare.features.garage.data.repository.FirebaseBikeRepository
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

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}