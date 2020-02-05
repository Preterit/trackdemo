package com.xiangxue.trackdemo.util

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.LruCache
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashSet

/**
 * Date:2020-02-04
 * author:lwb
 * Desc:
 */
class ImageCache {

    /**
     * 单利类
     */
    companion object {
        private var instance: ImageCache? = null
        fun getInstance(): ImageCache? {
            if (instance == null) {
                synchronized(ImageCache::class.java) {
                    if (instance == null) {
                        instance = ImageCache()
                    }
                }
            }
            return instance
        }
    }

    var lruCache: LruCache<String, Bitmap>? = null
    //复用池
    var reusablePool = Collections.synchronizedSet(HashSet<WeakReference<Bitmap>>())

    fun init(context: Context) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val cacheSize = am.memoryClass / 8 * 1024 * 1024

        lruCache = object : LruCache<String, Bitmap>(cacheSize) {
            //返回一张图片的大小
            override fun sizeOf(key: String?, value: Bitmap?): Int {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    return value?.allocationByteCount ?: super.sizeOf(key, value)
                }
                return value?.byteCount ?: super.sizeOf(key, value)
            }

            override fun entryRemoved(
                evicted: Boolean,
                key: String?,
                oldValue: Bitmap?,
                newValue: Bitmap?
            ) {
                /**
                 * 回收
                 */
                reusablePool.add(WeakReference<Bitmap>(oldValue,getReferenceQueue()))
                oldValue?.recycle()
            }
        }
    }

    private var referenceQueue: ReferenceQueue<Bitmap>? = null
    private var shutDown: Boolean = false  // 线程销毁 停止条件

    /**
     * 创建引用队列
     */
    fun getReferenceQueue(): ReferenceQueue<Bitmap> {
        if (referenceQueue == null) {
            referenceQueue = ReferenceQueue<Bitmap>()
            Thread {
                Runnable {
                    while (!shutDown) {
                        val remove = referenceQueue?.remove()
                        val bitmap = remove?.get()
                        if (bitmap != null && !bitmap.isRecycled) {
                            bitmap.recycle()
                        }
                    }
                }
            }.start()
        }
        return referenceQueue!!
    }

    /**
     * 将bitmap放到内存中
     */
    fun bitmap2Memory(key: String, value: Bitmap) {
        lruCache?.put(key, value)
    }

    /**
     * 从内存中获取bitmap
     */
    fun getBitmapFromMemory(key: String): Bitmap? {
        return lruCache?.get(key)
    }

    /**
     * 清空lrucache
     */
    fun clearMemory() {
        lruCache?.evictAll()
    }


    /**
     * 3.0 之前不能复用，
     * 3.0～4.4 宽高一样， inSampleSize = 1
     * 4.4 只要小于等于就行
     */
    fun getReusable(w: Int, h: Int, inSampleSize: Int): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return null
        }
        var reusable: Bitmap? = null

        var iterator = reusablePool.iterator()

        while (iterator.hasNext()) {
            val bitmap = iterator.next().get()
            if (bitmap != null) {
                if (chackInBitmap(bitmap, w, h, inSampleSize)) {
                    //复用
                    reusable = bitmap
                    //从复用池中移除
                    iterator.remove()
                    break
                }
            } else {
                iterator.remove()
            }
        }

        return reusable
    }

    /**
     * 校验bitmap 是否满足条件
     */
    fun chackInBitmap(bitmap: Bitmap, w: Int, h: Int, inSampleSize: Int): Boolean {
        /**
         * 3.0~4.4 之间  规则
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return bitmap.width == w && bitmap.height == h && inSampleSize == 1
        }
        var w = w
        var h = h
        if (inSampleSize > 1) {
            w /= inSampleSize
            h /= inSampleSize
        }
        val byteCount = w * h * getBytesPerPixel(bitmap.config)
        // 图片内存             系统分配内存
        return byteCount <= bitmap.allocationByteCount
    }

    /**
     * 通过像素格式计算一个像素占用多少字节
     */
    private fun getBytesPerPixel(config: Bitmap.Config): Int {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4
        }
        return 2
    }

}