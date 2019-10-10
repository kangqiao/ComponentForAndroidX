package com.zp.androidx.common

import androidx.fragment.app.FragmentActivity


/**
 * Created by zhaopan on 2018/10/10.
 */

fun <T : Any> FragmentActivity.argument(key: String) =
        lazy { intent.extras?.get(key) as? T ?: error("Intent Argument $key is missing") }

fun <T : Any> FragmentActivity.argument(key: String, def: T) = intent.extras?.get(key) as? T ?: def