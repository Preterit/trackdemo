package com.xiangxue.trackdemo.util;

import android.graphics.Bitmap;
import android.os.Environment;

import com.xiangxue.trackdemo.BuildConfig;
import com.xiangxue.trackdemo.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Date:2020-02-04
 * author:lwb
 * Desc:
 */
public class AA {


    public void sss() {


        try {
            DiskLruCache.open(new File(""), BuildConfig.VERSION_CODE, 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }


        String.valueOf(1);

        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();


        Set<WeakReference<Bitmap>> pool = Collections.synchronizedSet(new HashSet<WeakReference<Bitmap>>());
        Iterator<WeakReference<Bitmap>> iterator = pool.iterator();
        iterator.remove();


    }

}
