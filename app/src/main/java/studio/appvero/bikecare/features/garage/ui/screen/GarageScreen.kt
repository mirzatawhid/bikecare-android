package studio.appvero.bikecare.features.garage.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import studio.appvero.bikecare.R
import studio.appvero.bikecare.core.localization.localizedString
import studio.appvero.bikecare.features.garage.domain.model.Bike
import studio.appvero.bikecare.ui.components.BikeCareIconButton
import studio.appvero.bikecare.ui.components.BikeCarePrimaryButton
import studio.appvero.bikecare.ui.components.BikeCareTextField
import studio.appvero.bikecare.ui.theme.AppDimensions
import studio.appvero.bikecare.ui.theme.AppSpacing
import studio.appvero.bikecare.ui.theme.AppTheme

@Composable
fun GarageScreen(state: GarageUiState, addBikeForm: AddBikeFormState?, onEvent: (GarageEvent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppDimensions.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        item {
            Text(localizedString(R.string.nav_garage), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(AppSpacing.xs))
            Text(localizedString(R.string.garage_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when (state) {
            GarageUiState.Loading -> item {
                Column(
                    Modifier.fillMaxWidth().padding(AppSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                    Text(localizedString(R.string.garage_loading))
                }
            }
            is GarageUiState.Error -> item {
                GarageMessage(localizedString(state.message), isError = true) {
                    BikeCarePrimaryButton(
                        text = localizedString(R.string.garage_retry),
                        onClick = { onEvent(GarageEvent.Retry) },
                    )
                }
            }
            GarageUiState.Empty -> item {
                GarageMessage(localizedString(R.string.garage_empty)) {
                    Text(localizedString(R.string.garage_empty_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AddBikeButton { onEvent(GarageEvent.OpenAddBikeSheet) }
                }
            }
            is GarageUiState.Content -> {
                item { AddBikeButton { onEvent(GarageEvent.OpenAddBikeSheet) } }
                items(state.bikes, key = { it.id }) { BikeCard(it) }
            }
        }
    }

    if (addBikeForm != null) {
        AddBikeSheet(form = addBikeForm, onEvent = onEvent)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddBikeSheet(form: AddBikeFormState, onEvent: (GarageEvent) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    ModalBottomSheet(
        onDismissRequest = { onEvent(GarageEvent.DismissAddBikeSheet) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = AppDimensions.screenHorizontalPadding,
                    end = AppDimensions.screenHorizontalPadding,
                    bottom = AppSpacing.lg,
                ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    localizedString(R.string.garage_add_sheet_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                )
                BikeCareIconButton(
                    contentDescription = localizedString(R.string.garage_close_sheet),
                    onClick = { onEvent(GarageEvent.DismissAddBikeSheet) },
                    enabled = !form.isSaving,
                ) {
                    Text("×", style = MaterialTheme.typography.headlineSmall)
                }
            }
            BikeCareTextField(
                value = form.makeAndModel,
                onValueChange = { onEvent(GarageEvent.MakeAndModelChanged(it)) },
                label = localizedString(R.string.garage_make_model),
                placeholder = localizedString(R.string.garage_make_model_hint),
                required = true,
                error = if (form.errors.makeAndModel != null) localizedString(form.errors.makeAndModel) else null,
                enabled = !form.isSaving,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            )
            BikeCareTextField(
                value = form.nickname,
                onValueChange = { onEvent(GarageEvent.NicknameChanged(it)) },
                label = localizedString(R.string.garage_nickname_optional),
                placeholder = localizedString(R.string.garage_nickname_hint),
                enabled = !form.isSaving,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                BikeCareTextField(
                    value = form.modelYear,
                    onValueChange = { onEvent(GarageEvent.ModelYearChanged(it)) },
                    label = localizedString(R.string.garage_model_year),
                    modifier = Modifier.weight(1f),
                    required = true,
                    error = if (form.errors.modelYear != null) localizedString(form.errors.modelYear) else null,
                    enabled = !form.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                )
                BikeCareTextField(
                    value = form.engineCapacityCc,
                    onValueChange = { onEvent(GarageEvent.EngineCapacityChanged(it)) },
                    label = localizedString(R.string.garage_engine_optional),
                    modifier = Modifier.weight(1f),
                    error = if (form.errors.engineCapacityCc != null) localizedString(form.errors.engineCapacityCc) else null,
                    enabled = !form.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                )
            }
            BikeCareTextField(
                value = form.odometer,
                onValueChange = { onEvent(GarageEvent.OdometerChanged(it)) },
                label = localizedString(R.string.garage_odometer_input),
                required = true,
                error = if (form.errors.odometer != null) localizedString(form.errors.odometer) else null,
                enabled = !form.isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            )
            BikeCareTextField(
                value = form.registrationNumber,
                onValueChange = { onEvent(GarageEvent.RegistrationNumberChanged(it)) },
                label = localizedString(R.string.garage_registration_optional),
                error = if (form.errors.registrationNumber != null) localizedString(form.errors.registrationNumber) else null,
                enabled = !form.isSaving,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); onEvent(GarageEvent.SaveBike) }),
            )
            form.submissionError?.let {
                Text(
                    localizedString(it),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            BikeCarePrimaryButton(
                text = localizedString(R.string.garage_save_bike),
                onClick = { focusManager.clearFocus(); onEvent(GarageEvent.SaveBike) },
                isLoading = form.isSaving,
                contentDescription = localizedString(R.string.garage_save_bike),
            )
        }
    }
}

@Composable
private fun GarageMessage(
    message: String,
    isError: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(message, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun AddBikeButton(onClick: () -> Unit) {
    BikeCarePrimaryButton(
        text = localizedString(R.string.garage_add_bike),
        onClick = onClick,
    )
}

@Composable
private fun BikeCard(bike: Bike) {
    Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppDimensions.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text("${bike.brand} ${bike.model}", style = MaterialTheme.typography.titleLarge)
            bike.nickname?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(localizedString(R.string.garage_year, bike.year), color = MaterialTheme.colorScheme.onSurfaceVariant)
            bike.engineCapacityCc?.let { Text(localizedString(R.string.garage_engine, it)) }
            Text(bike.registrationNumber.ifBlank { localizedString(R.string.garage_unregistered) })
            HorizontalDivider()
            Text(localizedString(R.string.garage_odometer, bike.currentOdometer), style = MaterialTheme.typography.titleMedium)
            Text(
                localizedString(if (bike.isActive) R.string.garage_active else R.string.garage_inactive),
                color = if (bike.isActive) AppTheme.colors.success else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
