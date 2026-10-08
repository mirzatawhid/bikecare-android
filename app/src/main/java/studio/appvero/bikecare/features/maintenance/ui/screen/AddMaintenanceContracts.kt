package studio.appvero.bikecare.features.maintenance.ui.screen

import androidx.annotation.StringRes
import studio.appvero.bikecare.features.garage.domain.model.Bike
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceCategory
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceServiceType

enum class MaintenancePicker { BIKE, CATEGORY, SERVICE_TYPE, DATE, NEXT_DATE }

data class AddMaintenanceUiState(
    val bikes: List<Bike> = emptyList(),
    val bikesLoading: Boolean = true,
    @param:StringRes val bikesError: Int? = null,
    val bikeId: String = "",
    val title: String = "",
    val category: MaintenanceCategory = MaintenanceCategory.ENGINE,
    val serviceType: MaintenanceServiceType = MaintenanceServiceType.PREVENTIVE,
    val date: Long = 0,
    val odometer: String = "",
    val cost: String = "",
    val provider: String = "",
    val description: String = "",
    val nextServiceOdometer: String = "",
    val nextServiceDate: Long? = null,
    val openPicker: MaintenancePicker? = null,
    @param:StringRes val bikeError: Int? = null,
    @param:StringRes val titleError: Int? = null,
    @param:StringRes val dateError: Int? = null,
    @param:StringRes val odometerError: Int? = null,
    @param:StringRes val costError: Int? = null,
    @param:StringRes val providerError: Int? = null,
    @param:StringRes val descriptionError: Int? = null,
    @param:StringRes val nextOdometerError: Int? = null,
    @param:StringRes val nextDateError: Int? = null,
    @param:StringRes val error: Int? = null,
    val isSaving: Boolean = false,
)

sealed interface AddMaintenanceEvent {
    data class BikeSelected(val id: String) : AddMaintenanceEvent
    data class TitleChanged(val value: String) : AddMaintenanceEvent
    data class CategorySelected(val value: MaintenanceCategory) : AddMaintenanceEvent
    data class ServiceTypeSelected(val value: MaintenanceServiceType) : AddMaintenanceEvent
    data class DateSelected(val value: Long) : AddMaintenanceEvent
    data class OdometerChanged(val value: String) : AddMaintenanceEvent
    data class CostChanged(val value: String) : AddMaintenanceEvent
    data class ProviderChanged(val value: String) : AddMaintenanceEvent
    data class DescriptionChanged(val value: String) : AddMaintenanceEvent
    data class NextOdometerChanged(val value: String) : AddMaintenanceEvent
    data class NextDateSelected(val value: Long?) : AddMaintenanceEvent
    data class OpenPicker(val value: MaintenancePicker) : AddMaintenanceEvent
    data object ClosePicker : AddMaintenanceEvent
    data object RetryBikes : AddMaintenanceEvent
    data object Submit : AddMaintenanceEvent
    data object Back : AddMaintenanceEvent
    data object EffectHandled : AddMaintenanceEvent
}

sealed interface AddMaintenanceSideEffect {
    data object NavigateBack : AddMaintenanceSideEffect
}
