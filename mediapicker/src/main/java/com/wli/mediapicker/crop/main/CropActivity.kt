package com.wli.mediapicker.crop.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.wli.mediapicker.MediaPickerActivity
import com.wli.mediapicker.MediaPickerActivity.Companion.CAMERA_CROP_RESULT
import com.wli.mediapicker.MediaPickerActivity.Companion.GALLERY_CROP_RESULT
import com.wli.mediapicker.R
import com.wli.mediapicker.crop.ui.CroppedBitmapData
import com.wli.mediapicker.crop.ui.ImageCropFragment
import com.wli.mediapicker.databinding.ActivityCropBinding
import com.wli.mediapicker.photopickerbuilder.PhotoPicker
import com.wli.mediapicker.photopickerbuilder.utils.bitmapToUri
import com.wli.mediapicker.photopickerbuilder.utils.copyExif
import com.wli.mediapicker.photopickerbuilder.utils.getImagePathFromUri

class CropActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropBinding

    private lateinit var viewModel: CropActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_crop)

        viewModel = ViewModelProviders.of(this)[CropActivityViewModel::class.java]

        val cropRequest = intent.getParcelableExtra(KEY_CROP_REQUEST) ?: CropRequest.empty()
        val chooserDetectClickType = intent.getStringExtra("chooserDetectClickType")
        if (savedInstanceState == null) {
            val cropFragment = ImageCropFragment.newInstance(cropRequest).apply {
                onApplyClicked = { croppedImage ->
                    if (PhotoPicker.photoPickerType == PhotoPicker.Companion.PhotoPickerType.CAMERA) {
                        setCameraImageResultForApplyClick(croppedImage, cropRequest)
                    } else if (PhotoPicker.photoPickerType == PhotoPicker.Companion.PhotoPickerType.GALLERY) {
                        setGalleryImageResultForApplyClick(croppedImage, cropRequest)
                    } else if (PhotoPicker.photoPickerType == PhotoPicker.Companion.PhotoPickerType.CHOOSER) {
                        if (chooserDetectClickType == MediaPickerActivity.Companion.DetectChooserClickType.CameraClick.name) {
                            setCameraImageResultForApplyClick(croppedImage, cropRequest)
                        } else if (chooserDetectClickType == MediaPickerActivity.Companion.DetectChooserClickType.GalleryClick.name) {
                            setGalleryImageResultForApplyClick(croppedImage, cropRequest)
                        }
                    }
                }
                onCancelClicked = {
                    if (PhotoPicker.photoPickerType == PhotoPicker.Companion.PhotoPickerType.CAMERA) {
                        setCameraImageResultForCancelClick(cropRequest)
                    } else if (PhotoPicker.photoPickerType == PhotoPicker.Companion.PhotoPickerType.GALLERY) {
                        setGalleryImageResultForCancelClick(cropRequest)
                    } else if (PhotoPicker.photoPickerType == PhotoPicker.Companion.PhotoPickerType.CHOOSER) {
                        if (chooserDetectClickType == MediaPickerActivity.Companion.DetectChooserClickType.CameraClick.name) {
                            setCameraImageResultForCancelClick(cropRequest)
                        } else if (chooserDetectClickType == MediaPickerActivity.Companion.DetectChooserClickType.GalleryClick.name) {
                            setGalleryImageResultForCancelClick(cropRequest)
                        }
                    }
                }
            }
            supportFragmentManager.beginTransaction().add(R.id.containerCroppy, cropFragment)
                .commitAllowingStateLoss()
        }


        viewModel.getSaveBitmapLiveData().observe(this, Observer {
            setResult(Activity.RESULT_OK, Intent().apply { data = it })
            finish()
        })
    }

    /**
     * handled crop result for gallery for apply cropped image with copied metadata
     */
    private fun setGalleryImageResultForApplyClick(
        croppedImage: CroppedBitmapData,
        cropRequest: CropRequest
    ) {
        val galleryResultPassIntentData = Intent()
        galleryResultPassIntentData.data = croppedImage.croppedBitmap?.let {
            bitmapToUri(it)
        }
        val originalImagePath = getImagePathFromUri(cropRequest.sourceUri)
        val newImagePath = galleryResultPassIntentData.data?.let {
            this.getImagePathFromUri(it)
        }
        copyExif(originalImagePath, newImagePath)
        setResult(GALLERY_CROP_RESULT, galleryResultPassIntentData)
        finish()
    }

    /**
     * handled crop result for camera for apply cropped image with copied metadata
     */
    private fun setCameraImageResultForApplyClick(
        croppedImage: CroppedBitmapData,
        cropRequest: CropRequest
    ) {
        val cameraResultPassIntentData = Intent()
        cameraResultPassIntentData.data = croppedImage.croppedBitmap?.let {
            this.bitmapToUri(it)
        }
        val originalImagePath = this.getImagePathFromUri(cropRequest.sourceUri)
        val newImagePath = cameraResultPassIntentData.data?.let { this.getImagePathFromUri(it) }
        copyExif(originalImagePath, newImagePath)
        setResult(CAMERA_CROP_RESULT, cameraResultPassIntentData)
        finish()
    }

    /**
     * handled crop result for gallery default selected image
     */
    private fun setGalleryImageResultForCancelClick(cropRequest: CropRequest) {
        val galleryResultPassIntentData = Intent()
        galleryResultPassIntentData.data = cropRequest.sourceUri
        setResult(GALLERY_CROP_RESULT, galleryResultPassIntentData)
        finish()
    }

    /**
     * handled crop result for camera for default selected image
     */
    private fun setCameraImageResultForCancelClick(cropRequest: CropRequest) {
        val cameraResultPassIntentData = Intent()
        cameraResultPassIntentData.data = cropRequest.sourceUri
        setResult(CAMERA_CROP_RESULT, cameraResultPassIntentData)
        finish()
    }

    companion object {

        private const val KEY_CROP_REQUEST = "KEY_CROP_REQUEST"

        fun newIntent(context: Context, cropRequest: CropRequest): Intent {
            return Intent(context, CropActivity::class.java).apply {
                Bundle().apply { putParcelable(KEY_CROP_REQUEST, cropRequest) }
                    .also { this.putExtras(it) }
            }
        }
    }
}