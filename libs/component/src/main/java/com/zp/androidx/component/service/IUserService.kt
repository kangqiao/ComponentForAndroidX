package com.zp.androidx.component.service

import com.zp.androidx.component.BaseService

/**
 * Created by zhaopan on 2018/11/8.
 */

interface IUserService: BaseService {

    fun isLogin(): Boolean = false

    fun getUserName(): String = ""

    fun logout(callBack: HandleCallBack<Boolean>) {}

    fun collectOrCancelArticle(id: Int, isCollect: Boolean, callBack: HandleCallBack<String>) {}
}

class EmptyUserService: IUserService