package com.xiangxue.trackdemo.util

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.util.LruCache
import com.xiangxue.trackdemo.BuildConfig
import com.xiangxue.trackdemo.disklrucache.DiskLruCache
import java.io.File
import java.io.OutputStream
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
    private var reusablePool = Collections.synchronizedSet(HashSet<WeakReference<Bitmap>>())
    private var diskLruCache: DiskLruCache? = null

    fun init(context: Context, dir: String) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // 12000*5
//        val cacheSize = am.memoryClass / 8 * 1024 * 1024
        val cacheSize: Int = (Runtime.getRuntime().totalMemory() / 1024 / 8).toInt()
        lruCache = object : LruCache<String, Bitmap>(cacheSize) {
            //返回一张图片的大小
            override fun sizeOf(key: String?, value: Bitmap?): Int {
                return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    value?.allocationByteCount ?: 1
                } else value?.byteCount ?: 1
            }

            /**
             * bitmap内存回收
             */
            override fun entryRemoved(
                evicted: Boolean,
                key: String?,
                oldValue: Bitmap?,
                newValue: Bitmap?
            ) {
                if (oldValue!!.isMutable) {
                    reusablePool.add(
                        WeakReference(
                            oldValue,
                            getReferenceQueue()
                        )
                    )
                } else {
                    oldValue.recycle()
                }
            }
        }

        /**
         * 用于创建DiskLruCache
         * 参数一：表示磁盘在文件系统中存储的路径
         * 参数二：表示应用的版本号，一般设为1，当版本号发生改变时，会将之前的缓存信息清空
         * 参数三：表示单个节点所对应的数据的个数，一般设为1
         * 参数四：表示缓存的总大小，当缓存大小超过这个设定值后，DiskLruCache会清除一些缓存，从而保证不会超过这个设定值
         */
        diskLruCache =
            DiskLruCache.open(File(dir), BuildConfig.VERSION_CODE, 1, 5 * 1024 * 1024)
    }


    /**
     * 将图片放入磁盘中
     */
    fun putBitmap2Disk(key: String, bitmap: Bitmap) {
        var snapshot: DiskLruCache.Snapshot? = null
        var os: OutputStream? = null
        try {
            snapshot = diskLruCache?.get(key)
            //判断磁盘中是否有该资源
            if (snapshot == null) {
                val edit = diskLruCache?.edit(key)
                if (edit != null) {
                    os = edit.newOutputStream(0)
                    //压缩图片
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, os)
                    edit.commit()
                }
            }
        } finally {
            snapshot?.close()
            os?.close()
        }
    }

    fun getBitmapFromDIsk(key: String, reusable: Bitmap?): Bitmap? {
        var snapshot: DiskLruCache.Snapshot? = null
        var bitmap: Bitmap? = null
        try {
            snapshot = diskLruCache?.get(key)
            if (snapshot == null) {
                //磁盘中没有，直接返回
                return null
            }

            val gis = snapshot.getInputStream(0)
            val option = BitmapFactory.Options()

            option.inMutable = true
            option.inBitmap = reusable

            bitmap = BitmapFactory.decodeStream(gis, null, option)
            if (bitmap != null) {
                lruCache?.put(key, bitmap)
            }

        } finally {
            snapshot?.close()
        }

        return bitmap
    }

    private var referenceQueue: ReferenceQueue<Bitmap>? = null
    private var shutDown: Boolean = false  // 线程销毁 停止条件

    /**
     * 创建引用队列
     */
    fun getReferenceQueue(): ReferenceQueue<Bitmap> {
        if (referenceQueue == null) {
            referenceQueue = ReferenceQueue<Bitmap>()
            Thread(Runnable {
                while (!shutDown) {
                    try {
                        val remove = referenceQueue?.remove()
                        val bitmap = remove?.get()
                        if (bitmap != null && !bitmap.isRecycled) {
                            bitmap.recycle()
                        }
                    } finally {

                    }
                }
            }).start()
        }
        return referenceQueue!!
    }

    /**
     * 将bitmap放到内存中
     */
    fun putBitmap2Memory(key: String, value: Bitmap) {
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