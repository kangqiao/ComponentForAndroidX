package com.zp.androidx.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alibaba.android.arouter.launcher.ARouter
import com.zp.androidx.component.RouterConfig
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_web.setOnClickListener {
            ARouter.getInstance().build(RouterConfig.Base.WEB)
                .withString(RouterConfig.Base.Param.KEY_URL, "https://www.baidu.com")
                .withString(RouterConfig.Base.Param.KEY_TITLE, "zhaopan")
                .navigation()
        }
    }
}
