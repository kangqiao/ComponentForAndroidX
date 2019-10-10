package com.zp.androidx.base.arch.mvp

/**
 * Created by zhaopan on 2018/10/11.
 */

interface BaseView<out T : BasePresenter<*>> {

    fun showError(error: Throwable) {}

    val presenter: T
}