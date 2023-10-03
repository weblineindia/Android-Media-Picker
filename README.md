# Media Picker Library

Welcome to Media Picker Android Library!

![image](/media/image.png) &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ![video](/media/video.mp4)

## Features

* Media Picker library with camera & gallery options
* Multiple or single image and video selection
* Crop functionalities for picked images
* MediaPicker type chooser functionalities with Camera & Gallery option

## Getting Started

To build and run the project, follow these steps:

1. Clone the repository
2. Open the project in Android Studio
3. Build and run the app on your device or emulator

## Notes

1. To use of crop functionalities add dataBinding true in Build.gradle(app) file.

```
android {
    ...
    dataBinding {
        enabled = true
    }
    ...
}
```

### Implementation:

```
 PhotoPicker.PhotoPickerBuilder(this)
   .photoPickerType(photoPickerType)
   .cameraFacingType(cameraType)
   .crop(isCrop)
   .compress(1.0)
   .setMediaType(mediaType)
   .isMultipleSelection(isMultipleAllowed)
   .setCallBack(successCallback = { selectedUris ->
   //get all the selected images or video Uris here
   selectedMediaAdapter.addAll(selectedUris.toCollection(arrayListOf()))
   }, failCallback = { message ->
   Toast.makeText(this, message, Toast.LENGTH_LONG).show()
   }).build()
   ```

* **Description of options given in sample application**

    1) Open Gallery: Can pick multiple images and can crop image from gallery
        ```
         photoPickerType = PhotoPicker.Companion.PhotoPickerType.GALLERY
         cameraType = PhotoPicker.Companion.CameraFacingType.BACK
         mediaType = PhotoPicker.Companion.MediaType.IMAGE
         isCrop = true
         isMultipleAllowed = true
        ```
    2) Open Camera: Can take picture from Camera
        ```
        photoPickerType = PhotoPicker.Companion.PhotoPickerType.CAMERA
        cameraType = PhotoPicker.Companion.CameraFacingType.FRONT
        mediaType = PhotoPicker.Companion.MediaType.IMAGE
        isCrop = false
        isMultipleAllowed = false
        ```
    3) Select Image or Video: Can pick multiple image or videos from gallery. Image can be cropped if option is enable
       ```
       photoPickerType = PhotoPicker.Companion.PhotoPickerType.CHOOSER
       cameraType = PhotoPicker.Companion.CameraFacingType.BACK
       mediaType = PhotoPicker.Companion.MediaType.BOTH
       isCrop = true
       isMultipleAllowed = true
       ```
    4) Select Video: Can pick single video from gallery
       ```
       photoPickerType = PhotoPicker.Companion.PhotoPickerType.GALLERY
       cameraType = PhotoPicker.Companion.CameraFacingType.BACK
       mediaType = PhotoPicker.Companion.MediaType.VIDEO
       isCrop = false
       isMultipleAllowed = true
       ```

### Requirements
    * minSdkVersion = 24