package uk.matvey.frobot

import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import uk.matvey.frobot.RockGardenCell.Frog
import uk.matvey.frobot.RockGardenCell.Rock

class RockGardenBoard(private val cells: List<List<RockGardenCell>>) {

    fun move(i: Int, j: Int): RockGardenBoard {
        return if (isReachableRock(i, j)) {
            val arr = fromString(this.serialize().replace('b', 'f')).cells.map { it.toTypedArray() }.toTypedArray()
            arr[i][j] = Frog
            return fromString(arr.joinToString(separator = "") { row -> row.joinToString(separator = "") { it.symbol.toString() } })
        } else {
            this
        }
    }

    fun serialize(): String {
        return cells.joinToString(separator = "") { row -> row.joinToString(separator = "") { it.symbol.toString() } }
    }

    fun toInlineKeyboard(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(
            *cells.mapIndexed { i, row ->
                row.mapIndexed { j, cell -> InlineKeyboardButton(cell.emoji).callbackData("$i$j") }
                    .toTypedArray()
            }.toTypedArray()
        )
    }

    private fun isReachableRock(i: Int, j: Int): Boolean {
        return when (cells[i][j]) {
            is Rock -> {
                listOfNotNull(
                    cells.getOrNull(i + 1)?.getOrNull(j + 2),
                    cells.getOrNull(i + 2)?.getOrNull(j + 1),
                    cells.getOrNull(i + 2)?.getOrNull(j - 1),
                    cells.getOrNull(i + 1)?.getOrNull(j - 2),
                    cells.getOrNull(i - 1)?.getOrNull(j - 2),
                    cells.getOrNull(i - 2)?.getOrNull(j - 1),
                    cells.getOrNull(i - 2)?.getOrNull(j + 1),
                    cells.getOrNull(i - 1)?.getOrNull(j + 2),
                ).any { it is Frog }
            }
            else -> false
        }
    }

    companion object {

        fun fromString(s: String): RockGardenBoard {
            return RockGardenBoard(
                s.chunked(8).map { row ->
                    row.toCharArray().map(RockGardenCell::fromSymbol)
                }
            )
        }
    }
}
