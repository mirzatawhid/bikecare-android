package studio.appvero.bikecare.features.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}