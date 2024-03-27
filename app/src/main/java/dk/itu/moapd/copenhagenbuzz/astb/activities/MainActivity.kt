
/**
 * MIT License
 *
 * Copyright (c) [2024] [Astrid Emilie Bagge-Kjær, Julia Sjoukje Klompmaker]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dk.itu.moapd.copenhagenbuzz.astb.activities


import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.ActivityMainBinding



/**
 * Activity class to manage the main activity of CopenhagenBuzz
 */

class MainActivity : AppCompatActivity() {

    /**
     * View binding that creates a direct reference to make coding easier
     */
    private lateinit var binding: ActivityMainBinding
    private var isLoggedIn: Boolean = false

    /**
     * Main function to create the MainActivity at the start. Calls relevant functions to
     * provide functionality and initializes.
     *
     * @param savedInstanceState form of data that makes sure to contain the most recent data after
     * being previously run
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Calls method to create the correct icon determining if you are logged in or not
        onPrepareOptionsMenu(binding.contentMain.childAppBar.menu)

        /* Initializing navHosFragment which is responsible for displaying destinations
        via Navigation Graph
         */
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.fragment_container_view ) as NavHostFragment

        /* Initializing navController, which manages the current position and facilitates
        swapping between destinations as users use CopenhagenBuzz
         */
        val navController = navHostFragment.navController

        // Adds the bottomNavigation to easily navigate through the application
        binding.contentMain.bottomNavigation.setupWithNavController(navController)


        // Listener for user interaction with top app bar to either login or out
        binding.contentMain.childAppBar.setOnMenuItemClickListener{
            handleGoToLogin()
            true
        }

    }

    /**
     * Changes the icon depending on if you're logged in or are using CopenhagenBuzz as a guest
     * @param menu The top app bar
     *
     * @return return if the user is logged in or not
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