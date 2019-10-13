package com.zp.androidx.test

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.component.RouterExtras
import timber.log.Timber

@Deprecated("测试, 做模块独立运行入口.")
@Route(path = RouterConfig.TEST.MAIN, name = "Test首页", extras = RouterExtras.FLAG_LOGIN)
class MainActivity : BaseActivity() {

    companion object {
        fun open(){
            ARouter.getInstance().build(RouterConfig.TEST.MAIN).navigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSwipeBackEnable(false)
        Timber.d("进入Test")

    }
}
