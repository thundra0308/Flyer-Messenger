package com.example.flyer.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageDownloadTask(private val listener: OnImageDownloadListener) : AsyncTask<String, Void, Bitmap>() {

    interface OnImageDownloadListener {
        fun onImageDownloaded(bitmap: Bitmap?)
    }

    override fun doInBackground(vararg urls: String): Bitmap? {
        val urlString = urls[0]
        var bitmap: Bitmap? = null
        var inputStream: InputStream? = null
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            inputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            connection?.disconnect()
        }
        return bitmap
    }

    override fun onPostExecute(result: Bitmap?) {
        listener.onImageDownloaded(result)
    }
}