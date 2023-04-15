package uk.matvey.frobot

import uk.matvey.frobot.Frobot.BatteryLevel.LOW
import uk.matvey.frobot.FrobotState.BATTERY_LOW
import uk.matvey.frobot.FrobotState.FIRE_ROCKS

class FrobotStateResolver {

    fun resolveState(frobot: Frobot): FrobotState {
        return when {
            frobot.batteryLevel == LOW -> BATTERY_LOW
            else -> FIRE_ROCKS
        }
    }
}
