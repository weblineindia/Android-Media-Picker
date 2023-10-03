package com.wli.mediapicker.crop

import android.app.Activity
import com.wli.mediapicker.crop.main.CropRequest
import com.wli.mediapicker.crop.main.CropActivity

object CropObject {

    fun start(activity: Activity, cropRequest: CropRequest) {
        CropActivity.newIntent(context = activity, cropRequest = cropRequest)
            .also { activity.startActivityForResult(it, cropRequest.requestCode) }
    }
}