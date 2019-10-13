package com.zp.androidx.project

import com.zp.androidx.base.BaseApp
import com.zp.androidx.base.utils.SPStorage
import com.zp.androidx.net.RetrofitHelper
import com.zp.androidx.project.ui.CategoryTabContract
import com.zp.androidx.project.ui.CategoryTabPresenter
import com.zp.androidx.project.ui.ProjectListContract
import com.zp.androidx.project.ui.ProjectListPresenter
import fr.ekito.myweatherapp.util.coroutines.ApplicationSchedulerProvider
import fr.ekito.myweatherapp.util.coroutines.SchedulerProvider
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Created by zhaopan on 2018/11/7.
 */

@JvmField
val moduleList = module(createdAtStart = true) {
    // Presenter for category list
    factory<CategoryTabContract.Presenter> { CategoryTabPresenter(get()) }
    // Presenter for project list
    factory<ProjectListContract.Presenter> { ProjectListPresenter() }


    // Rx Schedulers
    single<SchedulerProvider>(createdAtStart = true) { ApplicationSchedulerProvider() }
    // provided serverAPI
    single { RetrofitHelper.createService(ServerAPI::class.java) }
    // User 模块专属ShardPreferences配置.
    single(qualifier = named("Project")) { SPStorage(BaseApp.application, "Project") }
}