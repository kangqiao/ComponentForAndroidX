package com.zp.androidx.knowledge.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.zp.androidx.base.arch.BaseFragment
import com.zp.androidx.base.utils.CtxUtil
import com.zp.androidx.base.ui.WebActivity
import com.zp.androidx.base.arch.mvvm.ExceptionEvent
import com.zp.androidx.base.arch.mvvm.FailedEvent
import com.zp.androidx.base.arch.mvvm.LoadingEvent
import com.zp.androidx.base.arch.mvvm.SuccessEvent
import com.zp.androidx.base.common.DBViewHolder
import com.zp.androidx.base.utils.CtxUtil.showToast
import com.zp.androidx.common.snackBarToast
import com.zp.androidx.common.widget.SpaceItemDecoration
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.component.ServiceManager
import com.zp.androidx.component.service.BackResult
import com.zp.androidx.component.service.HandleCallBack
import com.zp.androidx.knowledge.Article
import com.zp.androidx.knowledge.ArticleResponseBody
import com.zp.androidx.knowledge.BR
import com.zp.androidx.knowledge.R
import com.zp.androidx.net.NetUtils
import kotlinx.android.synthetic.main.knowledge_fragment_refresh_layout.*
import me.yokeyword.fragmentation.SupportFragment
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * Created by zhaopan on 2018/10/30.
 */

@Route(path = RouterConfig.Knowledge.DETAIL, name = "某知识详细信息")
class DetailFragment : BaseFragment() {

    companion object {
        const val CONTENT_CID_KEY = RouterConfig.Knowledge.PARAM.DETAIL_CID
        fun newInstance(cid: Int): SupportFragment {
            return DetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(CONTENT_CID_KEY, cid)
                }
            }
        }

        fun getInstance(cid: Int): SupportFragment {
            return ARouter.getInstance().build(RouterConfig.Knowledge.DETAIL)
                .withInt(CONTENT_CID_KEY, cid)
                .navigation() as SupportFragment
        }
    }

    @Autowired(name = CONTENT_CID_KEY)
    @JvmField var cid: Int = -1

    private val viewModel by viewModel<ViewModel>()
    private lateinit var adapter: BaseQuickAdapter<Article, DBViewHolder>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ARouter.getInstance().inject(this)
        return inflater.inflate(R.layout.knowledge_fragment_refresh_layout, container, false)
    }

    override fun initView(view: View) {
        statusView.setOnErrorViewConvertListener {
            it.setOnClickListener(R.id.sv_error_retry) {
                requestArticleData(0)
            }
        }

        swipeRefreshLayout.run {
            setRefreshing(false) //false隐藏刷新进度条
            setOnRefreshListener {
                adapter.setEnableLoadMore(false)
                requestArticleData(0)
            }
        }

        adapter = object : BaseQuickAdapter<Article, DBViewHolder>(R.layout.knowledge_item_detail) {
            override fun convert(holder: DBViewHolder, item: Article) {
                holder.bindTo(BR.item, item)
                holder.addOnClickListener(R.id.iv_like)
            }
        }

        recyclerView.apply {
            //layoutManager = LinearLayoutManager(_mActivity)
            addItemDecoration(SpaceItemDecoration(_mActivity))
            itemAnimator = DefaultItemAnimator()
        }.adapter = adapter.apply {
            setOnItemClickListener { adapter, view, position ->
                (adapter.getItem(position) as? Article)?.run {
                    // 打开某知识信息页面
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
                swipeRefreshLayout.isRefreshing = false
                val page = adapter.data.size / 20
                requestArticleData(page)
            }, recyclerView)

            setEmptyView(R.layout.fragment_view_empty)
        }

        viewModel.run {
            events.observe(this@DetailFragment, Observer { event ->
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
                        statusView.showErrorView()
                    }
                }
            })

            articleData.observe(this@DetailFragment, Observer {
                it?.run {
                    updateArticleData(it)
                }
            })
        }

        requestArticleData( 0)
    }

    fun requestArticleData(num: Int) {
        viewModel.requestArticleData(num, cid)
    }

    fun updateArticleData(body: ArticleResponseBody) {
        body.datas?.let {
            adapter.run {
                if (swipeRefreshLayout.isRefreshing) {
                    replaceData(it)
                } else {
                    addData(it)
                }
                if (it.size < body.size) { //如果返回数据小于每页总数, 说明没有新数据了.
                    loadMoreEnd(swipeRefreshLayout.isRefreshing)
                } else { //还有新数据
                    loadMoreComplete()
                }
            }
        }
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
            adapter.setEnableLoadMore(true)
        }
    }
}