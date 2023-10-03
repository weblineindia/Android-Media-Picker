package com.wli.mediapicker.photopickerbuilder.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.wli.mediapicker.MediaPickerActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun Activity.checkCameraPermission(): Boolean {
    val cameraPermission = Manifest.permission.CAMERA
    return ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.requestCameraPermission() {
    val cameraPermission = Manifest.permission.CAMERA
    ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), MediaPickerActivity.CAMERA_PERMISSION_REQUEST_CODE)
}

fun Activity.checkReadStoragePermission(): Boolean {
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //For Api 33
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.requestReadStoragePermission() {
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //For Api 33
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    ActivityCompat.requestPermissions(this, arrayOf(storagePermission), MediaPickerActivity.STORAGE_READ_PERMISSION_REQUEST_CODE)
}

fun Activity.checkVideoPermission(): Boolean {
    val videoStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //For Api 33
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(this, videoStoragePermission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.readVideoStoragePermission() {
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //For Api 33
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    ActivityCompat.requestPermissions(this, arrayOf(storagePermission), MediaPickerActivity.STORAGE_READ_PERMISSION_REQUEST_CODE)
}

fun Activity.checkStoragePermissionForBoth(): Boolean {
    var allGranted = false
    val storagePermission: ArrayList<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //For Api 33
            arrayListOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    storagePermission.onEach { permission ->
        allGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
    return allGranted
}

fun Activity.requestForGalleryAndVideo() {
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //For Api 33
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    ActivityCompat.requestPermissions(this, storagePermission, MediaPickerActivity.STORAGE_MULTI_PERMISSION_REQUEST_CODE)
}

fun copyExif(originalPath: String?, newPath: String?) {
    try {
        val attributes = arrayOf(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_GPS_LATITUDE
        )
        val oldExif = ExifInterface(
            originalPath!!
        )
        val newExif = ExifInterface(
            newPath!!
        )
        if (attributes.isNotEmpty()) {
            for (i in attributes.indices) {
                val value = oldExif.getAttribute(attributes[i].toString())
                if (value != null) newExif.setAttribute(attributes[i].toString(), value)
            }
            newExif.saveAttributes()
        }
    } catch (e: Exception) {
        Log.e("imagePathException", e.toString())
    }
}

fun Activity.getImagePathFromUri(uri: Uri): String? {
    if (uri.scheme == "file") {
        return uri.path
    }
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = contentResolver.query(uri, projection, null, null, null)

    cursor?.use { c ->
        if (c.moveToFirst()) {
            val columnIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            return c.getString(columnIndex)
        }
    }

    return null
}

fun Activity.showToast(toastMsg: String) {
    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show()
}

fun Activity.bitmapToUri(bitmap: Bitmap): Uri {
    val fileName = "image_${System.currentTimeMillis()}.jpg"

    val tempFile = File(this.cacheDir, fileName)
    tempFile.deleteOnExit()

    val outputStream = FileOutputStream(tempFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.flush()
    outputStream.close()

    return tempFile.toUri()
}

fun Activity.getFileSizeFromUri(uri: Uri): Long {
    val filePath = getImagePathFromUri(uri)
    val file = filePath?.let { File(it) }
    return file!!.length()
}

fun Activity.compressImage(uri: Uri, fileSizeInKB: Long, sizeInMB: Double, onSuccess: ((Bitmap) -> Unit)) {
    var bitmap: Bitmap?
    try {
        val inputStream = contentResolver.openInputStream(uri)
        bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        val convertInKB = sizeInMB * 1024
        val countImageQualityBasedOnSize = (convertInKB * 100) / fileSizeInKB
        val quality = (countImageQualityBasedOnSize * 0.8).toInt()
        val compression = if (quality > 100) {
            100
        } else {
            quality
        }
        outputStream.reset()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compression, outputStream)
        val compressedData = outputStream.toByteArray()
        bitmap.recycle()
        bitmap = BitmapFactory.decodeByteArray(compressedData, 0, compressedData.size)
        onSuccess.invoke(bitmap)
    } catch (e: Exception) {
        Log.e("ExceptionInCompressImage", e.toString())
    }
}

fun Activity.isVideoUri(uri: Uri): Boolean {
    val mediaMimeType = contentResolver.getType(uri)
    return mediaMimeType?.startsWith("video") == true
}