package com.example.locationtrackingapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.locationtrackingapp.R
import com.example.locationtrackingapp.databinding.LocationFragmentBinding
import com.example.locationtrackingapp.model.LocationData
import com.example.locationtrackingapp.utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class LocationFragment(val mContext:Context): Fragment() {
   private lateinit var binding:LocationFragmentBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var fragment:ImageFragment
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private val LOCATION_PERMISSION_REQUEST_CODE = 1002
    var currentPhotoPath = ""
    private var locationName = ""
    private var timeStamp = ""
    private  var latitude = ""
    private var longitude = ""
    private var isLocationTrackingEnabled = false
    private val handler = Handler()
    private var previousLocation: Location? = null
    private var idleStartTime: Long = 0
    private var isIdle = false
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LocationFragmentBinding.inflate(inflater,container,false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding.idleTime.visibility = View.GONE
        binding.trackLocation.setOnClickListener{
            if(!isLocationTrackingEnabled) {
                isLocationTrackingEnabled = true
                requestLocationPermissions()
                isIdle = false
                binding.idleTime.visibility = View.VISIBLE
                startIdleTimeUpdater()
                binding.trackLocation.text = "Stop Tracking"
            }else{
                isLocationTrackingEnabled = false
                binding.trackLocation.text = "Start Location Tracking"
                Utils.stopLocationTracking(mContext)
                idleStartTime = 0
                binding.idleTime.visibility = View.GONE
            }

        }
        binding.capturePhoto.setOnClickListener{
            requestCameraPermissions()
        }
        return binding.root
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && resultCode == -1) {
            getLastLocation()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    fun getLastLocation(){
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity(), object :
            OnCompleteListener<Location> {
            override fun onComplete(task: Task<Location>) {
                val geocoder = Geocoder(requireActivity(), Locale.getDefault())
                if (task.isSuccessful && task.result != null) {
                  val address =  geocoder.getFromLocation(task.result.getLatitude(), task.result.getLongitude(), 1);
                    latitude = address?.get(0)?.latitude.toString()
                    longitude = address?.get(0)?.longitude.toString()
                    timeStamp = task.result.time.toString()
                    locationName = address?.get(0)?.getAddressLine(0).toString()
                    val imageUri: Uri = Uri.parse(currentPhotoPath)
                    val location = LocationData(1,imageUri,locationName,latitude,longitude,timeStamp)
                    fragment = ImageFragment(location)
                    val transition = fragmentManager?.beginTransaction()
                    transition?.add(R.id.image_frame_layout,fragment,"ImageFragment")?.commit()
                }
            }
        })
    }
    fun updateIdleTimeTextView(idleDuration:Long){
        val idleTimeInSeconds = Utils.timeFormatter(idleDuration)
        binding.idleTime.text = "Idle Time: ${idleTimeInSeconds}s"
    }
    private fun startIdleTimeUpdater() {
        // Start a recurring task to update the idle time on the TextView every second
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkLocation()
                handler.postDelayed(this, 2000) // Check every 5 seconds
            }
        }, 0)
    }
    @SuppressLint("MissingPermission")
    private fun checkLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                if (previousLocation != null) {
                    val distance = location.distanceTo(previousLocation!!)
                    // If the user is within a small distance (e.g., 2 meters), consider them idle
                    if (distance < 2) { // 2 meters threshold
                        if (!isIdle) {
                            idleStartTime = System.currentTimeMillis()
                            isIdle = true
                        } else {
                            val idleDuration = System.currentTimeMillis() - idleStartTime
                            updateIdleTimeTextView(idleDuration)
                        }
                    } else {
                        // Reset idle time tracking if the user has moved
                        isIdle = false
                        idleStartTime = 0
                    }
                }
                previousLocation = location
            }
        }
    }
    fun requestCameraPermissions(){
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            // If permission is not granted, request it
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            captureImage()
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestLocationPermissions() {
        // Request both fine and coarse location permissions
        if(ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ||ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
          requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }else{
           Utils.startLocationTracking(mContext)
        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage()
            } else {
                Toast.makeText(requireActivity(), "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
            }
        }
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            Utils.startLocationTracking(mContext)
            binding.idleTime.visibility = View.VISIBLE
        }
    }
    fun captureImage(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.example.android.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, CAMERA_PERMISSION_REQUEST_CODE)
            }
        }
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
}