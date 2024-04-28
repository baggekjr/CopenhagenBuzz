package dk.itu.moapd.copenhagenbuzz.astb

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.widget.EditText
import java.util.Locale

class GeocodingHelper(private val context: Context) {

    fun getLocationFromAddress(address: String, callback: (Double?, Double?) -> Unit) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(address, 1)
        if (addresses!!.isNotEmpty()) {
            val latitude = addresses[0].latitude
            val longitude = addresses[0].longitude
            callback(latitude, longitude)
        } else {
            callback(null, null)
        }
    }
    fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            geocoder.getFromLocation(latitude, longitude, 1)
        } else {
            geocoder.getFromLocation(latitude, longitude, 1)
        }
        return addresses?.firstOrNull()?.toAddressString() ?: ""
    }

    private fun Address.toAddressString(): String =
        with(StringBuilder()) {
            append(getAddressLine(0)).append("\n")
            append(countryName)
            toString()
        }

    fun setAddress(latitude: Double, longitude: Double, eventLocationTextField: EditText?) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            geocoder.getFromLocation(latitude, longitude, 1)
        } else {
            geocoder.getFromLocation(latitude, longitude, 1)
        }

            addresses?.firstOrNull()?.toAddressString()?.let { address ->
                eventLocationTextField?.setText(address)
            }

    }
}
