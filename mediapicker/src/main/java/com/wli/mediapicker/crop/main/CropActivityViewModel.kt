package com.wli.mediapicker.crop.main

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable

class CropActivityViewModel(val app: Application) : AndroidViewModel(app) {

    private val disposable = CompositeDisposable()

    private val saveBitmapLiveData = MutableLiveData<Uri>()

    fun getSaveBitmapLiveData(): LiveData<Uri> = saveBitmapLiveData

    override fun onCleared() {
        super.onCleared()
        if (disposable.isDisposed.not()) {
            disposable.dispose()
        }
    }

}