package com.zp.androidx.base.arch.mvp

/**
 * Created by zhaopan on 2018/10/11.
 */

interface BasePresenter<V> {

    fun subscribe(view: V)

    fun unSubscribe()

    var view : V?

}