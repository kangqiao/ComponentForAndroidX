package com.zp.androidx.user

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.component.RouterExtras
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.user.ui.CollectListActivity
import com.zp.androidx.user.ui.LoginActivity
import com.zp.androidx.user.ui.RegisterActivity
import kotlinx.android.synthetic.main.user_activity_main_test.*

/**
 * Created by zhaopan on 2018/11/7.
 */

@Deprecated("测试, 做模块独立运行入口.")
@Route(path = RouterConfig.User.MAIN, name = "User首页", extras = RouterExtras.FLAG_LOGIN)
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSwipeBackEnable(false)
        setContentView(R.layout.user_activity_main_test)
        btn_login.setOnClickListener {
            LoginActivity.open()
        }

        btn_register.setOnClickListener {
            RegisterActivity.open()
        }

        btn_collect.setOnClickListener {
            CollectListActivity.open()
        }
    }
}
