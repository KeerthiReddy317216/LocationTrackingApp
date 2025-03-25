package com.example.locationtrackingapp.view
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.locationtrackingapp.databinding.ImageFragmentBinding
import com.example.locationtrackingapp.model.LocationData
import com.example.locationtrackingapp.room.RoomDatabaseInstance
import kotlinx.coroutines.launch


class ImageFragment(val location: LocationData):Fragment() {
    lateinit var  binding:ImageFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ImageFragmentBinding.inflate(inflater)
        binding.imageView.setImageURI(location.imageUrl)
        binding.locationName.text = location.locationName
        binding.longitude.text = "Longitude:"+location.longitude
        binding.latitude.text = "Latitude:"+location.latitude
        binding.timeStamp.text = location.timeStamp
        val dao = RoomDatabaseInstance.provideRoomDatabase(requireActivity()).locationDao()
        lifecycleScope.launch {
            dao.insertDetails(location)
        }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}