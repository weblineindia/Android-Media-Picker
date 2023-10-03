package com.wli.mediapicker.crop.util.file

import android.content.Context
import android.os.Environment
import com.wli.mediapicker.crop.main.StorageType.CACHE
import com.wli.mediapicker.crop.main.StorageType.EXTERNAL
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object FileCreator {

    fun createFile(fileOperationRequest: FileOperationRequest, context: Context): File {
        return when (fileOperationRequest.storageType) {
            CACHE -> createCacheFile(context)
            EXTERNAL -> createExternalFile(fileOperationRequest, context)
        }
    }

    private fun createCacheFile(context: Context): File {
        val outputDir = context.cacheDir
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "image_$timeStamp"
        return File.createTempFile(
            fileName,
            ".png",
            outputDir
        )
    }

    private fun createExternalFile(
        fileOperationRequest: FileOperationRequest,
        context: Context
    ): File {
        val path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "image_$timeStamp.jpg"
        val parentFolder = File(path, fileName)
            .also { it.mkdirs() }

        return File(
            parentFolder,
            "${fileOperationRequest.fileName}${fileOperationRequest.fileExtension.fileExtensionName}"
        )
    }
}