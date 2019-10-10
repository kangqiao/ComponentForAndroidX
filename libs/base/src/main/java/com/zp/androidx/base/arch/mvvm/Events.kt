package com.zp.androidx.base.arch.mvvm

/**
 * Created by zhaopan on 2018/10/21.
 */

/**
 * Abstract Event from ViewModel
 */
open class ViewModelEvent

/**
 * Generic Loading Event
 */
object LoadingEvent : ViewModelEvent()

/**
 * Generic Success Event
 */
object SuccessEvent : ViewModelEvent()

/**
 * Generic Failed Event
 */
data class FailedEvent(val errorMsg: String) : ViewModelEvent()

/**
 * Generic Exception Event
 */
data class ExceptionEvent(val error: Throwable) : ViewModelEvent()
