package com.xiangxue.trackdemo.util;

import android.graphics.Bitmap;

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
