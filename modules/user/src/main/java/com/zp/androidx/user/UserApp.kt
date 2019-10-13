package com.zp.androidx.user

import android.app.Application
import com.zp.androidx.base.ModuleApp
import com.zp.androidx.component.ServiceManager
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules

/**
 * Created by zhaopan on 2018/11/07.
 */

class UserApp : ModuleApp() {

    // 通过Koin单例注入userService, 并在initModuleData中手动设置到ServiceFactory中.
    private val userService: UserService by inject()

    override fun onCreate() {
        super.onCreate()
        initModuleApp(this)
        initModuleData(this)
        //todo 独立运行时, 测试环境设置. 此处可设置测试网络.
    }

    override fun initModuleApp(application: Application) {
        loadKoinModules(moduleList)
    }

    override fun initModuleData(application: Application) {
        ServiceManager.userService1 = userService
    }
}
