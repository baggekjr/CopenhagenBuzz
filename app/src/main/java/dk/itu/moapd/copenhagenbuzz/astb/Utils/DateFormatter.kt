package dk.itu.moapd.copenhagenbuzz.astb.Utils

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Utility class for formatting dates in a specific format.
 * This class provides a method to format a given timestamp into a string
 */

object DateFormatter {
    private val dateFormatter = SimpleDateFormat("EEE dd/MM/yyyy", Locale.ENGLISH)

    fun formatDate(date: Long): String {
        return dateFormatter.format(date)
    }
}
