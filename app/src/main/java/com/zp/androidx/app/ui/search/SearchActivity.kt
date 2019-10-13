package com.zp.androidx.app.ui.search

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.zp.androidx.app.R
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.component.RouterConfig
import kotlinx.android.synthetic.main.toolbar_search.*

/**
 * Created by zhaopan on 2018/11/1.
 */

@Route(path = RouterConfig.APP.SEARCH, name = "搜索页面")
class SearchActivity : BaseActivity() {
    companion object {
        fun open() {
            ARouter.getInstance().build(RouterConfig.APP.SEARCH).navigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initView(savedInstanceState)
    }

    private fun initView(savedInstanceState: Bundle?) {
        toolbar.run {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

    }
}