package com.gallerydemo.imageloading

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.Collections


class DiskLruCache private constructor(private val mCacheDir: File, maxByteSize: Int) {
    private var cacheSize = 0
    private var cacheByteSize = 0
    private val maxCacheItemSize = 50
    private var maxCacheByteSize = (1024 * 1024 * 50)
    private var mCompressFormat = CompressFormat.PNG
    private var mCompressQuality = 80
    private val mLinkedHashMap = Collections.synchronizedMap(
        LinkedHashMap<String, String?>(
            INITIAL_CAPACITY, LOAD_FACTOR, true
        )
    )
    init {
        maxCacheByteSize = maxByteSize
    }

    /**
     * Add a bitmap to the disk cache.
     *
     * @param key A unique identifier for the bitmap.
     * @param data The bitmap to store.
     */
    fun put(key: String, data: Bitmap) {
        synchronized(mLinkedHashMap) {
            if (mLinkedHashMap[key] == null) {
                try {
                    val file = createFilePath(mCacheDir, key)
                    if (writeBitmapToFile(data, file)) {
                        put(key, file)
                        flushCache()
                    }
                } catch (e: FileNotFoundException) {
                    Log.e(TAG, "Error in put: " + e.message)
                } catch (e: IOException) {
                    Log.e(TAG, "Error in put: " + e.message)
                }
            }
        }
    }

    private fun put(key: String, file: String?) {
        mLinkedHashMap[key] = file
        cacheSize = mLinkedHashMap.size
        file?.let { cacheByteSize += File(it).length().toInt() }
    }

    /**
     * Flush the cache, removing oldest entries if the total size is over the specified cache size.
     * Note that this isn't keeping track of stale files in the cache directory that aren't in the
     * HashMap. If the images and keys in the disk cache change often then they probably won't ever
     * be removed.
     */
    private fun flushCache() {
        var eldestEntry: Map.Entry<String, String?>
        var eldestFile: File
        var eldestFileSize: Long
        var count = 0
        while (count < MAX_REMOVALS &&
            (cacheSize > maxCacheItemSize || cacheByteSize > maxCacheByteSize)
        ) {
            eldestEntry = mLinkedHashMap.entries.iterator().next()
            eldestEntry.value?.let {
                eldestFile = File(it)
                eldestFileSize = eldestFile.length()
                mLinkedHashMap.remove(eldestEntry.key)
                eldestFile.delete()
                cacheSize = mLinkedHashMap.size
                cacheByteSize -= eldestFileSize.toInt()
                count++
                Log.d(
                    TAG, "flushCache - Removed cache file, " + eldestFile + ", "
                            + eldestFileSize
                )
            }


        }
    }

    operator fun get(key: String): Bitmap? {
        synchronized(mLinkedHashMap) {
            val file = mLinkedHashMap[key]
            if (file != null) {
                Log.d(TAG, "Disk cache hit")
                return BitmapFactory.decodeFile(file)
            } else {
                val existingFile =
                    createFilePath(mCacheDir, key)
                if (existingFile != null && File(existingFile).exists()) {
                    put(key, existingFile)
                    Log.d(
                        TAG,
                        "Disk cache hit (existing file)"
                    )
                    return BitmapFactory.decodeFile(existingFile)
                }
            }
            return null
        }
    }

    fun containsKey(key: String): Boolean {
        if (mLinkedHashMap.containsKey(key)) {
            return true
        }
        val existingFile = createFilePath(mCacheDir, key)
        if (existingFile != null && File(existingFile).exists()) {
            put(key, existingFile)
            return true
        }
        return false
    }

    /**
     * Removes all disk cache entries from this instance cache dir
     */
    fun clearCache() {
        clearCache(mCacheDir)
    }


    fun createFilePath(key: String): String? {
        return createFilePath(mCacheDir, key)
    }

    fun setCompressParams(compressFormat: CompressFormat, quality: Int) {
        mCompressFormat = compressFormat
        mCompressQuality = quality
    }

    @Throws(IOException::class, FileNotFoundException::class)
    private fun writeBitmapToFile(bitmap: Bitmap, file: String?): Boolean {
        var out: OutputStream? = null
        return try {
            out = BufferedOutputStream(FileOutputStream(file), Utils.IO_BUFFER_SIZE)
            bitmap.compress(mCompressFormat, mCompressQuality, out)
        } finally {
            out?.close()
        }
    }

    companion object {
        private const val TAG = "DiskLruCache"
        private const val CACHE_FILENAME_PREFIX = "cache_"
        private const val MAX_REMOVALS = 4
        private const val INITIAL_CAPACITY = 32
        private const val LOAD_FACTOR = 0.75f

        private val cacheFileFilter = FilenameFilter { _, filename ->
            filename.startsWith(
                CACHE_FILENAME_PREFIX
            )
        }

        fun openCache(context: Context?, cacheDir: File, maxByteSize: Int): DiskLruCache? {
            if (!cacheDir.exists()) {
                cacheDir.mkdir()
            }
            return if (cacheDir.isDirectory && cacheDir.canWrite() && Utils.getUsableSpace(cacheDir) > maxByteSize
            ) {
                DiskLruCache(cacheDir, maxByteSize)
            } else null
        }

        private fun clearCache(context: Context, uniqueName: String) {
            val cacheDir = getDiskCacheDir(context, uniqueName)
            clearCache(cacheDir)
        }

        private fun clearCache(cacheDir: File) {
            val files = cacheDir.listFiles(cacheFileFilter)
            if (files != null) {
                for (i in files.indices) {
                    files[i].delete()
                }
            }
        }

        private fun getDiskCacheDir(context: Context, uniqueName: String): File {
            val cachePath =
                if (Environment.getExternalStorageState() === Environment.MEDIA_MOUNTED ||
                    !Utils.isExternalStorageRemovable
                ) Utils.getExternalCacheDir(context)?.path else context.cacheDir.path
            return File(cachePath + File.separator + uniqueName)
        }

        fun createFilePath(cacheDir: File, key: String): String? {
            try {

                return cacheDir.absolutePath + File.separator +
                        CACHE_FILENAME_PREFIX + URLEncoder.encode(key.replace("*", ""), "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, "createFilePath - $e")
            }
            return null
        }
    }
}