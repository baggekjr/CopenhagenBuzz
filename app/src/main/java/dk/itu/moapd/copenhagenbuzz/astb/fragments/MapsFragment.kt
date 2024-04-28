package dk.itu.moapd.copenhagenbuzz.astb.fragments;

import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import dk.itu.moapd.copenhagenbuzz.astb.LocationService
import dk.itu.moapd.copenhagenbuzz.astb.databinding.FragmentMapsBinding
import dk.itu.moapd.copenhagenbuzz.astb.R
import android.Manifest.permission
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.MarkerOptions
import dk.itu.moapd.copenhagenbuzz.astb.SharedPreferenceUtil



class MapsFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener, OnMapReadyCallback {


    private inner class LocationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(LocationService.EXTRA_LOCATION)
            location?.let {
                moveCameraToLocation(it)
            }
        }
    }

    private var _binding: FragmentMapsBinding? = null
    private var isFirstCameraMove = true
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
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 1001
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity()
            .getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        locationBroadcastReceiver = LocationBroadcastReceiver()

        val mapFragment = childFragmentManager
            .findFragmentById(binding.map.id) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())


    }

    override fun onResume() {
        super.onResume()

        // Register the broadcast receiver.
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationBroadcastReceiver,
            IntentFilter(LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )

        // Check if the service is a foreground service, and if not, subscribe to location updates
        if (locationServiceBound && !SharedPreferenceUtil.getLocationTrackingPref(requireContext())) {
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
            if (!SharedPreferenceUtil.getLocationTrackingPref(requireContext())) {
                locationService.unsubscribeToLocationUpdates()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        Intent(requireContext(), LocationService::class.java).let { serviceIntent ->
            requireActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationBroadcastReceiver,
            IntentFilter(LocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
        sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

    }

    override fun onStop() {
        if (locationServiceBound) {
            requireActivity().unbindService(serviceConnection)
            locationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)

        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("MapsFragment", "onMapReady")
        this.googleMap = googleMap
        googleMap.setPadding(0, 100, 0, 0) // Move the Google Maps UI buttons under the OS top bar.
        if (checkPermission()) {
            googleMap.isMyLocationEnabled = true
            getCurrentLocation()

        } else {
            requestUserPermissions()
        }


    }


    private fun getCurrentLocation() {
        if (checkPermission()) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    moveCameraToLocation(it)
                }
            }
        } else {
            requestUserPermissions()
        }
    }
    private fun moveCameraToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        // Clear existing markers
        googleMap?.clear()
        // Add a marker at the new location
        googleMap?.addMarker(MarkerOptions().position(latLng))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        /*googleMap?.addMarker(MarkerOptions().position(latLng))
        // Move the camera to the new location with animation

        if (isFirstCameraMove) {
            // Move the camera to the new location with animation
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            isFirstCameraMove=false
        }*/

    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        // No need for any action here since there is no start/stop button
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

