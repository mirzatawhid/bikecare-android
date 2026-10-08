package studio.appvero.bikecare.features.garage.ui.components

import studio.appvero.bikecare.features.garage.domain.model.Bike

fun previewBike(
    id: String = "bike-1",
    brand: String = "Yamaha",
    model: String = "R15 V4",
    year: Int = 2024,
    registrationNumber: String = "DHAKA METRO-LA-12-3456",
    initialOdometer: Long = 1_500L,
    currentOdometer: Long = 8_750L,
    imageUrl: String? = null,
) = Bike(
    id = id,
    brand = brand,
    model = model,
    year = year,
    registrationNumber = registrationNumber,
    initialOdometer = initialOdometer,
    currentOdometer = currentOdometer,
    imageUrl = imageUrl,
    createdAt = 0L,
    updatedAt = 0L,
    isActive = true,
)