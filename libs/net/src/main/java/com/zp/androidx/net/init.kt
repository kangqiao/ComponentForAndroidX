package com.zp.androidx.net

import android.app.Application

/**
 * Created by zhaopan on 2018/10/20.
 */

lateinit var appContext: Application
private var netSignKey: String? = null
val BASE_URL: String by lazy { NetUtils.getString(R.string.api_host) }

fun initNetConfig(application: Application, signKey: String? = null) {
    appContext = application
    netSignKey = signKey
}