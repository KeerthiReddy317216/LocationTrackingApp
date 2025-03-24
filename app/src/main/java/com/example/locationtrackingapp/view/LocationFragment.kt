package com.example.locationtrackingapp.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.locationtrackingapp.R
import com.example.locationtrackingapp.databinding.LocationFragmentBinding
import com.example.locationtrackingapp.model.LocationData
import com.example.locationtrackingapp.service.LocationTrackingService
import com.example.locationtrackingapp.service.LocationTrackingWorker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class LocationFragment(val mContext:Context): Fragment() {
   private lateinit var binding:LocationFragmentBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var fragment:ImageFragment
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private val PERMISSION_REQUEST_CODE = 1
    var currentPhotoPath = ""
    private var locationName = ""
    private var timeStamp = ""
    private  var latitude = ""
    private var longitude = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LocationFragmentBinding.inflate(inflater,container,false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding.trackLocation.setOnClickListener{
            binding.idleTime.text = getIdleTime()
            startLocationTracking()
        }
        binding.capturePhoto.setOnClickListener{
            requestCameraPermissions()
        }
        return binding.root
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE ) {
            getLastLocation()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun getIdleTime():String{
        return "00:00:02"
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
                    val location = LocationData(imageUri,locationName,latitude,longitude,timeStamp)
                    fragment = ImageFragment(location)
                    val transition = fragmentManager?.beginTransaction()
                    transition?.add(R.id.image_frame_layout,fragment,"ImageFragment")?.commit()
                }
            }
        })
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
    fun startLocationTracking(){
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
            return
        }
        val locationTrackingRequest = OneTimeWorkRequestBuilder<LocationTrackingWorker>()
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(requireActivity()).enqueue(locationTrackingRequest)

    }
}