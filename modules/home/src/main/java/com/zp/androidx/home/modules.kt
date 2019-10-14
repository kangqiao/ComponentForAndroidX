package com.zp.androidx.home

import com.zp.androidx.home.ui.HomeViewModel
import com.zp.androidx.net.RetrofitHelper
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

/**
 * Created by zhaopan on 2018/10/21.
 */

val moduleList = module {
    // ViewModel for HomeFragment
    viewModel { HomeViewModel(get()) }

    // HomeApi 网络请求
    single<HomeApi> { RetrofitHelper.createService(HomeApi::class.java) }
}
