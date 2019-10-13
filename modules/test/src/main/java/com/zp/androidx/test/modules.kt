package com.zp.androidx.test

import com.zp.androidx.net.RetrofitHelper
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Created by zhaopan on 2018/11/7.
 */

val moduleList = module(createdAtStart = true) {
    // ViewModel for ViewModel
    viewModel { ViewModel(get()) }

    // Test 网络请求
    single<ServerAPI> { RetrofitHelper.createService(ServerAPI::class.java) }

}
