package com.zp.androidx.home.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import cn.bingoogolapple.bgabanner.BGABanner
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.zp.androidx.base.arch.BaseFragment
import com.zp.androidx.base.arch.mvvm.*
import com.zp.androidx.base.common.DataBindingQuickAdapter
import com.zp.androidx.base.common.DataBindingViewHolder
import com.zp.androidx.base.ui.WebActivity
import com.zp.androidx.base.utils.CtxUtil
import com.zp.androidx.base.utils.CtxUtil.showToast
import com.zp.androidx.common.snackBarToast
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.component.ServiceManager
import com.zp.androidx.component.service.BackResult
import com.zp.androidx.component.service.HandleCallBack
import com.zp.androidx.home.BR
import com.zp.androidx.home.Article
import com.zp.androidx.home.ArticleResponseBody
import com.zp.androidx.home.BannerItem
import com.zp.androidx.lib.statusview.*
import com.zp.androidx.net.NetUtils
import kotlinx.android.synthetic.main.home_fragment_main.*
import me.yokeyword.fragmentation.SupportFragment
import timber.log.Timber
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by zhaopan on 2018/10/17.
 */

@Route(path = RouterConfig.Home.HOME, name = "Home模块首页入口")
class HomeFragment : BaseFragment() {

    companion object {
        const val TAG: String = "HomeFragment"
        fun newInstance(): SupportFragment {
            return HomeFragment().apply {
                arguments = Bundle()
            }
        }
    }

    private val viewModel by viewModel<HomeViewModel>()
    private lateinit var statusView: StatusView
    val bannerView: BGABanner by lazy { initAndAddBannerView() }
    private lateinit var adapter: DataBindingQuickAdapter<Article>
    private var isRefresh = false
    private var bannerList: List<BannerItem>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment_main, container, false)
    }

    override fun initView(view: View) {
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE)
        swipeRefreshLayout.setOnRefreshListener {
            isRefresh = true
            adapter.setEnableLoadMore(false)
            requestHomeData(isRefresh, 0)
        }

        recyclerView.apply {
            //layoutManager = LinearLayoutManager(_mActivity)
            addItemDecoration(DividerItemDecoration(_mActivity, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(_mActivity, R.drawable.base_divider_line)?.let { setDrawable(it) }
            })
        }.adapter = object : DataBindingQuickAdapter<Article>(R.layout.home_item_home) {
            override fun convert(holder: DataBindingViewHolder, item: Article) {
                holder.bindTo(BR.item, item)
            }
        }.apply {
            adapter = this
            setOnItemClickListener { adapter, view, position ->
                (adapter.getItem(position) as? Article)?.run {
                    // 打开知识体系
                    WebActivity.open(link, title, id)
                }
            }
            setOnItemChildClickListener { adapter, view, position ->
                (adapter.getItem(position) as? Article)?.run {
                    if(view.id == R.id.iv_like) {
                        if (ServiceManager.getUserService().isLogin()) {
                            if (!NetUtils.isNetworkAvailable(CtxUtil.context())) {
                                snackBarToast(recyclerView, CtxUtil.getString(R.string.no_network))
                                return@setOnItemChildClickListener
                            }
                            val collect = !this.collect
                            ServiceManager.getUserService()
                                .collectOrCancelArticle(this.id, collect, object : HandleCallBack<String> {
                                    override fun onResult(result: BackResult<String>) {
                                        if (result.isOk()) {
                                            this@run.collect = collect
                                            adapter.setData(position, this@run) //刷新当前ItemView.
                                        }
                                        result.data?.let { CtxUtil.showToast(it) }
                                    }
                                })
                        } else {
                            ARouter.getInstance().build(RouterConfig.User.LOGIN).navigation()
                            CtxUtil.showToast(R.string.login_tint)
                        }
                    }
                }
            }

            setOnLoadMoreListener({
                isRefresh = false
                swipeRefreshLayout.isRefreshing = false
                val page = adapter.data.size / 20
                requestHomeData(isRefresh, page)
            }, recyclerView)
        }

        statusView = initStatusView(R.id.recyclerView)
        statusView.config(StatusViewBuilder.Builder()
            .setOnEmptyRetryClickListener {
                requestHomeData(true, 0)
            }
            .setOnErrorRetryClickListener {
                requestHomeData(true, 0)
            }
            .build())
        viewModel.events.observe(this, Observer { event ->
            when (event) {
                is LoadingEvent -> { /*显示加载中...*/
                    statusView.showLoadingView()
                }
                is SuccessEvent -> { /*加载完成.*/
                    statusView.showContentView()
                }
                is FailedEvent -> {
                    showToast(event.errorMsg)
                    statusView.showErrorView()
                }
                is ExceptionEvent -> {
                    Timber.e(event.error)
                }
            }
        })
        viewModel.articleData.observe(this, Observer {
            it?.let { updateArticleData(it) }
        })
        viewModel.bannerList.observe(this, Observer {
            it?.let {
                updateBannerList(it)
            }
        })

        requestHomeData(true, 0)
    }

    /**
     * 延时初始化BannerView
     */
    private fun initAndAddBannerView(): BGABanner {
        val bannerLayout = LayoutInflater.from(recyclerView.context).inflate(R.layout.home_banner_layout, null, false)
        val banner: BGABanner = bannerLayout.find(R.id.banner)
        banner.run {
            setAdapter { _, itemView, model, _ ->
                if (itemView is ImageView && model is BannerItem && model.getImageUrl()?.isNotEmpty()) {
                    Glide.with(this.context)
                        .load(model.getImageUrl())
                        .into(itemView)
                }
            }
            setDelegate { _, _, _, position ->
                bannerList?.get(position)?.run {
                    WebActivity.open(url, title, id)
                }
            }
        }

        adapter.addHeaderView(bannerLayout)
        return banner
    }

    /**
     * 通过ViewModel去加载文章列表和Banner列表数据.
     */
    fun requestHomeData(isRefresh: Boolean, num: Int) {
        viewModel.getArticleData(num)
        if (isRefresh) viewModel.getBannerList()
    }

    /**
     * 更新Banner数据.
     */
    fun updateBannerList(list: List<BannerItem>) {
        bannerList = list
        bannerView.run {
            setAutoPlayAble(list.size > 1)
            setData(list, list.map { it.title })
        }
    }

    /**
     * 更新Home页文章数据.
     */
    fun updateArticleData(articles: ArticleResponseBody) {
        if (isRefresh) {
            swipeRefreshLayout.isRefreshing = false
            adapter.setEnableLoadMore(true)
        }
        articles.datas?.let {
            adapter.run {
                if (isRefresh) {
                    replaceData(it)
                } else {
                    addData(it)
                }
                val size = it.size
                if (size < articles.size) {
                    loadMoreEnd(isRefresh)
                } else {
                    loadMoreComplete()
                }
            }
        }
    }


}
