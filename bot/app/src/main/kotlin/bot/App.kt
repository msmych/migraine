package bot

import bot.State.SLEEPING
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL

fun main(vararg args: String) {
    val bot = TelegramBot(args[0])
    val keyValueStore = keyValueStore()
    bot.setUpdatesListener { updates ->
        updates.forEach { update ->
            val from = update.from()
            val state = keyValueStore.get(from.id()) ?: keyValueStore.put(from.id(), SLEEPING)
            val next = state.transition.next(update, bot)
            if (next != null) {
                keyValueStore.put(from.id(), next)
            }
        }
        CONFIRMED_UPDATES_ALL
    }
    println("Поехали!")
}

private fun keyValueStore(): HashMapKeyValueStore<Long, State> {
    return HashMapKeyValueStore()
}
