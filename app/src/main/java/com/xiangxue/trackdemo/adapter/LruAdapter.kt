package com.xiangxue.trackdemo.adapter

import android.app.Activity
import android.util.Log
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.xiangxue.trackdemo.R
import com.xiangxue.trackdemo.util.ImageCache
import com.xiangxue.trackdemo.util.ImageReSize

/**
 * Date:2020-02-04
 * author:lwb
 * Desc:
 */
class LruAdapter(private val context: Activity) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_lru) {

    private val TAG = LruAdapter::class.java.name
    private var instance = ImageCache.getInstance()

    init {
        var list = arrayListOf<String>()
        for (index in 1..1000) {
            list.add("$index")
        }
        replaceData(list)
    }

    override fun convert(helper: BaseViewHolder, item: String?) {

//        var bitmap:Bitmap = BitmapFactory.decodeResource(mContext.resources,R.drawable.tiger)

        //优化
        var bitmap = instance?.getBitmapFromMemory(helper.layoutPosition.toString())
        Log.e(TAG, "使用内存缓存$bitmap")
        if (null == bitmap) {
            val reusable = instance?.getReusable(60, 60, 1)
            Log.e(TAG, "使用复用缓存$reusable")
            bitmap = instance?.getBitmapFromDIsk(helper.layoutPosition.toString(), reusable)
            Log.e(TAG, "使用磁盘缓存$bitmap")
            //内存磁盘都没有/网络获取
            if (bitmap == null) {
                bitmap =
                    ImageReSize.reSizeBitmap(context, R.drawable.tiger, 80, 80, true, reusable)
                //放入内存
                instance?.putBitmap2Memory(helper.layoutPosition.toString(), bitmap)
                //放入磁盘
                instance?.putBitmap2Disk(helper.layoutPosition.toString(), bitmap)
            }
        }
        helper.setImageBitmap(R.id.imageView, bitmap)
        helper.addOnClickListener(R.id.imageView)
    }

}