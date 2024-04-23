package com.gallerydemo.imageloading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Environment.isExternalStorageRemovable
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import com.gallerydemo.R
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


public class ImageLoader(private val context: Context) {

    private var memoryCache = LruCache<String, Bitmap>(50)

    //    private val diskCacheDir = File(Environment.getExternalStorageDirectory(), "app_cache")
    private var diskDirectory: File? = null
//    private var diskCache = null
//        DiskLruCache.open(diskCacheDir, 1, 1, 10 * 1024 * 1024) // 10MB Disk Cache

    private val DISK_CACHE_SIZE = 1024 * 1024 * 50
    private val DISK_CACHE_SUBDIR = "thumbnails"
    private var diskLruCache: DiskLruCache? = null
    private val diskCacheLock = ReentrantLock()
    private val diskCacheLockCondition: Condition = diskCacheLock.newCondition()
    private var diskCacheStarting = true

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }

        diskDirectory = getDiskCacheDir(context, DISK_CACHE_SUBDIR)
        diskCacheLock.withLock {
            diskLruCache = DiskLruCache.openCache(context, diskDirectory!!, DISK_CACHE_SIZE)
            diskCacheStarting = false // Finished initialization
            diskCacheLockCondition.signalAll() // Wake any waiting threads
        }
    }


    fun loadImage(url: String, imageView: ImageView, placeHolder:Int) {
        imageView.setImageResource(placeHolder)
        val bitmap = memoryCache.get(url)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
            return
        }
        val cachedBitmap = getBitmapFromDiskCache(url)
        if (cachedBitmap != null) {
            memoryCache.put(url, cachedBitmap)
            imageView.setImageBitmap(cachedBitmap)
            return
        }

        downloadImage(url, imageView)
    }


    private fun downloadImage(url: String, imageView: ImageView) {

        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {

            try {
                //Background work here
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)

                handler.post {
                    addBitmapToCache(url, bitmap)
                    imageView.post { imageView.setImageBitmap(bitmap) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun addBitmapToCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap)
        }

        // Also add to disk cache
        synchronized(diskCacheLock) {
            diskLruCache?.apply {
                if (diskLruCache?.containsKey(key) == false) {
                    diskLruCache?.put(key, bitmap)
                }
            }
        }
    }

    private fun getBitmapFromMemCache(url: String): Bitmap? {
        return memoryCache.get(url)
    }

    private fun getBitmapFromDiskCache(key: String): Bitmap? =
        diskCacheLock.withLock {
            // Wait while disk cache is started from background thread
            while (diskCacheStarting) {
                try {
                    diskCacheLockCondition.await()
                } catch (_: InterruptedException) {
                }

            }
            return diskLruCache?.get(key)
        }

    private fun getDiskCacheDir(context: Context, uniqueName: String): File {
        val cachePath =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                || !isExternalStorageRemovable()
            ) {
                context.externalCacheDir?.path
            } else {
                context.cacheDir.path
            }
        return File(cachePath + File.separator + uniqueName)
    }



}
