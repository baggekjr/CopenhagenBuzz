package dk.itu.moapd.copenhagenbuzz.astb.Utils


import android.content.Context
import androidx.core.content.edit
import dk.itu.moapd.copenhagenbuzz.astb.R


/**
 * Provides access to SharedPreferences for location to Fragments and Services.
 */
internal object SharedPreferenceUtil {

    /**
     * The name of the SharedPreferences variable.
     */
    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"


    fun getLocationTrackingPref(context: Context): Boolean {
        return context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        ).getBoolean(KEY_FOREGROUND_ENABLED, false)
    }

    /**
     * Stores the location updates state in SharedPreferences.
     *
     * @param requestingLocationUpdates The location updates state.
     */
    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        }

}