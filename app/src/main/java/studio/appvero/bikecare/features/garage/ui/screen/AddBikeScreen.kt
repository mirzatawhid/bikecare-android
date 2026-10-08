package studio.appvero.bikecare.features.garage.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.ui.components.BikeCareButton
import studio.appvero.bikecare.ui.components.BikeCareTextField
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.AppSpacing
import studio.appvero.bikecare.ui.theme.BikeCareTheme

@Composable
fun AddBikeScreen(state: AddBikeUiState, onEvent: (AddBikeEvent) -> Unit) {
    val focus = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(localizedString(R.string.garage_add_bike), style = MaterialTheme.typography.headlineLarge)
        Text(localizedString(R.string.add_bike_form_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)

        BikeCareTextField(
            value = state.brand,
            onValueChange = { onEvent(AddBikeEvent.BrandChanged(it)) },
            label = localizedString(R.string.add_bike_brand),
            enabled = !state.isSaving,
            isError = state.brandError != null,
            supportingText = state.brandError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
        )
        BikeCareTextField(
            value = state.model,
            onValueChange = { onEvent(AddBikeEvent.ModelChanged(it)) },
            label = localizedString(R.string.add_bike_model),
            enabled = !state.isSaving,
            isError = state.modelError != null,
            supportingText = state.modelError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
        )
        BikeCareTextField(
            value = state.year,
            onValueChange = { onEvent(AddBikeEvent.YearChanged(it)) },
            label = localizedString(R.string.add_bike_year),
            enabled = !state.isSaving,
            isError = state.yearError != null,
            supportingText = state.yearError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
        )
        BikeCareTextField(
            value = state.registrationNumber,
            onValueChange = { onEvent(AddBikeEvent.RegistrationChanged(it)) },
            label = localizedString(R.string.add_bike_registration),
            enabled = !state.isSaving,
            isError = state.registrationError != null,
            supportingText = state.registrationError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
        )
        BikeCareTextField(
            value = state.initialOdometer,
            onValueChange = { onEvent(AddBikeEvent.InitialOdometerChanged(it)) },
            label = localizedString(R.string.add_bike_initial_odometer),
            enabled = !state.isSaving,
            isError = state.initialOdometerError != null,
            supportingText = state.initialOdometerError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
        )
        BikeCareTextField(
            value = state.currentOdometer,
            onValueChange = { onEvent(AddBikeEvent.CurrentOdometerChanged(it)) },
            label = localizedString(R.string.add_bike_current_odometer),
            enabled = !state.isSaving,
            isError = state.currentOdometerError != null,
            supportingText = state.currentOdometerError?.let { localizedString(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); onEvent(AddBikeEvent.Submit) }),
        )

        state.error?.let { error ->
            Text(localizedString(error), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(AppSpacing.xs))
        BikeCareButton(
            text = localizedString(R.string.add_bike_save),
            onClick = { onEvent(AddBikeEvent.Submit) },
            enabled = !state.isSaving,
            isLoading = state.isSaving,
        )
        BikeCareButton(
            text = localizedString(R.string.garage_back),
            onClick = { onEvent(AddBikeEvent.Back) },
            enabled = !state.isSaving,
            outlined = true,
        )
    }
}

@Preview(
    name = "Add Bike - Filled",
    showBackground = true,
    showSystemUi = true,
)
@Composable
private fun AddBikeScreenFilledPreview() {
    BikeCareTheme {
        AddBikeScreen(
            state = AddBikeUiState(
                brand = "Yamaha",
                model = "FZ-S FI V3",
                year = "2024",
                registrationNumber = "DHAKA METRO-LA-12-3456",
                initialOdometer = "1000",
                currentOdometer = "8500",
            ),
            onEvent = {},
        )
    }
}

@Preview(
    name = "Add Bike - Validation Errors",
    showBackground = true,
    showSystemUi = true,
)
@Composable
private fun AddBikeScreenErrorPreview() {
    BikeCareTheme {
        AddBikeScreen(
            state = AddBikeUiState(
                brand = "",
                model = "",
                year = "1800",
                registrationNumber = "",
                initialOdometer = "10000",
                currentOdometer = "5000",
                brandError = R.string.add_bike_required,
                modelError = R.string.add_bike_required,
                yearError = R.string.add_bike_year_invalid,
                registrationError = R.string.add_bike_registration_too_long,
                currentOdometerError = R.string.add_bike_current_odometer_invalid,
            ),
            onEvent = {},
        )
    }
}
