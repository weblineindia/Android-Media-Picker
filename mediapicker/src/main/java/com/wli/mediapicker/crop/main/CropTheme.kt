package com.wli.mediapicker.crop.main

import android.os.Parcelable
import androidx.annotation.ColorRes
import com.wli.mediapicker.R

@kotlinx.parcelize.Parcelize
data class CropTheme(@ColorRes val accentColor: Int) : Parcelable {

    companion object {
        fun default() = CropTheme(R.color.blue)
    }
}