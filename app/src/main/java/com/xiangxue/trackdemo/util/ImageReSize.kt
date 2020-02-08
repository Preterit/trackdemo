package com.xiangxue.trackdemo.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Date:2020-02-05
 * author:lwb
 * Desc:
 */
object ImageReSize {

    /**
     * 压缩图片
     */
    fun reSizeBitmap(
        context: Context,
        id: Int,
        maxW: Int,
        maxH: Int,
        hasAlpha: Boolean,
        reusable: Bitmap?
    ): Bitmap {
        val resources = context.resources
        val options = BitmapFactory.Options()
        //设置位true后，再去解析，就只解析out参数
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resources, id, options)

        var w = options.outWidth
        var h = options.outHeight

        options.inSampleSize = calcuteInSampleSize(w, h, maxW, maxH)

        if (hasAlpha) {
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        options.inJustDecodeBounds = false

        //复用   inMutable 为true 表示易变
        options.inMutable = true
        options.inBitmap = reusable


        return BitmapFactory.decodeResource(resources, id, options)
    }

    /**
     * 计算缩放系数
     */
    fun calcuteInSampleSize(w: Int, h: Int, maxW: Int, maxH: Int): Int {
        var inSampleSize = 1
        if (w > maxW && h > maxH) {
            inSampleSize = 2
            while (w / inSampleSize > maxW && h / inSampleSize > maxH) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}