package studio.appvero.bikecare.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val BikeTrackerShapes = Shapes(

    /*
     * Small components:
     * text fields, compact controls, small surfaces.
     */
    small = RoundedCornerShape(8.dp),

    /*
     * Medium components:
     * buttons, standard cards, list items.
     */
    medium = RoundedCornerShape(16.dp),

    /*
     * Large surfaces:
     * dashboard cards, major content containers.
     */
    large = RoundedCornerShape(24.dp)
)