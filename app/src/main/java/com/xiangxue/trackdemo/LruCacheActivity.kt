package com.xiangxue.trackdemo

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.xiangxue.trackdemo.adapter.LruAdapter
import com.xiangxue.trackdemo.util.ImageCache
import kotlinx.android.synthetic.main.activity_lru_cache.*

class LruCacheActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lru_cache)

        recyclerView.layoutManager = LinearLayoutManager(this)
        var adapter = LruAdapter()
        recyclerView.adapter = adapter


        ImageCache.getInstance()?.init(this,Environment.getExternalStorageDirectory().toString()+"/bitmap2")

//        adapter.setOnItemChildClickListener { baseQuickAdapter, view, i ->
//            pickFile()
//        }
    }

    // 打开系统的文件选择器
    fun pickFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        this.startActivityForResult(intent, 1)
    }

}
