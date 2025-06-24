package uk.matvey.frobot

import uk.matvey.frobot.RockGardenCell.Frog
import uk.matvey.frobot.RockGardenCell.Rock
import uk.matvey.frobot.RockGardenCell.TreasureMap
import uk.matvey.telek.ReplyMarkup

class RockGardenBoard(private val cells: List<List<RockGardenCell>>) {

    fun cellAt(i: Int, j: Int): RockGardenCell {
        return cells[i][j]
    }

    fun move(i: Int, j: Int): RockGardenBoard {
        return if (isReachableRock(i, j)) {
            val arr = fromString(this.serialize().replace('b', 'f')).cells.map { it.toTypedArray() }.toTypedArray()
            arr[i][j] = Frog
            val updated = arr.joinToString(separator = "") { row -> row.joinToString(separator = "") { it.symbol.toString() } }
            return fromString(if (updated.count { it == 'r' } == 1) updated.replace('r', 'm') else updated)
        } else {
            this
        }
    }

    fun isReachableRock(i: Int, j: Int): Boolean {
        return when (cells[i][j]) {
            is Rock, TreasureMap -> {
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

    fun serialize(): String {
        return cells.joinToString(separator = "") { row -> row.joinToString(separator = "") { it.symbol.toString() } }
    }

    fun toInlineKeyboard(): List<List<ReplyMarkup.InlineKeyboardButton>> {
        return cells.mapIndexed { i, row ->
            row.mapIndexed { j, cell -> ReplyMarkup.InlineKeyboardButton(cell.emoji, callbackData = "$i$j") }
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
