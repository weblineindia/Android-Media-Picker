package com.wli.mediapicker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wli.mediapicker.crop.main.CropActivity
import com.wli.mediapicker.crop.main.CropRequest
import com.wli.mediapicker.photopickerbuilder.PhotoPicker
import com.wli.mediapicker.photopickerbuilder.utils.Constant.ALLOW_MULTI_SELECTION
import com.wli.mediapicker.photopickerbuilder.utils.Constant.CAMERA_FACING_TYPE
import com.wli.mediapicker.photopickerbuilder.utils.Constant.ENABLE_COMPRESS_IMAGE
import com.wli.mediapicker.photopickerbuilder.utils.Constant.ENABLE_CROP
import com.wli.mediapicker.photopickerbuilder.utils.Constant.MEDIA_TYPE
import com.wli.mediapicker.photopickerbuilder.utils.Constant.PICKER_TYPE
import com.wli.mediapicker.photopickerbuilder.utils.Constant.SIZE_IN_MB
import com.wli.mediapicker.photopickerbuilder.utils.bitmapToUri
import com.wli.mediapicker.photopickerbuilder.utils.checkCameraPermission
import com.wli.mediapicker.photopickerbuilder.utils.checkReadStoragePermission
import com.wli.mediapicker.photopickerbuilder.utils.checkStoragePermissionForBoth
import com.wli.mediapicker.photopickerbuilder.utils.checkVideoPermission
import com.wli.mediapicker.photopickerbuilder.utils.compressImage
import com.wli.mediapicker.photopickerbuilder.utils.copyExif
import com.wli.mediapicker.photopickerbuilder.utils.getFileSizeFromUri
import com.wli.mediapicker.photopickerbuilder.utils.getImagePathFromUri
import com.wli.mediapicker.photopickerbuilder.utils.isVideoUri
import com.wli.mediapicker.photopickerbuilder.utils.readVideoStoragePermission
import com.wli.mediapicker.photopickerbuilder.utils.requestCameraPermission
import com.wli.mediapicker.photopickerbuilder.utils.requestForGalleryAndVideo
import com.wli.mediapicker.photopickerbuilder.utils.requestReadStoragePermission
import com.wli.mediapicker.photopickerbuilder.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MediaPickerActivity : AppCompatActivity() {
    private var isCropEnable: Boolean = false
    private var compressImage: Boolean = false
    private var isMultipleSelection: Boolean = false
    private var sizeInMB: Double = 0.0
    private var cameraFacingType: String? = ""
    private var detectChooserClickType: DetectChooserClickType? = null
    private var isBottomSheetButtonClicked = false
    private var cameraImageUri: Uri? = null
    private var cameraLauncher: ActivityResultLauncher<Intent>? = null
    private var galleryLauncher: ActivityResultLauncher<Intent>? = null
    private var cropLauncher: ActivityResultLauncher<Intent>? = null
    private var photoPickerType: String? = null
    private var mediaType: String? = null
    private var selectedImageOrVideoUri = ArrayList<Uri>()
    private var galleryCroppedImages = ArrayList<Uri>()
    private val cameraImageUris: ArrayList<Uri> = arrayListOf()
    private val compressedImagesList = ArrayList<Uri>()
    private val filterImagesList = ArrayList<Uri>()
    private val filterVideosList = ArrayList<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)
        val bundle = intent.extras

        if (bundle != null) {
            photoPickerType = bundle.getString(PICKER_TYPE)
            cameraFacingType = bundle.getString(CAMERA_FACING_TYPE)
            isCropEnable = bundle.getBoolean(ENABLE_CROP)
            compressImage = bundle.getBoolean(ENABLE_COMPRESS_IMAGE)
            isMultipleSelection = bundle.getBoolean(ALLOW_MULTI_SELECTION)
            sizeInMB = bundle.getDouble(SIZE_IN_MB)
            mediaType = bundle.getString(MEDIA_TYPE)

            setupActivityResultLaunchers()
            checkPhotoPickerType(photoPickerType)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            //Only opens camera for capture image
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkCameraFacingType(cameraFacingType)
                } else {
                    requestCameraPermission()
                }
            }

            STORAGE_READ_PERMISSION_REQUEST_CODE -> {
                if (mediaType == PhotoPicker.Companion.MediaType.VIDEO.name) {
                    //Open gallery for Video pick
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        launchGalleryForVideo()
                    } else {
                        readVideoStoragePermission()
                    }
                } else {
                    //Open gallery for Image pick
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        launchGalleryForImage()
                    } else {
                        requestReadStoragePermission()
                    }
                }
            }

            STORAGE_MULTI_PERMISSION_REQUEST_CODE -> {
                val allGranted = grantResults.toList().stream().distinct().count() <= 1
                if (grantResults.isNotEmpty() && allGranted) {
                    launchGalleryForBothType()
                } else {
                    requestForGalleryAndVideo()
                }
            }
        }
    }

    /**
     * function for handling result from camera/gallery and crop activities
     */
    private fun setupActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    cameraImageUri?.let { cameraImageUris.add(it) }
                    handleCrop(cameraImageUris)
                } else {
                    onFailAndGoBack()
                }
            }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handleGalleryResult(result)
                } else {
                    onFailAndGoBack()
                }
            }

        cropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    GALLERY_CROP_RESULT -> {
                        /**
                         * handled gallery image result
                         */
                        handleGalleryCropResult(result)
                    }

                    CAMERA_CROP_RESULT -> {
                        /**
                         * handled camera image result
                         */
                        val cameraImageUriFromCropResult = result.data?.data
                        val croppedCameraImageUris: ArrayList<Uri> = arrayListOf()
                        if (cameraImageUriFromCropResult != null) {
                            croppedCameraImageUris.add(cameraImageUriFromCropResult)
                        }
                        doCompression(croppedCameraImageUris)
                    }

                    else -> {
                        onFailAndGoBack()
                    }
                }
            }
    }

    /**
     * function for handled gallery result from selected images from gallery
     */
    private fun handleGalleryResult(result: ActivityResult) {
        if (result.data?.clipData != null) {
            val clipData = result.data?.clipData
            val count = clipData?.itemCount ?: 0
            for (i in 0 until count) {
                clipData?.getItemAt(i)?.uri?.let { selectedImageOrVideoUri.add(it) }
            }
        } else {
            val selectedImageUri: Uri? = result.data?.data
            selectedImageUri?.let { selectedImageOrVideoUri.add(it) }
        }

        //providing functionality of Video And Image(both) selection for CHOOSER type only
        if (mediaType == PhotoPicker.Companion.MediaType.VIDEO.name) {
            onSuccessAndGoBack(selectedImageOrVideoUri)
        } else {
            //filtering video and images uris here
            selectedImageOrVideoUri.onEach {
                if (isVideoUri(it)) {
                    filterVideosList.add(it)
                } else {
                    filterImagesList.add(it)
                }
            }
            if (filterImagesList.isNotEmpty()) {
                handleCrop(filterImagesList)
            } else {
                onSuccessAndGoBack(filterVideosList)
            }
        }
    }

    /**
     * function for handled crop result from selected images from gallery
     */
    private fun handleGalleryCropResult(result: ActivityResult) {
        if (result.data?.clipData != null) {
            val clipData = result.data?.clipData
            val count = clipData?.itemCount ?: 0
            for (i in 0 until count) {
                val clipDataImageUri = clipData?.getItemAt(i)?.uri
                clipDataImageUri?.let { galleryCroppedImages.add(it) }
            }
        } else if (result.data?.data != null) {
            val imageUri = result.data?.data
            imageUri?.let { galleryCroppedImages.add(it) }
        }
        filterImagesList.removeFirst()
        if (filterImagesList.isEmpty()) {
            doCompression(galleryCroppedImages)
        } else {
            /**
             * handled case for selectedGalleryImages goes one by one to crop activity
             */
            handleCrop(filterImagesList)
        }
    }

    /**
     * commonFunction for compressingImage
     */
    private fun compressingImageCommon(originalUris: ArrayList<Uri>) {
        val originalImagePath = ArrayList<String?>()
        val newImagePath = ArrayList<String?>()
        var onSuccessCallBack: (() -> Unit)? = null
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                originalUris.onEach { uri ->
                    originalImagePath.add(this@MediaPickerActivity.getImagePathFromUri(uri))
                    val fileSizeInBytes = this@MediaPickerActivity.getFileSizeFromUri(uri)
                    val fileSizeInKB = fileSizeInBytes / 1024

                    compressImage(uri, fileSizeInKB, sizeInMB) { bitmap ->
                        val compressUri = bitmapToUri(bitmap)
                        compressedImagesList.add(compressUri)
                        newImagePath.add(getImagePathFromUri(compressUri))
                    }
                }
                /**
                 * copied meta data after compress.
                 */
                newImagePath.onEachIndexed { index, newPath ->
                    copyExif(originalImagePath[index], newPath)
                }
                onSuccessCallBack?.invoke()
            }
        }
        onSuccessCallBack = {
            runOnUiThread {
                val combinedUris = (compressedImagesList + filterVideosList).toCollection(arrayListOf())
                onSuccessAndGoBack(combinedUris)
            }
        }
    }

    /**
     * check for photo picker type either gallery or camera
     */
    private fun checkPhotoPickerType(type: String?) {
        when (type) {
            PhotoPicker.Companion.PhotoPickerType.CAMERA.name -> {
                cameraOnly()
            }

            PhotoPicker.Companion.PhotoPickerType.GALLERY.name -> {
                selectLauncher()
            }

            PhotoPicker.Companion.PhotoPickerType.CHOOSER.name -> {
                cameraGalleryBoth()
            }
        }
    }

    /**
     * function for crop activity
     */
    private fun handleCrop(imageUris: ArrayList<Uri>) {
        if (isCropEnable) {
            val themeCropRequest = CropRequest.Auto(sourceUri = imageUris[0], requestCode = RC_CROP_IMAGE)
            val cropIntent = CropActivity.newIntent(this@MediaPickerActivity, themeCropRequest)
            cropIntent.putExtra("chooserDetectClickType", detectChooserClickType?.name)
            cropLauncher?.launch(cropIntent)
        } else {
            doCompression(imageUris)
        }
    }

    /**
     * function for open camera
     */
    private fun cameraOnly() {
        if (checkCameraPermission()) {
            checkCameraFacingType(cameraFacingType)
        } else {
            requestCameraPermission()
        }
    }

    /**
     * function for check camera facing type
     * faceType == 0 is Back Camera
     * faceType == 1 is Front Camera
     */
    private fun checkCameraFacingType(cameraFacingType: String?) {
        val faceType = when (cameraFacingType) {
            PhotoPicker.Companion.CameraFacingType.BACK.name -> {
                0
            }

            PhotoPicker.Companion.CameraFacingType.FRONT.name -> {
                1
            }

            else -> {
                0
            }
        }

        openCamera(faceType)
    }

    /**
     * function for open back camera
     */
    private fun openCamera(faceType: Int) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val values = ContentValues()
        cameraImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val packageManager: PackageManager = this@MediaPickerActivity.packageManager
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", faceType)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
            cameraLauncher?.launch(cameraIntent)
        } else {
            showToast("Camera Feature is not available on your device")
        }
    }

    /**
     * function for open gallery
     */
    private fun openGalleryForImage() {
        if (checkReadStoragePermission()) {
            launchGalleryForImage()
        } else {
            requestReadStoragePermission()
        }
    }

    private fun openGalleryForVideo() {
        if (checkVideoPermission()) {
            launchGalleryForVideo()
        } else {
            readVideoStoragePermission()
        }
    }

    private fun openGalleryForBothType() {
        if (checkStoragePermissionForBoth()) {
            launchGalleryForBothType()
        } else {
            requestForGalleryAndVideo()
        }
    }

    private fun launchGalleryForImage() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, isMultipleSelection)
        galleryLauncher?.launch(galleryIntent)
    }

    private fun launchGalleryForBothType() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "*/*"
        pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, isMultipleSelection)
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        galleryLauncher?.launch(pickIntent)
    }

    private fun launchGalleryForVideo() {
        val videoIntent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        videoIntent.type = "video/*"
        videoIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, isMultipleSelection)
        galleryLauncher?.launch(videoIntent)
    }

    /**
     * function for open bottom sheet to chooser for camera or gallery
     */
    private fun cameraGalleryBoth() {
        openBottomSheetDialog(
            this@MediaPickerActivity,
            onCameraClick = {
                cameraOnly()
            },
            onGalleryClick = {
                selectLauncher()
            },
        )
    }

    private fun selectLauncher() {
        when (mediaType) {
            PhotoPicker.Companion.MediaType.BOTH.name -> {
                openGalleryForBothType()
            }
            PhotoPicker.Companion.MediaType.VIDEO.name -> {
                openGalleryForVideo()
            }
            else -> {
                openGalleryForImage()
            }
        }
    }

    /**
     * function for open bottom sheet
     */
    @SuppressLint("InflateParams")
    private fun openBottomSheetDialog(context: Context, onCameraClick: () -> Unit, onGalleryClick: () -> Unit) {
        val layoutInflater = LayoutInflater.from(context)
        val bottomSheetDialog = BottomSheetDialog(context)

        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        val cameraButton = bottomSheetView.findViewById<ImageButton>(R.id.cameraButtonBottomSheet)
        val galleryButton = bottomSheetView.findViewById<ImageButton>(R.id.galleryButtonBottomSheet)
        cameraButton.setOnClickListener {
            isBottomSheetButtonClicked = true
            detectChooserClickType = DetectChooserClickType.CameraClick
            onCameraClick.invoke()
            bottomSheetDialog.dismiss()
        }

        galleryButton.setOnClickListener {
            isBottomSheetButtonClicked = true
            detectChooserClickType = DetectChooserClickType.GalleryClick
            onGalleryClick.invoke()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setOnDismissListener {
            if (!isBottomSheetButtonClicked) {
                finish()
            }
        }
        bottomSheetDialog.show()
    }

    private fun onSuccessAndGoBack(uriList: ArrayList<Uri>) {
        if (uriList.isEmpty()) {
            onFailAndGoBack()
        } else {
            PhotoPicker.successCallback?.invoke(uriList)
            finish()
        }
    }

    private fun onFailAndGoBack() {
        PhotoPicker.failCallback?.invoke("Couldn't Complete the Process")
        finish()
    }

    private fun doCompression(imageUris: ArrayList<Uri>) {
        val combinedUris = (imageUris + filterVideosList).toCollection(arrayListOf())
        if (compressImage) {
            try {
                compressingImageCommon(imageUris)
            } catch (e: Exception) {
                Log.e("compressImageButCropFalseException", e.toString())
                onSuccessAndGoBack(combinedUris)
            }
        } else {
            onSuccessAndGoBack(combinedUris)
        }
    }

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        const val STORAGE_READ_PERMISSION_REQUEST_CODE = 1005
        const val STORAGE_MULTI_PERMISSION_REQUEST_CODE = 1006 //separate request code for both type
        const val RC_CROP_IMAGE = 102
        const val GALLERY_CROP_RESULT = 104
        const val CAMERA_CROP_RESULT = 105

        enum class DetectChooserClickType { CameraClick, GalleryClick }
    }
}

