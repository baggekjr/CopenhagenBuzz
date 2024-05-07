package dk.itu.moapd.copenhagenbuzz.astb.fragments;

import android.Manifest.permission
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import dk.itu.moapd.copenhagenbuzz.astb.DATABASE_URL
import dk.itu.moapd.copenhagenbuzz.astb.R
import dk.itu.moapd.copenhagenbuzz.astb.Utils.LocationService
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentMapsBinding
import dk.itu.moapd.copenhagenbuzz.astb.models.Event
import dk.itu.moapd.copenhagenbuzz.astb.viewmodels.DataViewModel

/**
 * A Fragment subclass responsible for displaying a map with event markers and the user's location.
 * It also handles location updates and displays event details when a marker is clicked.
 */
class MapsFragment : Fragment(){

    private var isFirstMove = true

    private val BUZZ = "CopenhagenBuzz"
    private val EVENT = "events"
    private inner class LocationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                intent.getParcelableExtra(LocationService.EXTRA_LOCATION, Location::class.java)
            else
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(LocationService.EXTRA_LOCATION)
            location?.let {
                getCurrentLocation()
            }
        }
    }

    private var _binding: FragmentMapsBinding? = null
    private lateinit var dataViewModel: DataViewModel


    private val binding get() = requireNotNull(_binding) { "Binding is null" }
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var locationBroadcastReceiver: LocationBroadcastReceiver
    private lateinit var locationService: LocationService
    private var locationServiceBound = false
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.service
            locationServiceBound = true
            locationService.subscribeToLocationUpdates()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            locationServiceBound = false
        }
    }

    companion object {
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentMapsBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity()
            .getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        locationBroadcastReceiver = LocationBroadcastReceiver()

        val mapFragment = childFragmentManager
            .findFragmentById(binding.map.id) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    // Initialize the map asynchronously
    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
        googleMap.setPadding(0, 100, 0, 0)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val database = Firebase.database(DATABASE_URL).getReference(BUZZ)

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (eventSnapShot in snapshot.children){
                    val event = eventSnapShot.getValue(Event::class.java)
                    val pos = event?.eventLocation.let {
                        LatLng(event?.eventLocation!!.latitude!!, event.eventLocation.longitude!!)
                    }
                    pos.let {
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(pos)
                                .title(event?.eventName)
                        )?.tag = event
                    }}}

            override fun onCancelled(error: DatabaseError) {
               Log.e(TAG, "Database error: ${error.message}")
            }
        }
        database.child(EVENT).addValueEventListener(eventListener)
        googleMap.setOnMarkerClickListener {
            val event = it.tag as Event

            EventDetailsDialogFragment(event).apply {
                isCancelable = false
            }.also { dialogFragment ->
                dialogFragment.show(requireActivity().supportFragmentManager, "EventInfoDialogFragment")
            }

            false
        }

        if (checkPermission()) {
            googleMap.isMyLocationEnabled = true
        } else {
            requestUserPermissions()
        }
    }



    override fun onResume() {
        super.onResume()

        // Register the broadcast receiver.
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationBroadcastReceiver,
            IntentFilter(LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )

        // Check if the service is a foreground service, and if not, subscribe to location updates
        if (locationServiceBound) {
            if (checkPermission()) {
                locationService?.subscribeToLocationUpdates()
            } else {
                requestUserPermissions()
            }
        }
    }


    override fun onPause() {
        // Unregister the broadcast receiver.
        super.onPause()


        if (::locationService.isInitialized) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)

            // Unsubscribe from location updates if the service is no longer a foreground service
            locationService.unsubscribeToLocationUpdates()
        }
    }


    override fun onStart() {
        super.onStart()
        Intent(requireContext(), LocationService::class.java).let { serviceIntent ->
            requireActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationBroadcastReceiver,
            IntentFilter(LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    override fun onStop() {
        if (locationServiceBound) {
            requireActivity().unbindService(serviceConnection)
            locationServiceBound = false
        }
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)

        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCurrentLocation() {
        if (checkPermission()) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    if (isFirstMove){
                    moveCameraToLocation(it)
                        isFirstMove=false}

                } ?: run {
                    Log.e(TAG, "Last known location is null")
                    // Handle the scenario where the location is not available
                    // For example, display a message to the user or prompt them to enable location services
                }
            }
        } else {
            requestUserPermissions()
        }
    }
    private fun moveCameraToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

    }


    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestUserPermissions() {
        if (!checkPermission())
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
    }
}