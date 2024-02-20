package dk.itu.moapd.copenhagenbuzz.astb.activities


import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import java.util.Date


/**
 * License??
 */

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isLoggedIn: Boolean = false

    val navHostFragment = supportFragmentManager .findFragmentById(
        R.id.fragment_container_view ) as NavHostFragment
    val navController = navHostFragment.navController


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        onPrepareOptionsMenu(binding.contentMain.childAppBar.menu)


        // Listener for user interaction with top app bar to either login or out
        binding.contentMain.childAppBar.setOnMenuItemClickListener{
            handleGoToLogin()
            true
        }

    }

    /**
     * Changes the icon depending on if you're logged in or are using CopenhagenBuzz as a guest
     * @param menu The top app bar
     */

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        isLoggedIn = intent.getBooleanExtra("IsLoggedIn", false)
        if(isLoggedIn) {
            binding.contentMain.childAppBar.menu.findItem(R.id.nav_to_login).setIcon(R.drawable.baseline_hail_24)
        } else {
            binding.contentMain.childAppBar.menu.findItem(R.id.nav_to_login).setIcon(R.drawable.baseline_account_circle_24)
        }
        return true

    }

    /**
     * Handles the top menu button to go to the login-page
     */
    private fun handleGoToLogin() {
        val intent = Intent(this, LoginActivity::class.java).putExtra("IsLoggedIn",isLoggedIn)
        startActivity(intent)
        finish()
    }



}