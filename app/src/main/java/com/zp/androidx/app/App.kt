package com.zp.androidx.app

import android.util.Log
import com.zp.androidx.base.MainApp

/**
 * Created by zhaopan on 2018/10/10.
 */

class APP : MainApp() {
    companion object {
        const val TAG = "APP"
    }

    override fun onCreate() {
        super.onCreate()
        //com.zp.androidx.api.initOkGoRequestApi(this/*, net.idik.lib.cipher.so.CipherClient.signkey()*/)
        Log.e(TAG, BuildConfig.APPLICATION_ID)
//        Log.e(TAG, com.zp.androidx.home.BuildConfig.APPLICATION_ID)
//        Log.e(TAG, com.zp.androidx.user.BuildConfig.APPLICATION_ID)
//        Log.e(TAG, com.zp.androidx.knowledge.BuildConfig.APPLICATION_ID)
//        Log.e(TAG, com.zp.androidx.project.BuildConfig.APPLICATION_ID)
    }
}