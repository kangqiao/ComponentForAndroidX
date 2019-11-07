package com.zp.androidx.app.ui

import android.os.Bundle
import com.alibaba.android.arouter.launcher.ARouter
import com.zp.androidx.app.R
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.base.utils.CtxUtil
import com.zp.androidx.base.utils.Zlog
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.security.SecurityManager

/**
 * Created by zhaopan on 2019-10-15.
 */

class SplashActivity: BaseActivity() {
    companion object {
        const val TAG: String = "SplashActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (SecurityManager.checkSignature()) {
            Zlog.d(TAG, "SecurityManager.getApiSignId=" + SecurityManager.getApiSignId())
            CtxUtil.showToast("签名校验成功!")
            ARouter.getInstance().build(RouterConfig.APP.MAIN).navigation()
        } else {
            Zlog.d(TAG, "SecurityManager.getApiSignKey=" + SecurityManager.getApiSignKey())
            CtxUtil.showToast("签名校验失败!!!")
        }
    }
}