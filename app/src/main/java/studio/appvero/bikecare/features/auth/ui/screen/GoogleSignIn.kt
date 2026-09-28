package studio.appvero.bikecare.features.auth.ui.screen

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CancellationException
import studio.appvero.bikecare.R

// This is the Android account-picker UI boundary. Firebase authentication stays in the repository.
internal suspend fun requestGoogleSignIn(
    context: Context,
    onToken: (String) -> Unit,
    onFailure: (Int?) -> Unit,
) {
    try {
        // The resource is generated only when google-services.json contains a web OAuth client.
        val resource = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val clientId = if (resource != 0) context.getString(resource) else ""
        if (clientId.isBlank()) {
            onFailure(R.string.auth_google_setup)
            return
        }
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
            .build()
        val credential = CredentialManager.create(context).getCredential(context, request).credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            onToken(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            onFailure(R.string.auth_google_error)
        }
    } catch (_: GetCredentialCancellationException) {
        onFailure(null)
    } catch (_: NoCredentialException) {
        onFailure(R.string.auth_google_unavailable)
    } catch (cancelled: CancellationException) {
        onFailure(null)
        throw cancelled
    } catch (_: GoogleIdTokenParsingException) {
        onFailure(R.string.auth_google_error)
    } catch (_: GetCredentialException) {
        onFailure(R.string.auth_google_error)
    } catch (_: Exception) {
        onFailure(R.string.auth_google_error)
    }
}
