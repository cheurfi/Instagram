package com.example.instagram.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.Toast
import com.example.instagram.R
import com.example.instagram.helper.ImageProcessor
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity() {

    private lateinit var bitmap: Bitmap
    private lateinit var helper: ImageProcessor
    private lateinit var processedImage: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        helper = ImageProcessor()
        requestPermission()

        start.setOnClickListener {
            pickPhoto()
        }

        binary.setOnClickListener {
            launch {
                processedImage = helper.makeBlackWhite(bitmap)
                image.setImageBitmap(processedImage)
            }
            launch {
                withContext(Dispatchers.IO) {
                    helper.saveToFile(processedImage, this@MainActivity)
                }
            }
        }

        contrast.setOnClickListener {
            processedImage = helper.increaseContrast(bitmap, 3f, 70f)
            image.setImageBitmap(processedImage)

            launch {
                withContext(Dispatchers.IO) {
                    helper.saveToFile(processedImage, this@MainActivity)
                }
            }
        }

        blur.setOnClickListener {
            processedImage = helper.blur(bitmap, this)
            image.setImageBitmap(processedImage)

            launch {
                withContext(Dispatchers.IO) {
                    helper.saveToFile(processedImage, this@MainActivity)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri = data?.data
            val inputStream = contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(inputStream)
            image.setImageBitmap(bitmap)

            buttons.visibility = View.VISIBLE
        } else Toast.makeText(this, "No Photo selected", Toast.LENGTH_LONG).show()
    }

    private fun pickPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_PHOTO)
    }

    private fun requestPermission() {
        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1
            )
        }
    }

    companion object {
        const val PICK_PHOTO = 1234
    }
}
