package dk.itu.moapd.copenhagenbuzz.astb.Utils

import java.text.SimpleDateFormat
import java.util.Locale

object DateFormatter {
    private val dateFormatter = SimpleDateFormat("EEE dd/MM/yyyy", Locale.ENGLISH)

    fun formatDate(date: Long): String {
        return dateFormatter.format(date)
    }
}
