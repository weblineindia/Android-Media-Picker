package com.wli.mediapicker.crop.main

import android.net.Uri
import android.os.Parcelable
import com.wli.mediapicker.R
import com.wli.mediapicker.crop.aspectratio.model.AspectRatio
import kotlinx.parcelize.Parcelize

@Parcelize
open class CropRequest(
    open val sourceUri: Uri,
    open val requestCode: Int,
    open val excludedAspectRatios: List<AspectRatio>,
    open val cropTheme: CropTheme
) : Parcelable {

    @Parcelize
    class Manual(
        override val sourceUri: Uri,
        val destinationUri: Uri,
        override val requestCode: Int,
        override val excludedAspectRatios: List<AspectRatio> = arrayListOf(),
        override val cropTheme: CropTheme = CropTheme(R.color.blue)
    ) : CropRequest(sourceUri, requestCode, excludedAspectRatios, cropTheme)

    @Parcelize
    class Auto(
        override val sourceUri: Uri,
        override val requestCode: Int,
        val storageType: StorageType = StorageType.CACHE,
        override val excludedAspectRatios: List<AspectRatio> = arrayListOf(),
        override val cropTheme: CropTheme = CropTheme(R.color.orange)
    ) : CropRequest(sourceUri, requestCode, excludedAspectRatios, cropTheme)

    companion object {
        fun empty(): CropRequest =
            CropRequest(Uri.EMPTY, -1, arrayListOf(), CropTheme(R.color.blue))
    }
}


