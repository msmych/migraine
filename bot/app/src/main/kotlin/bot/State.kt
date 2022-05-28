package bot

import bot.State.Transition
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Update

enum class State(val transition: Transition = Transition { _, _ -> null }) {

    SLEEPING(SleepingTransition),
    IN_GARAGE(InGarageTransition),
    ON_THE_STREET(OnTheStreetTransition),
    AT_THE_ICE_CREAM_TRUCK(AtTheIceCreamTruckTransition),
    GOT_DESSERT(GotDessertTransition),
    FINAL
    ;

    fun interface Transition {

        fun next(update: Update, bot: TelegramBot): State?
    }
}