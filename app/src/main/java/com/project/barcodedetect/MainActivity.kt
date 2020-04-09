package com.project.barcodedetect

/**
 * Main activity for the barcode application
 */

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.project.barcodedetect.databinding.ActivityMainBinding
import com.project.barcodedetect.viewmodel.BarcodeViewModel
import java.io.File
import java.io.IOException
import java.security.AccessController.getContext

const val MY_WRITE_STORAGE = 1
const val REQUEST_IMAGE_CAPTURE = 1

class MainActivity : AppCompatActivity() {
    lateinit var viewModel: BarcodeViewModel
    lateinit var photoURI: Uri
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(BarcodeViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.myBitmap.observe(this, Observer { myBitmap ->
            binding.imgview.setImageBitmap(Bitmap.createScaledBitmap(myBitmap, 50, 50, false))
        })


        binding.button.setOnClickListener {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission_group.CAMERA,
                    Manifest.permission.CAMERA
                ),
                MY_WRITE_STORAGE
            )
        }

    }

    fun takePicture() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    viewModel.createImageFile(this)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.i("file", "not created")

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    Log.i("file", "file created")
                    photoURI = getUriForFile(this, "com.project.barcodedetect.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }

            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = photoURI
            launchMediaScanIntent(mediaScanIntent)
            viewModel.setPic(this, photoURI)
            viewModel.setUpDetector(this)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_WRITE_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    takePicture()
                } else {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT)
                        .show()
                }
                return
            }

        }

    }

    private fun launchMediaScanIntent(mediaScanIntent: Intent) {

        this.sendBroadcast(mediaScanIntent)
    }

}

