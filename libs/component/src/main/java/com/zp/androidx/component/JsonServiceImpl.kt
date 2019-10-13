package com.zp.androidx.component

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.service.SerializationService
import java.lang.reflect.Type
import com.squareup.moshi.Moshi

/**
 * Created by zhaopan on 2019/1/6.
 */

//@Route(path = RouterConfig.Service.JSON, name = "SerializationService")
class JsonServiceImpl : SerializationService {

    private lateinit var moshi: Moshi

    override fun init(context: Context?) {
        moshi = Moshi.Builder().build()
    }

    override fun <T : Any?> json2Object(input: String, clazz: Class<T>): T? {
        val adapter = moshi.adapter<T>(clazz)
        return adapter.fromJson(input)
    }

    override fun object2Json(instance: Any): String {
        val adapter = moshi.adapter(instance.javaClass)
        return adapter.toJson(instance)
    }

    override fun <T : Any?> parseObject(input: String, clazz: Type): T? {
        val adapter = moshi.adapter<T>(clazz)
        return adapter.fromJson(input)
    }

}