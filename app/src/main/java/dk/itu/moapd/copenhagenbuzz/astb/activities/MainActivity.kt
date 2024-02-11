package dk.itu.moapd.copenhagenbuzz.astb.activities


import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.datepicker.MaterialDatePicker
import dk.itu.moapd.copenhagenbuzz.astb.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // A set of private constants used in this class .
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    // An instance of the ‘Event ‘ class.
    private val event: Event = Event("", "", "", "", "")


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(binding.root)


        // Listener for user interaction in the ‘Add Event‘ button.
        binding.contentMain.addEventButton.setOnClickListener {
            // Only execute the following code when the user fills all ‘EditText ‘.
            handleButtonOnClick()
        }

        //Listener for user interaction in the "Add event date" textfield
        binding.contentMain.editTextEventDate.setOnClickListener {
            handleDateOnClick()
        }

    }

    private fun handleDateOnClick() {
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select dates").build()
        dateRangePicker.show(supportFragmentManager, "DatePicker")

        dateRangePicker.addOnPositiveButtonClickListener { datePicked ->
            val startDate = datePicked.first
            val endDate = datePicked.second

            binding.contentMain.editTextEventDate.setText(
                convertLongToDate(startDate) + "  " + convertLongToDate(
                    endDate
                )
            )


        }
    }

    private fun convertLongToDate(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat(
            "dd-MM-yyyy"
        )

        return format.format(date)
    }

    private fun handleButtonOnClick() {
        if (binding.contentMain.editTextEventName.text.toString()
                .isNotEmpty() && binding.contentMain.editTextEventLocation.text.toString()
                .isNotEmpty() && binding.contentMain.editTextEventDate.text.toString()
                .isNotEmpty() && binding.contentMain.editTextEventType.text.toString()
                .isNotEmpty() && binding.contentMain.editTextEventDescription.text.toString()
                .isNotEmpty()
        ) {
            // Update the object attributes.
            Event(
                binding.contentMain.editTextEventName.text.toString().trim(),
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