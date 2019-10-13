package com.zp.androidx.knowledge

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.component.RouterExtras
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.knowledge.ui.MainFragment

/**
 * Created by zhaopan on 2018/10/28.
 */

@Deprecated("测试, 做模块独立运行入口.")
@Route(path = RouterConfig.Knowledge.MAIN, name = "", extras = RouterExtras.FLAG_LOGIN)
class MainActivity : BaseActivity() {

    companion object {
        fun open(){
            ARouter.getInstance().build(RouterConfig.Knowledge.MAIN).navigation()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSwipeBackEnable(false)
        setContentView(R.layout.base_root_container)

        if (null == findFragment(MainFragment::class.java)){
            loadRootFragment(R.id.root_container, MainFragment.newInstance())
        }
    }
}