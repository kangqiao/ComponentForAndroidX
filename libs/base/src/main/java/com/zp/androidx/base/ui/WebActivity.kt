package com.zp.androidx.base.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.android.material.appbar.AppBarLayout
import com.just.agentweb.AgentWeb
import com.just.agentweb.DefaultWebClient
import com.just.agentweb.NestedScrollAgentWebView
import com.zp.androidx.base.R
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.component.RouterConfig
import kotlinx.android.synthetic.main.base_activity_web.*
import kotlinx.android.synthetic.main.base_toolbar.*


/**
 * Created by zhaopan on 2018/10/21.
 */

@Route(path = RouterConfig.Base.WEB, name = "公共的Web页面")
class WebActivity : BaseActivity() {

    companion object {
        @JvmOverloads
        @JvmStatic
        fun open(context: Context, url: String, title: String, id: Int = 0, isShowTitle: Boolean = true) {
            context.startActivity(Intent(context, WebActivity::class.java).apply {
                putExtra(RouterConfig.Base.Param.KEY_ID, id)
                putExtra(RouterConfig.Base.Param.KEY_URL, url)
                putExtra(RouterConfig.Base.Param.KEY_TITLE, title)
                putExtra(RouterConfig.Base.Param.KEY_IS_SHOW_TITLE, isShowTitle)
            })
        }

        @JvmOverloads
        @JvmStatic
        fun open(url: String, title: String, id: Int = 0, isShowTitle: Boolean = true) {
            ARouter.getInstance().build(RouterConfig.Base.WEB)
                .withInt(RouterConfig.Base.Param.KEY_ID, id)
                .withString(RouterConfig.Base.Param.KEY_URL, url)
                .withString(RouterConfig.Base.Param.KEY_TITLE, title)
                .withBoolean(RouterConfig.Base.Param.KEY_IS_SHOW_TITLE, isShowTitle)
                .navigation()
        }
    }

    @Autowired(name = RouterConfig.Base.Param.KEY_ID)
    @JvmField var id = 0
    @Autowired(name = RouterConfig.Base.Param.KEY_TITLE)
    @JvmField var title = ""
    @Autowired(name = RouterConfig.Base.Param.KEY_URL)
    @JvmField var url = ""
    @Autowired(name = RouterConfig.Base.Param.KEY_IS_SHOW_TITLE)
    @JvmField var isShowTitle = true

    private lateinit var agentWeb: AgentWeb
    private lateinit var errorMsg: TextView
    private val mWebView: NestedScrollAgentWebView by lazy {
        NestedScrollAgentWebView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        setContentView(R.layout.base_activity_web)

        toolbar.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        iv_close.apply {
            visibility = View.VISIBLE
            setOnClickListener { finish() }
        }
        tv_title.apply {
            visibility = View.VISIBLE
            text = getString(R.string.agentweb_loading)
            isSelected = true
        }
        initWebView()
    }

    private fun initWebView() {
        val layoutParams = CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layoutParams.behavior = AppBarLayout.ScrollingViewBehavior()
        val errorView = layoutInflater.inflate(R.layout.base_web_error_page, null)
        errorMsg = errorView.findViewById(R.id.error_msg)
        errorMsg.text = getString(R.string.base_web_page_err, url)

        agentWeb = AgentWeb.with(this)
            .setAgentWebParent(web_container, 1, layoutParams)
            .useDefaultIndicator()
            .setWebView(mWebView)
            .setWebChromeClient(webChromeClient)
            .setMainFrameErrorView(errorView)
            .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)
            .createAgentWeb()
            .ready()
            .go(url)

        agentWeb?.webCreator?.webView?.run {
            settings.domStorageEnabled = true
            webViewClient = mWebViewClient
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }
    }


    override fun onBackPressedSupport() {
        agentWeb?.run {
            if (!back()) {
                super.onBackPressedSupport()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (agentWeb?.handleKeyEvent(keyCode, event)!!) {
            true
        } else {
            finish()
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onResume() {
        agentWeb?.webLifeCycle?.onResume()
        super.onResume()
    }

    override fun onPause() {
        agentWeb?.webLifeCycle?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        agentWeb?.webLifeCycle?.onDestroy()
        super.onDestroy()
    }

    private val webChromeClient = object : com.just.agentweb.WebChromeClient() {
        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            tv_title.text = title
        }
    }

    private val mWebViewClient = object : com.just.agentweb.WebViewClient() {
    }

}