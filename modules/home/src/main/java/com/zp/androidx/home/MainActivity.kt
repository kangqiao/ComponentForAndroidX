package com.zp.androidx.home

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.component.RouterExtras
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.home.ui.HomeFragment
import com.zp.androidx.net.RetrofitHelper
import timber.log.Timber

@Deprecated("测试, 做模块独立运行入口.")
@Route(path = RouterConfig.Home.MAIN, name = "", extras = RouterExtras.FLAG_LOGIN)
class MainActivity : BaseActivity() {

    companion object {
        fun open() {
            ARouter.getInstance().build(RouterConfig.TEST.MAIN).navigation()
        }
    }

    //val homeApi by lazy { RetrofitHelper.createService(HomeApi::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSwipeBackEnable(false)
        Timber.d("进入Test")

        setContentView(R.layout.base_root_container)

        if (null == findFragment(HomeFragment::class.java)){
            loadRootFragment(R.id.root_container, HomeFragment.newInstance())
        }

        /*constraintLayout {
            val tvMain = textView("Test模块首页Main") {
                id = View.generateViewId()
                textSize = 16f
                textColorResource = R.color.base_text_green
            }.lparams(wrapContent, wrapContent) {
            }

            button("加载数据") {
                onClick {
                    homeApi.getArticles(1)
                        .compose(RxUtil.applySchedulersToObservable())
                        .subscribe({
                            it.data.datas
                            toast("加载成功!!!")
                        }, {
                            snackBarToast(this@button, "加载失败!!!")
                            Timber.d(it)
                        })
                }
            }.lparams(matchParent, wrapContent) {
                topToBottom = tvMain.id
            }
        }*/
    }
}
