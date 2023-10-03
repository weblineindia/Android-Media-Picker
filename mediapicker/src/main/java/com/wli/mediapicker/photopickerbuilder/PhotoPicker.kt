package com.wli.mediapicker.photopickerbuilder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.wli.mediapicker.MediaPickerActivity
import com.wli.mediapicker.photopickerbuilder.utils.Constant.ALLOW_MULTI_SELECTION
import com.wli.mediapicker.photopickerbuilder.utils.Constant.CAMERA_FACING_TYPE
import com.wli.mediapicker.photopickerbuilder.utils.Constant.ENABLE_COMPRESS_IMAGE
import com.wli.mediapicker.photopickerbuilder.utils.Constant.ENABLE_CROP
import com.wli.mediapicker.photopickerbuilder.utils.Constant.MEDIA_TYPE
import com.wli.mediapicker.photopickerbuilder.utils.Constant.PICKER_TYPE
import com.wli.mediapicker.photopickerbuilder.utils.Constant.SIZE_IN_MB


class PhotoPicker constructor(builder: PhotoPickerBuilder) {
    private val context: Context?

    init {
        photoPickerType = builder.photoPickerType!!
        context = builder.context
        successCallback = builder.successCallback
        failCallback = builder.failCallback
        val intent = Intent(context, MediaPickerActivity::class.java)
        val bundle = Bundle()
        bundle.putString(PICKER_TYPE, photoPickerType.name)
        bundle.putString(CAMERA_FACING_TYPE, builder.cameraFacingType.name)
        bundle.putString(MEDIA_TYPE, builder.mediaType.name)
        bundle.putBoolean(ENABLE_CROP, builder.crop)
        bundle.putBoolean(ENABLE_COMPRESS_IMAGE, builder.compressImage)
        bundle.putDouble(SIZE_IN_MB, builder.sizeInMB)
        bundle.putBoolean(ALLOW_MULTI_SELECTION, builder.isMultiple)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }


    class PhotoPickerBuilder(var context: Context) {
        var photoPickerType: PhotoPickerType? = PhotoPickerType.CHOOSER
        var cameraFacingType: CameraFacingType = CameraFacingType.BACK
        var crop: Boolean = false
        var compressImage: Boolean = false
        var sizeInMB: Double = 0.0
        var successCallback: ((MutableList<Uri>) -> Unit)? = null
        var failCallback: ((String) -> Unit)? = null
        var isMultiple: Boolean = false
        var mediaType = MediaType.IMAGE

        fun photoPickerType(photoPickerType: PhotoPickerType): PhotoPickerBuilder {
            this.photoPickerType = photoPickerType
            return this
        }

        fun cameraFacingType(cameraFacingType: CameraFacingType): PhotoPickerBuilder {
            this.cameraFacingType = cameraFacingType
            return this
        }

        fun crop(crop: Boolean): PhotoPickerBuilder {
            this.crop = crop
            return this
        }

        fun compress(sizeInMB: Double): PhotoPickerBuilder {
            this.compressImage = true
            this.sizeInMB = sizeInMB
            return this
        }

        fun setCallBack(successCallback: ((MutableList<Uri>) -> Unit)?, failCallback: ((String) -> Unit)?): PhotoPickerBuilder {
            this.successCallback = successCallback
            this.failCallback = failCallback
            return this
        }

        fun isMultipleSelection(isMultiple: Boolean): PhotoPickerBuilder {
            this.isMultiple = isMultiple
            return this
        }

        fun setMediaType(mediaType: MediaType) : PhotoPickerBuilder {
            this.mediaType = mediaType
            return this
        }


        //Return the finally constructed PhotoPicker object
        fun build(): PhotoPicker {
            return PhotoPicker(this)
        }
    }

    companion object {
        var photoPickerType: PhotoPickerType = PhotoPickerType.CHOOSER
        var successCallback: ((MutableList<Uri>) -> Unit)? = null
        var failCallback: ((String) -> Unit)? = null

        enum class PhotoPickerType { CAMERA, GALLERY, CHOOSER }
        enum class MediaType { VIDEO, IMAGE, BOTH }
        enum class CameraFacingType { FRONT, BACK }
    }
}