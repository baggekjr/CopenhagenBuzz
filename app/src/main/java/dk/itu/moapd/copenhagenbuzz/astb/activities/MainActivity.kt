package dk.itu.moapd.copenhagenbuzz.astb.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dk.itu.moapd.copenhagenbuzz.astb.databinding.ActivityMainBinding
import android.util.Log
import android.widget.EditText
import androidx.core.view.WindowCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.R

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // A set of private constants used in this class .
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    // An instance of the ‘Event ‘ class.
    private val event: Event = Event("", "","","","")


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Listener for user interaction in the ‘Add Event‘ button.
        binding.contentMain.addEventButton.setOnClickListener {
            // Only execute the following code when the user fills all ‘EditText ‘.
            handleButtonOnClick()
        }
    }

    private fun handleButtonOnClick() {
        if (binding.contentMain.editTextEventName.text.toString().isNotEmpty() &&
            binding.contentMain.editTextEventLocation.text.toString().isNotEmpty() &&
            binding.contentMain.editTextEventDate.text.toString().isNotEmpty() &&
            binding.contentMain.editTextEventType.text.toString().isNotEmpty() &&
            binding.contentMain.editTextEventDescription.text.toString().isNotEmpty()
        ) {
            // Update the object attributes.
            Event(binding.contentMain.editTextEventName.text.toString().trim(),
                binding.contentMain.editTextEventLocation.text.toString().trim(),
                binding.contentMain.editTextEventDate.text.toString().trim(),
                binding.contentMain.editTextEventType.text.toString().trim(),
                binding.contentMain.editTextEventDescription.text.toString().trim()
            )

            // Write in the 'Logcat' system
            showMessage()
        }
    }


    private fun showMessage() {
        Log.d(TAG, event.toString())
    }

}