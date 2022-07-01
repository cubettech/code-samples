package io.odinmanufacturing.utils

import android.graphics.Bitmap
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utils class to save images to local storage
 */
object ImageUtils {
    /**
     * To save bitmap image to local storage
     * @param bitmap Bitmap?
     * @param name String
     */
    fun saveImages(bitmap: Bitmap?, name: String) {
        try {
            val photoPath = File(getPhotoPath(), "$name.jpg")
            val stream = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray: ByteArray = stream.toByteArray()
            val outputStream: FileOutputStream = FileOutputStream(photoPath)
            outputStream.write(byteArray)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Returns  file name for the image
     * @return String
     */
    fun getFileName(): String {
        return System.currentTimeMillis().toString() + "-photo"
    }

    /**
     * Returns folder path for image to save
     * @return String?
     */
    private fun getPhotoPath(): String? {
        val photoPath: File = File(Environment.getExternalStorageDirectory(), "Photos")
        if (!photoPath.exists()) {
            if (!photoPath.mkdirs()) {
            }
        }
        return photoPath.toString()
    }
}
