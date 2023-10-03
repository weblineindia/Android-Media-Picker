package com.wli.mediapicker.crop.aspectratio.extensions

import android.content.Context
import android.util.TypedValue

fun Context.fetchAccentColor(): Int {
    val typedValue = TypedValue()
    val a = obtainStyledAttributes(typedValue.data, intArrayOf(androidx.appcompat.R.attr.colorAccent))
    val color = a.getColor(0, 0)
    a.recycle()
    return color
}