
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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.ActivityMainBinding


/**
 * Activity class to manage the main activity of CopenhagenBuzz
 */

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    /**
     * View binding that creates a direct reference to make coding easier
     */
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    //private var isLoggedIn: Boolean = false

    override fun onStart() {
        super.onStart()
        // Redirect the user to the LoginActivity
        // if they are not logged in.
        //auth.currentUser ?: startLoginActivity()

        // Check if user is authenticated
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is authenticated, make "Add Event" menu item visible
            binding.contentMain.bottomNavigation.menu.findItem(R.id.fragment_event)?.isVisible = true
        } else {
            // User is not authenticated, hide "Add Event" menu item
            binding.contentMain.bottomNavigation.menu.findItem(R.id.fragment_event)?.isVisible = false
        }

    }

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
        //onPrepareOptionsMenu(binding.contentMain.childAppBar.menu)

        /* Initializing navHosFragment which is responsible for displaying destinations
        via Navigation Graph
         */
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.fragment_container_view ) as NavHostFragment

        /* Initializing navController, which manages the current position and facilitates
        swapping between destinations as users use CopenhagenBuzz
         */
        val navController = navHostFragment.navController


        setSupportActionBar(binding.contentMain.childAppBar)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Adds the bottomNavigation to easily navigate through the application
        binding.contentMain.bottomNavigation.setupWithNavController(navController)


        auth = FirebaseAuth.getInstance()



       /* // Listener for user interaction with top app bar to either login or out
        binding.contentMain.childAppBar.setOnMenuItemClickListener{
            onStart()
            true
        }*/

    }



    private fun startLoginActivity() {
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let(::startActivity)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d("MainActivity", "onCreateOptionsMenu called") // Add this log statement
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //isLoggedIn = intent.getBooleanExtra("IsLoggedIn", false)
            when (item.itemId) {
                R.id.action_logout -> {
                    Log.d("MainActivity", "Logout menu item clicked") // Add this log statement
                    auth.signOut()
                    startLoginActivity()
                    // Return false to allow normal processing
                }
            }
            return true

    }






     /* Changes the icon depending on if you're logged in or are using CopenhagenBuzz as a guest
     * @param menu The top app bar
     *
     * @return return if the user is logged in or not
    */

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is authenticated, make "Add Event" menu item visible
            binding.contentMain.childAppBar.menu.findItem(R.id.action_logout).setIcon(R.drawable.baseline_hail_24)

        } else {
            // User is not authenticated, hide "Add Event" menu item
            binding.contentMain.childAppBar.menu.findItem(R.id.action_logout).setIcon(R.drawable.baseline_account_circle_24)

        }
        return true

    }





     //* Handles the top menu button to go to the login-page

    /*private fun handleGoToLogin() {
        val intent = Intent(this, LoginActivity::class.java).putExtra("IsLoggedIn",isLoggedIn)
        startActivity(intent)
        finish()
    }

     */




}