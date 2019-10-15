package com.zp.androidx.app.ui

import android.os.Bundle
import com.alibaba.android.arouter.launcher.ARouter
import com.zp.androidx.app.R
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.component.RouterConfig

/**
 * Created by zhaopan on 2019-10-15.
 */

class SplashActivity: BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ARouter.getInstance().build(RouterConfig.APP.MAIN).navigation()
    }
}