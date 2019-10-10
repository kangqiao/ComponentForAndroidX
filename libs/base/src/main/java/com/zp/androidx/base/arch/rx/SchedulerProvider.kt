package com.zp.androidx.base.arch.rx

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Created by zhaopan on 2018/10/21.
 * Rx Scheduler Provider
 */
interface SchedulerProvider {
    fun ui(): CoroutineDispatcher
}