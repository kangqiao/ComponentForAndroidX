package com.zp.androidx.project

import android.app.Application
import com.zp.androidx.base.ModuleApp
import com.zp.androidx.base.utils.RxUtil
import com.zp.androidx.base.utils.SPSingleton
import org.koin.core.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.inject

/**
 * Created by zhaopan on 2018/11/07.
 */

class ProjectApp : ModuleApp(), KoinComponent {

    private val serverApi: ServerAPI by inject()
    private val spStorage: SPSingleton by inject()

    override fun onCreate() {
        super.onCreate()
        initModuleApp(this)
        initModuleData(this)
    }

    override fun initModuleApp(application: Application) {
        loadKoinModules(moduleList)
    }

    override fun initModuleData(application: Application) {
        serverApi.getProjectTree()
            .compose(RxUtil.applySchedulersToObservable())
            .subscribe({
                if(it.isSuccess()) {

                } else {
                    //view?.setCategoryList(null)
                }
            }, { error ->
                //view?.showError(error)
            })
    }
}
