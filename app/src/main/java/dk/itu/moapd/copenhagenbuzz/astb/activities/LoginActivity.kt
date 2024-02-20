package dk.itu.moapd.copenhagenbuzz.astb.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.databinding.ActivityLoginBinding
import dk.itu.moapd.copenhagenbuzz.astb.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener{
            navigateToMain(isLoggedIn = true)
        }
        binding.guestButton.setOnClickListener{
            navigateToMain(isLoggedIn = false)
        }
    }

    private fun navigateToMain(isLoggedIn : Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("IsLoggedIn", isLoggedIn)
        }
        startActivity(intent)
        finish()
    }
}