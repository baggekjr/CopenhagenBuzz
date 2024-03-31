package dk.itu.moapd.copenhagenbuzz.astb.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.ActivityLoginBinding
import dk.itu.moapd.copenhagenbuzz.astb.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        onSignInResult(result)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                // Successfully signed in.
                showSnackBar("User logged in the app.")
                startMainActivity()
            }
            else -> {
                // Sign in failed.
                showSnackBar("Authentication failed.")
            }
        }

    }

    private fun startMainActivity() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }
    private fun showSnackBar(message: String) {
        Snackbar.make(
            window.decorView.rootView, message, Snackbar.LENGTH_SHORT
        ).show()
    }

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener{
            //navigateToMain(isLoggedIn = true)
            signInLauncher
        }
        binding.guestButton.setOnClickListener{

        }
        createSignInIntent()
    }

    private fun createSignInIntent() {

        // Choose authentication providers.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent.
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setLogo(R.drawable.baseline_add_location_alt_24)
            .apply {
                setTosAndPrivacyPolicyUrls(
                    "https://firebase.google.com/terms/",
                    "https://firebase.google.com/policies/analytics"
                )
            }
            .build()
        signInLauncher.launch(signInIntent)
    }

    /*private fun navigateToMain(isLoggedIn : Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("IsLoggedIn", isLoggedIn)
        }
        startActivity(intent)
        finish()
    }

     */
}