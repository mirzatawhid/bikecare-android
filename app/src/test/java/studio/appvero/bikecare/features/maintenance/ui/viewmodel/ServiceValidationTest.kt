package studio.appvero.bikecare.features.maintenance.ui.viewmodel

import org.junit.Assert.*
import org.junit.Test
import studio.appvero.bikecare.features.garage.domain.model.Bike
import studio.appvero.bikecare.features.maintenance.data.repository.*
import studio.appvero.bikecare.features.maintenance.ui.screen.LogServiceFormState
import studio.appvero.bikecare.features.maintenance.ui.screen.ServiceField
import studio.appvero.bikecare.features.maintenance.ui.screen.ServiceField.*
import java.time.LocalDate

class ServiceValidationTest {
    private val bike = Bike("bike", "Honda", "Hornet", year = 2025,
        registrationNumber = "", initialOdometer = 100, currentOdometer = 8200)
    private fun form(reminder: Boolean = false, vararg fields: Pair<ServiceField, String>) = LogServiceFormState(
        id = "service", bike = bike, maintenanceId = "task", reminderEnabled = reminder,
        fields = mapOf(Name to "Oil change", ServiceDate to "2026-01-31", Odometer to "8200") + fields,
    )

    @Test fun serviceWithoutReminderHasNoSchedule() {
        val log = requireNotNull(form().validateService().log)
        validateServiceLog(log)
        assertNull(log.reminder)
        assertNull(log.totalCostPoisha)
        assertEquals("task", log.maintenanceId)
        assertEquals("bike", log.bikeId)
    }
    @Test fun serviceWithReminderCalculatesFromCompletion() {
        val log = requireNotNull(form(true, RepeatKm to "1000", RepeatDays to "30").validateService().log)
        validateServiceLog(log)
        assertEquals(9200L, log.reminder!!.dueOdometerKm)
        assertEquals(LocalDate.of(2026, 3, 2), log.reminder.dueDate)
    }
    @Test fun explicitThresholdsTakePrecedence() {
        val reminder = form(true, RepeatKm to "1000", RepeatDays to "30", DueOdometer to "9000",
            DueDate to "2026-02-15").validateService().log!!.reminder!!
        assertEquals(9000L, reminder.dueOdometerKm)
        assertEquals(LocalDate.of(2026, 2, 15), reminder.dueDate)
    }
    @Test fun disabledReminderIgnoresInvalidFields() {
        assertNotNull(form(false, RepeatDays to "bad", DueDate to "invalid", RepeatKm to "-1").validateService().log)
    }
    @Test fun atLeastOneEffectiveThresholdIsRequiredWhenEnabled() {
        val errors = form(true).validateService().errors
        assertTrue(errors.containsKey(ReminderDue))
    }
    @Test fun mileageOnlyReminderIsValid() {
        val reminder = form(true, DueOdometer to "9000").validateService().log!!.reminder!!
        assertEquals(9000L, reminder.dueOdometerKm)
        assertNull(reminder.dueDate)
    }
    @Test fun dateOnlyReminderIsValid() {
        val reminder = form(true, DueDate to "2026-03-01").validateService().log!!.reminder!!
        assertEquals(LocalDate.of(2026, 3, 1), reminder.dueDate)
        assertNull(reminder.dueOdometerKm)
    }
    @Test fun oneRepeatIntervalCanSupplyTheOnlyThreshold() {
        val reminder = form(true, RepeatKm to "1000").validateService().log!!.reminder!!
        assertEquals(9200L, reminder.dueOdometerKm)
        assertNull(reminder.dueDate)
    }
    @Test fun partialManualAndCalculatedThresholds() {
        val reminder = form(true, DueOdometer to "9000", RepeatDays to "1").validateService().log!!.reminder!!
        assertEquals(LocalDate.of(2026, 2, 1), reminder.dueDate)
    }
    @Test fun requiredFieldsRejectBlankAndInvalidValues() {
        for ((field, value) in listOf(Name to " ", ServiceDate to "", ServiceDate to "2026-02-30",
            Odometer to "", Odometer to "-1", Odometer to "1.5", Odometer to "abc")) {
            assertTrue("$field=$value", form(false, field to value).validateService().errors.containsKey(field))
        }
    }
    @Test fun negativeAndNonNumericCostsRejected() {
        for (cost in listOf("-1", "abc", "1.001", "1000000001", "NaN")) {
            assertTrue(form(false, Cost to cost).validateService().errors.containsKey(Cost))
        }
    }
    @Test fun moneyRetainsExactDecimalAmount() {
        assertEquals(12345L, form(false, Cost to "123.45").validateService().log!!.totalCostPoisha)
        assertEquals(0L, form(false, Cost to "0").validateService().log!!.totalCostPoisha)
    }
    @Test fun repeatsMustBePositiveEvenWithManualThresholds() {
        val errors = form(true, DueDate to "2026-03-01", DueOdometer to "9000",
            RepeatKm to "0", RepeatDays to "-1").validateService().errors
        assertTrue(errors.containsKey(RepeatKm))
        assertTrue(errors.containsKey(RepeatDays))
    }
    @Test fun calculatedThresholdsRespectStorageBounds() {
        val errors = form(true, Odometer to "10000000", RepeatKm to "1",
            ServiceDate to "9999-12-31", RepeatDays to "1").validateService().errors
        assertTrue(errors.containsKey(DueOdometer))
        assertTrue(errors.containsKey(DueDate))
    }
    @Test fun leapYearCalculation() {
        assertEquals(LocalDate.of(2028, 2, 29), form(true, ServiceDate to "2028-02-28",
            RepeatDays to "1", RepeatKm to "1").validateService().log!!.reminder!!.dueDate)
    }
    @Test fun odometerOnlyIncreases() {
        assertEquals(9000L, serviceOdometer(8200, 9000))
        assertEquals(8200L, serviceOdometer(8200, 8100))
        assertEquals(8200L, serviceOdometer(8200, 8200))
    }
    @Test fun zeroOdometerAndManualThresholdAreValid() {
        assertNotNull(form(true, Odometer to "0", DueOdometer to "0", DueDate to "2026-02-01").validateService().log)
    }
    @Test fun repositoryRejectsInvalidCallerData() {
        val valid = form().validateService().log!!
        for (log in listOf(valid.copy(bikeId = "../bike"), valid.copy(odometerKm = -1), valid.copy(name = ""))) {
            try { validateServiceLog(log); fail("Invalid log accepted") }
            catch (error: MaintenanceException) { assertEquals(MaintenanceFailure.InvalidData, error.failure) }
        }
    }
    @Test fun repositoryRejectsReminderWithoutAnyThreshold() {
        val valid = form(true, DueOdometer to "9000").validateService().log!!
        try {
            validateServiceLog(valid.copy(reminder = valid.reminder!!.copy(dueDate = null, dueOdometerKm = null)))
            fail("Reminder without a due threshold accepted")
        } catch (error: MaintenanceException) {
            assertEquals(MaintenanceFailure.InvalidData, error.failure)
        }
    }
}
