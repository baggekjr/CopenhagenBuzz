package dk.itu.moapd.copenhagenbuzz.astb.activities


import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dk.itu.moapd.copenhagenbuzz.astb.databinding.ActivityMainBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import java.util.Date


/**
 * License??
 */

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

        setContentView(binding.root)


        // Listener for user interaction in the ‘Add Event‘ button.
        binding.contentMain.addEventButton.setOnClickListener {
            // Only execute the following code when the user fills all ‘EditText ‘.
            handleEventButtonOnClick()
        }

        // Listener for user interaction in the "Add event date" textfield
        binding.contentMain.editTextEventDate.setOnClickListener {
            handleDateOnClick()
        }

    }

    /**
     * Handles the date picker to pop up and the dates selected to get written out
     *
     * @param dateRangePicker The pop up calendar.
     * @param startDate The start date the user chose.
     * @param endDate The end date the user chose.
     *
     */

    private fun handleDateOnClick() {
        // TODO: Fix it so you dont have to press twice for it to open.
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker().setTitleText("Select dates").build()
        dateRangePicker.show(supportFragmentManager, "DatePicker")

        dateRangePicker.addOnPositiveButtonClickListener { datePicked ->
            val startDate = datePicked.first
            val endDate = datePicked.second

            binding.contentMain.editTextEventDate.setText(
                convertLongToDate(startDate) + " - " + convertLongToDate(endDate)
            )
        }
    }

    /**
     * Inspired from: Date Range Picker Android Material Design (Kotlin)????
     * Converts numbers in long format to dates
     *
     * @param date The date
     * @param format The formatted date
     *
     * @return The date in the correct format
     */
    private fun convertLongToDate(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat(
            "dd-MM-yyyy"
        )
        return format.format(date)
    }

    /**
     * Handle when the eventButton gets clicked.
     *
     * checks whether the textfields are empty or not. If they are all not empty it updates the
     * Event()
     *
     */
    private fun handleEventButtonOnClick() {
        if (binding.contentMain.editTextEventName.text.toString()
                .isNotEmpty() && binding.contentMain.editTextEventLocation.text.toString()
                .isNotEmpty() && binding.contentMain.editTextEventDate.text.toString()
                .isNotEmpty() && binding.contentMain.editTextEventType.text.toString()
                .isNotEmpty() && binding.contentMain.editTextEventDescription.text.toString()
                .isNotEmpty()
        ) {
            // Update the object attributes.
            val eventName = binding.contentMain.editTextEventName.text.toString().trim()
            val eventLocation = binding.contentMain.editTextEventLocation.text.toString().trim()
            val eventDate = binding.contentMain.editTextEventDate.text.toString().trim()
            val eventType = binding.contentMain.editTextEventType.text.toString().trim()
            val eventDescription = binding.contentMain.editTextEventDescription.text.toString().trim()

            Event(eventName, eventLocation, eventDate, eventType, eventDescription)

            // Show snackbar with the event
            Snackbar.make(binding.root, eventName+" " +eventLocation+" "+eventDate+" "+eventType+" "+eventDescription, Snackbar.LENGTH_SHORT).show()


            // Write in the 'Logcat' system
            showMessage()
        }
    }


    private fun showMessage() {
        Log.d(TAG, event.toString())
    }

}