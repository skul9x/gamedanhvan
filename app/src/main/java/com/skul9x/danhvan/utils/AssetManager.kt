package com.skul9x.danhvan.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AssetManager(private val context: Context) {

    fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val filename = "img_${UUID.randomUUID()}.png"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    fun saveAudioToInternalStorage(bytes: ByteArray): String {
        val filename = "audio_${UUID.randomUUID()}.wav"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out ->
            out.write(bytes)
        }
        return file.absolutePath
    }

    // Stub for AI Image Generation
    fun generatePlaceholderImage(text: String): String {
        val width = 512
        val height = 512
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        // Background
        paint.color = Color.parseColor("#FFEB3B") // Yellow background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // Text
        paint.color = Color.BLACK
        paint.textSize = 60f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(text, width / 2f, height / 2f, paint)
        
        return saveImageToInternalStorage(bitmap)
    }

    fun deleteAsset(path: String?) {
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
