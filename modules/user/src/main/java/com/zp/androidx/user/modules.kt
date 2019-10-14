package com.zp.androidx.user

import com.zp.androidx.base.BaseApp
import com.zp.androidx.base.utils.SPSingleton
import com.zp.androidx.net.RetrofitHelper
import com.zp.androidx.user.ui.CollectViewModel
import com.zp.androidx.user.ui.UserViewModel
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

/**
 * Created by zhaopan on 2018/11/7.
 */

val moduleList = module(createdAtStart = true) {
    // UserViewModel for UserViewModel
    viewModel { UserViewModel(get(), get()) }
    viewModel { CollectViewModel(get()) }

    // User 模块专属ShardPreferences配置.
    single { SPSingleton.get(BaseApp.application, "User") }

    // HomeApi 网络请求
    single { RetrofitHelper.createService(ServerAPI::class.java) }

    // UserService
    single { UserService(get(), get())}
    single { UserService2()}
}