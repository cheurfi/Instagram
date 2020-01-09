package com.example.instagram.helper

import android.content.Context
import android.graphics.*
import android.media.MediaScannerConnection
import android.os.Environment
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*
import kotlin.math.roundToInt

class ImageProcessor {

    fun makeBlackWhite(bitmap: Bitmap): Bitmap {
        val blackWhiteImage =
            Bitmap.createBitmap(Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888))
        val canvas = Canvas(blackWhiteImage)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return blackWhiteImage
    }

    fun increaseContrast(bm: Bitmap, contrast: Float, brightness: Float): Bitmap {
        val colorMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))
        val bitmap = Bitmap.createBitmap(bm.width, bm.height, bm.config)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bm, 0f, 0f, paint)

        return bitmap
    }

    fun blur(bitmap: Bitmap, context: Context): Bitmap {
        val width = (bitmap.width * BITMAP_SCALE).roundToInt()
        val height = (bitmap.height * BITMAP_SCALE).roundToInt()

        val original = Bitmap.createScaledBitmap(bitmap, width, height, false)
        val result = Bitmap.createBitmap(original)

        val renderScript = RenderScript.create(context)
        val blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        val allocation = Allocation.createFromBitmap(renderScript, original)
        val allocationOutput = Allocation.createFromBitmap(renderScript, result)
        blur.setRadius(RADIUS)
        blur.setInput(allocation)
        blur.forEach(allocationOutput)
        allocationOutput.copyTo(result)

        return result
    }

    fun saveToFile(bitmap: Bitmap, context: Context) {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        path.mkdirs()
        val file = File(path, UUID.randomUUID().toString() + ".png")
        val outputStream = FileOutputStream(file)
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            MediaScannerConnection.scanFile(context,
                arrayOf(file.absolutePath),
                null
            ) { path, uri ->
                Log.e("saving", path)
                Log.e("saving", uri.toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream.close()
        }
    }

    companion object {
        const val BITMAP_SCALE = 0.5f
        const val RADIUS = 15f
    }
}