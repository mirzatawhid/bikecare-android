package studio.appvero.bikecare.features.maintenance.ui.viewmodel

import org.junit.Assert.*
import org.junit.Test
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceItem
import studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceStatus.*
import java.time.LocalDate

class MaintenanceStatusTest {
    private val today = LocalDate.of(2026, 10, 4)
    private val odometer = 5_000L
    private fun item(days: Long, km: Long, id: String = "task") = MaintenanceItem(
        id, "bike", "Service", today.plusDays(days), odometer + km, 0, 0,
    )
    private fun status(days: Long, km: Long) = item(days, km).assess(today, odometer).status

    @Test fun overdueByDate() = assertEquals(OVERDUE, status(-1, 5_000))
    @Test fun overdueByOdometer() = assertEquals(OVERDUE, status(100, -1))
    @Test fun dueSoonByDate() = assertEquals(DUE_SOON, status(10, 5_000))
    @Test fun dueSoonByOdometer() = assertEquals(DUE_SOON, status(100, 500))
    @Test fun upToDate() = assertEquals(UP_TO_DATE, status(31, 1_001))
    @Test fun exactDueDateIsOverdue() = assertEquals(OVERDUE, status(0, 5_000))
    @Test fun exactDueOdometerIsOverdue() = assertEquals(OVERDUE, status(100, 0))
    @Test fun exactWarningBoundariesAreDueSoon() {
        assertEquals(DUE_SOON, status(DUE_SOON_DAYS, 5_000))
        assertEquals(DUE_SOON, status(100, DUE_SOON_KM))
    }

    @Test fun remainingDaysAndKmKeepTheirSigns() {
        val remaining = item(15, 550).assess(today, odometer)
        assertEquals(15L, remaining.remainingDays)
        assertEquals(550L, remaining.remainingKm)
        val overdue = item(-3, -125).assess(today, odometer)
        assertEquals(-3L, overdue.remainingDays)
        assertEquals(-125L, overdue.remainingKm)
    }

    @Test fun priorityPutsOverdueBeforeDueSoonBeforeUpcoming() {
        val ordered = listOf(item(100, 5_000, "upcoming"), item(5, 2_000, "soon"), item(-1, 2_000, "overdue"))
            .map { it.assess(today, odometer) }.byPriority()
        assertEquals(listOf("overdue", "soon", "upcoming"), ordered.map { it.item.id })
    }

    @Test fun priorityUsesMostUrgentThresholdWithinStatus() {
        val ordered = listOf(item(-1, 2_000, "date"), item(50, -1_000, "distance"))
            .map { it.assess(today, odometer) }.byPriority()
        assertEquals("distance", ordered.first().item.id)
    }

    @Test fun nearestUpcomingUsesBothThresholds() {
        val ordered = listOf(item(90, 5_000, "later"), item(60, 1_100, "distance"), item(31, 5_000, "date"))
            .map { it.assess(today, odometer) }.byPriority()
        assertEquals(listOf("date", "distance", "later"), ordered.map { it.item.id })
    }

    @Test fun priorityTiesAreStableAndEmptyListHasNoPriority() {
        assertTrue(emptyList<studio.appvero.bikecare.features.maintenance.domain.model.MaintenanceAssessment>().byPriority().isEmpty())
        assertEquals("a", listOf(item(10, 500, "b"), item(10, 500, "a"))
            .map { it.assess(today, odometer) }.byPriority().first().item.id)
    }

    @Test fun progressUsesClosestWarningWindowAndIsClamped() {
        assertEquals(0f, item(90, 5_000).assess(today, odometer).dueWindowProgress, 0.001f)
        assertEquals(0.5f, item(15, 2_000).assess(today, odometer).dueWindowProgress, 0.001f)
        assertEquals(0.75f, item(20, 250).assess(today, odometer).dueWindowProgress, 0.001f)
        assertEquals(1f, item(-1, 2_000).assess(today, odometer).dueWindowProgress, 0.001f)
        assertEquals(1f, item(90, 0).assess(today, odometer).dueWindowProgress, 0.001f)
    }

    @Test fun statusRespondsToDateAndOdometerChanges() {
        val task = item(31, 1_001)
        assertEquals(UP_TO_DATE, task.assess(today, odometer).status)
        assertEquals(DUE_SOON, task.assess(today.plusDays(1), odometer).status)
        assertEquals(OVERDUE, task.assess(today, odometer + 1_001).status)
    }

    @Test fun calendarDaysWorkAcrossLeapDay() {
        val task = item(1, 2_000).copy(dueDate = LocalDate.of(2028, 3, 1))
        assertEquals(2L, task.assess(LocalDate.of(2028, 2, 28), odometer).remainingDays)
    }
}
