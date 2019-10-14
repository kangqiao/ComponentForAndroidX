package com.zp.androidx.project

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.component.RouterExtras
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.project.ui.CategoryTabFragment

@Deprecated("测试, 做模块独立运行入口.")
@Route(path = RouterConfig.Project.MAIN, name = "Project模块首页", extras = RouterExtras.FLAG_LOGIN)
class MainActivity : BaseActivity() {

    companion object {
        fun open(){
            ARouter.getInstance().build(RouterConfig.Project.MAIN).navigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSwipeBackEnable(false)

        setContentView(R.layout.base_root_container)

        if (null == findFragment(CategoryTabFragment::class.java)){
            loadRootFragment(R.id.root_container, CategoryTabFragment.getInstance())
        }
    }

    override fun onBackPressedSupport() {
        super.onBackPressedSupport()
    }
}
