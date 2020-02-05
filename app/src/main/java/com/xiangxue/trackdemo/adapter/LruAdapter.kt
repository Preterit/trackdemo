package com.xiangxue.trackdemo.adapter

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
class LruAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_lru) {

    init {
        var list = arrayListOf<String>()
        for (index in 1..10) {
            list.add("$index")
        }
        replaceData(list)
    }

    override fun convert(helper: BaseViewHolder, item: String?) {

//        var bitmap:Bitmap = BitmapFactory.decodeResource(mContext.resources,R.drawable.tiger)

        //优化
        var bitmap =
            ImageCache.getInstance()?.getBitmapFromMemory(helper.layoutPosition.toString())
        if (null == bitmap) {
            val reusable = ImageCache.getInstance()?.getReusable(60, 60, 1)
            bitmap =
                ImageReSize.reSizeBitmap(mContext, R.drawable.tiger, 80, 80, true, reusable)
        }

        helper.setImageBitmap(R.id.imageView, bitmap)
        helper.addOnClickListener(R.id.imageView)
    }

}