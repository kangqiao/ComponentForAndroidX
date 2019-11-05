package com.zp.androidx.knowledge

import com.zp.androidx.knowledge.ui.ViewModel
import com.zp.androidx.net.RetrofitHelper
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Created by zhaopan on 2018/10/28.
 */

val moduleList = module(createdAtStart = true) {
    // ViewModel for ViewModel
    viewModel { ViewModel(get()) }

    // HomeApi 网络请求
    single { RetrofitHelper.createService(ServerAPI::class.java) }

}