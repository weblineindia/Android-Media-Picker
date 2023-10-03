package com.wli.customcameralibrary

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.customcameralibrary.databinding.ActivityMainBinding
import com.wli.mediapicker.photopickerbuilder.PhotoPicker

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private val selectedMediaAdapter by lazy {
        SelectedMediaAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            btnCamera.setOnClickListener(this@MainActivity)
            btnGallery.setOnClickListener(this@MainActivity)
            btnChoice.setOnClickListener(this@MainActivity)
            btnSelectVideo.setOnClickListener(this@MainActivity)
        }
        binding.imageRecyclerView.adapter = selectedMediaAdapter
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnCamera -> {
                openChooser(TYPE_CAMERA)
            }
            binding.btnGallery -> {
                openChooser(TYPE_GALLERY)
            }
            binding.btnChoice -> {
                openChooser(TYPE_CHOICE)
            }
            binding.btnSelectVideo -> {
                openChooser(TYPE_GALLERY_VIDEO)
            }
        }
    }

    private fun openChooser(type: Int) {
        val photoPickerType: PhotoPicker.Companion.PhotoPickerType
        val cameraType: PhotoPicker.Companion.CameraFacingType
        val mediaType: PhotoPicker.Companion.MediaType
        val isCrop: Boolean
        val isMultipleAllowed: Boolean

        when (type) {
            TYPE_CAMERA -> {
                photoPickerType = PhotoPicker.Companion.PhotoPickerType.CAMERA
                cameraType = PhotoPicker.Companion.CameraFacingType.FRONT
                mediaType = PhotoPicker.Companion.MediaType.IMAGE
                isCrop = false
                isMultipleAllowed = false
            }

            TYPE_GALLERY -> {
                photoPickerType = PhotoPicker.Companion.PhotoPickerType.GALLERY
                cameraType = PhotoPicker.Companion.CameraFacingType.BACK
                mediaType = PhotoPicker.Companion.MediaType.IMAGE
                isCrop = true
                isMultipleAllowed = true
            }

            TYPE_GALLERY_VIDEO -> {
                photoPickerType = PhotoPicker.Companion.PhotoPickerType.GALLERY
                cameraType = PhotoPicker.Companion.CameraFacingType.BACK
                mediaType = PhotoPicker.Companion.MediaType.VIDEO
                isCrop = false
                isMultipleAllowed = true
            }

            else -> {
                photoPickerType = PhotoPicker.Companion.PhotoPickerType.CHOOSER
                cameraType = PhotoPicker.Companion.CameraFacingType.BACK
                mediaType = PhotoPicker.Companion.MediaType.BOTH
                isCrop = true
                isMultipleAllowed = true
            }
        }

        PhotoPicker.PhotoPickerBuilder(this)
            .photoPickerType(photoPickerType)
            .cameraFacingType(cameraType)
            .crop(isCrop)
            .compress(1.0)
            .setMediaType(mediaType)
            .isMultipleSelection(isMultipleAllowed)
            .setCallBack(successCallback = { selectedUris ->
                //get all the selected images or video Uris here
                selectedMediaAdapter.addAll(selectedUris.toCollection(arrayListOf()))
            }, failCallback = { message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }).build()
    }

    companion object {
        const val TYPE_CAMERA = 1
        const val TYPE_GALLERY = 2
        const val TYPE_CHOICE = 3
        const val TYPE_GALLERY_VIDEO = 4
    }
}
