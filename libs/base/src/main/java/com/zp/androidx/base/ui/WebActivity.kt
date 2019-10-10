package com.zp.androidx.base.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.webkit.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.just.agentweb.AgentWeb
import com.just.agentweb.DefaultWebClient
import com.just.agentweb.NestedScrollAgentWebView
import com.zp.androidx.base.R
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.component.RouterConfig
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.design.themedAppBarLayout

/**
 * Created by zhaopan on 2018/10/21.
 */
@Route(path = RouterConfig.Base.WEB, name = "公共的Web页面")
class WebActivity : BaseActivity() {

    companion object {
        @Deprecated("采用ARouter形式, 方便管理.")
        @JvmOverloads
        @JvmStatic
        fun open(
            context: Context,
            url: String,
            title: String,
            id: Int = 0,
            isShowTitle: Boolean = true
        ) {
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
    @JvmField
    var id = 0
    @Autowired(name = RouterConfig.Base.Param.KEY_TITLE)
    @JvmField
    var title = ""
    @Autowired(name = RouterConfig.Base.Param.KEY_URL)
    @JvmField
    var url = ""
    @Autowired(name = RouterConfig.Base.Param.KEY_IS_SHOW_TITLE)
    @JvmField
    var isShowTitle = true

    private val ui: WebActivityUI by lazy { WebActivityUI() }
    private lateinit var agentWeb: AgentWeb

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        id = argument(RouterConfig.Base.Param.KEY_ID, id)
        url = argument(RouterConfig.Base.Param.KEY_URL, url)
        title = argument(RouterConfig.Base.Param.KEY_TITLE, title)
        isShowTitle = argument(RouterConfig.Base.Param.KEY_IS_SHOW_TITLE, isShowTitle)
        ui.setContentView(this)

        ui.toolbar.run {
            //setTitle(R.string.loading)
            setSupportActionBar(ui.toolbar)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setNavigationOnClickListener { finish() }
        }
        ui.tvTitle.apply {
            setText(R.string.agentweb_loading)
            visible()
            postDelayed({
                ui.tvTitle.isSelected = true
            }, 2000)
        }

        val layoutParams = CoordinatorLayout.LayoutParams(-1, -1)
        layoutParams.behavior = AppBarLayout.ScrollingViewBehavior()

        agentWeb = AgentWeb.with(this)//传入Activity or Fragment
            .setAgentWebParent(ui.webContainer, -1, layoutParams)//传入AgentWeb 的父控件
            .useDefaultIndicator()// 使用默认进度条
            .setWebView(NestedScrollAgentWebView(this))
            .setWebChromeClient(webChromeClient)
            .setWebViewClient(webViewClient)
            .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
            .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
            .createAgentWeb()
            .ready()
            .go(url)

        agentWeb?.webCreator?.webView?.let {
            it.settings.domStorageEnabled = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.base_menu_webpage, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share -> {
                Intent().run {
                    action = Intent.ACTION_SEND
                }
                return true
            }
            R.id.action_like -> {

                return true
            }
            R.id.action_browser -> {
                Intent().run {
                    action = "android.intent.action.VIEW"
                    data = Uri.parse(url)
                    startActivity(this)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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

    /**
     * webViewClient
     */
    private val webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            // super.onReceivedSslError(view, handler, error)
            handler?.proceed()
        }
    }

    /**
     * webChromeClient
     */
    private val webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            title.let {
                // toolbar.title = it
                ui.tvTitle.text = it
            }
        }
    }
}

class WebActivityUI : AnkoComponent<WebActivity> {
    lateinit var rootView: ConstraintLayout
    lateinit var appBarLayout: AppBarLayout
    lateinit var toolbar: Toolbar
    lateinit var tvTitle: TextView
    lateinit var webContainer: FrameLayout

    override fun createView(ui: AnkoContext<WebActivity>) = with(ui) {
        constraintLayout {
            id = View.generateViewId()
            this@WebActivityUI.rootView = this
            backgroundColorResource = R.color.base_bg_white

            appBarLayout = themedAppBarLayout(R.style.base_AppTheme_AppBarOverlay) {
                id = View.generateViewId()
                fitsSystemWindows = true
                elevation = dip(0).toFloat()

                toolbar = toolbarV7 {
                    id = View.generateViewId()
                    fitsSystemWindows = true
                    popupTheme = R.style.base_AppTheme_PopupOverlay

                    tvTitle = textView {
                        ellipsize = TextUtils.TruncateAt.MARQUEE
                        singleLine = true
                        textColorResource = R.color.white
                        textSize = 18f
                        gone()
                    }.lparams(matchParent, wrapContent)
                }.lparams(matchParent, attrDimen(android.R.attr.actionBarSize)) {
                    scrollFlags = SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS
                }
            }.lparams(matchParent, wrapContent) {
            }

            webContainer = frameLayout {
                id = View.generateViewId()
                fitsSystemWindows = true
                backgroundColorResource = R.color.base_bg_white
            }.lparams(matchParent, matchParent) {
                topMargin = attrDimen(android.R.attr.actionBarSize)
                //topToBottom = appBarLayout.id
            }

            /*nestedScrollAgentWebView {

            }.lparams(matchParent, matchParent){

            }*/
        }
    }

}