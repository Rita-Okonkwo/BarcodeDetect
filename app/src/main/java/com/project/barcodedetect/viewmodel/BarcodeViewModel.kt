package com.project.barcodedetect.viewmodel

/**
 * A class acting as the viewmodel for the main activity
 */
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.SparseArray
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.project.barcodedetect.R
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*


class BarcodeViewModel : ViewModel() {

    var currentPhotoPath = MutableLiveData<String>()
    var displayValue = MutableLiveData<String>()
    var myBitmap = MutableLiveData<Bitmap>()

    init {
        displayValue.value = "Barcode Text shows here"
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath.value = absolutePath
        }
    }

    fun setUpDetector(context: Context) {
        val detector = BarcodeDetector.Builder(context.applicationContext)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        if (!detector.isOperational) {
            displayValue.value = "Couldn't set up detector"
            return
        }
        var frame = Frame.Builder().setBitmap(myBitmap.value).build()
        var barcodes = detector.detect(frame)
        for (index in 0 until barcodes.size()) {
            val thisCode = barcodes.valueAt(index)
            displayValue.value += thisCode.rawValue
        }
    }

    @Throws(FileNotFoundException::class)
    fun setPic(context: Context, uri: Uri?) {
        // Get the dimensions of the View
        val targetW: Int = 100
        val targetH: Int = 100

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true
            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
        }
        myBitmap.value = BitmapFactory.decodeStream(
            context.contentResolver
                .openInputStream(uri!!), null, bmOptions
        )
    }


}