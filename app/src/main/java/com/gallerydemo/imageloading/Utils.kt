package com.gallerydemo.imageloading

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File


/**
 * Class containing some static utility methods.
 */
object Utils {
    const val IO_BUFFER_SIZE = 8 * 1024

    @SuppressLint("NewApi")
    fun getBitmapSize(bitmap: Bitmap): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            bitmap.byteCount
        } else bitmap.rowBytes * bitmap.height
    }

    @get:SuppressLint("NewApi")
    val isExternalStorageRemovable: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            Environment.isExternalStorageRemovable()
        } else true

    @SuppressLint("NewApi")
    fun getExternalCacheDir(context: Context): File? {
        if (hasExternalCacheDir()) {
            return context.externalCacheDir
        }
        val cacheDir = "/Android/data/" + context.packageName + "/cache/"
        return File(Environment.getExternalStorageDirectory().path + cacheDir)
    }
    @SuppressLint("NewApi")
    fun getUsableSpace(path: File): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.usableSpace
        }
        val stats = StatFs(path.path)
        return stats.blockSize.toLong() * stats.availableBlocks.toLong()
    }

    fun hasExternalCacheDir(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
    }

}