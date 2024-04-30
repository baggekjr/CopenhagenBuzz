package dk.itu.moapd.copenhagenbuzz.astb


import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * A service class with several methods to manage the location service of Geolocation application.
 */
class LocationService : Service() {

    /**
     * Class used for the client Binder. Since this service runs in the same process as its clients,
     * we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
    }

    /**
     * The binder instance for this service.
     */
    private val localBinder = LocalBinder()

    /**
     * A set of private constants used in this class.
     */
    companion object {
        private const val PACKAGE_NAME = "dk.itu.moapd.geolocation"
        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"
        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
    }

    /**
     * The primary instance for receiving location updates.
     */
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    /**
     * This callback is called when `FusedLocationProviderClient` has a new `Location`.
     */
    private lateinit var locationCallback: LocationCallback

    /**
     * Called by the system when the service is first created. Do not call this method directly.
     */
    override fun onCreate() {
        super.onCreate()

        // Start receiving location updates.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the `LocationCallback`.
        locationCallback = object : LocationCallback() {

            /**
             * This method will be executed when `FusedLocationProviderClient` has a new location.
             *
             * @param locationResult The last known location.
             */
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // Get the current user's location.
                val currentLocation = locationResult.lastLocation

                // Notify our Activity that a new location was added.
                val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }
    }

    /**
     * Return the communication channel to the service. May return `null` if clients can not bind to
     * the service. The returned `IBinder` is usually for a complex interface that has been
     * described using aidl.
     *
     * Note that unlike other application components, calls on to the `IBinder` interface returned
     * here may not happen on the main thread of the process. More information about the main thread
     * can be found in the official Android documentation (`Processes and Threads`).
     *
     * @param intent The `Intent` that was used to bind to this service, as given to
     *      `bindService()`. Note that any extras that were included with the `Intent` at that point
     *      will not be seen here.
     *
     * @return Return an `IBinder` through which clients can call on to the service.
     */
    override fun onBind(intent: Intent): IBinder {
        // Return the communication channel to the service.
        return localBinder
    }

    /**
     * Subscribes this application to get the location changes via the `locationCallback()`.
     */
    fun subscribeToLocationUpdates() {

        // Save the location tracking preference.
        SharedPreferenceUtil.saveLocationTrackingPref(this, true)

        // Sets the accuracy and desired interval for active location updates.
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_HIGH_ACCURACY, 60)
            .setMinUpdateIntervalMillis(30)
            .setMaxUpdateDelayMillis(2)
            .build()

        // Subscribe to location changes.
        try {
            fusedLocationProviderClient
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        }
    }

    /**
     * Unsubscribes this application of getting the location changes from  the `locationCallback()`.
     */
    fun unsubscribeToLocationUpdates() {
        // Unsubscribe to location changes.
        try {
            fusedLocationProviderClient
                .removeLocationUpdates(locationCallback)
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
        }
    }

}