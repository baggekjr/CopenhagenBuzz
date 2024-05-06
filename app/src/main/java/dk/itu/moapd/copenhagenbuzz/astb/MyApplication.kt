package dk.itu.moapd.copenhagenbuzz.astb

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.astb.Utils.SharedPreferenceUtil
import io.github.cdimascio.dotenv.dotenv


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