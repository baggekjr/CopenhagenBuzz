package dk.itu.moapd.copenhagenbuzz.astb

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.astb.Utils.SharedPreferenceUtil
import io.github.cdimascio.dotenv.dotenv


/**
 * Application class for the CopenhagenBuzz app. This class initializes necessary components
 * and configurations when the application starts.
 * It sets up DynamicColors for activities, saves default location tracking preference using SharedPreferenceUtil,
 * and configures Firebase Realtime Database with disk persistence and synchronization.
 *
 * @property DATABASE_URL The URL to the Firebase Realtime Database retrieved from the environment configuration file.
 * @property BUCKET_URL The URL to the Firebase Storage bucket retrieved from the environment configuration file.
 */

inline fun <reified T> T.TAG(): String = T::class.java.simpleName


val DATABASE_URL: String = dotenv {
    directory = "/assets"
    filename = "env"
}["DATABASE_URL"] ?: throw IllegalArgumentException("DATABASE_URL not found in env file")


val BUCKET_URL: String = dotenv {
        directory = "/assets"
        filename = "env"
    }["BUCKET_URL"] ?: throw IllegalArgumentException("BUCKET_URL not found in env file")


class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        SharedPreferenceUtil.saveLocationTrackingPref(this, false)

        // Enable disk persistence for the Firebase Realtime Database and keep it synchronized.
        Firebase.database(DATABASE_URL).setPersistenceEnabled(true)
        Firebase.database(DATABASE_URL).reference.keepSynced(true)
    }
}