package com.zp.androidx.user.ui

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.base.utils.CtxUtil
import com.zp.androidx.base.arch.mvvm.ExceptionEvent
import com.zp.androidx.base.arch.mvvm.FailedEvent
import com.zp.androidx.base.arch.mvvm.LoadingEvent
import com.zp.androidx.base.arch.mvvm.SuccessEvent
import com.zp.androidx.base.common.DBViewHolder
import com.zp.androidx.base.ui.WebActivity
import com.zp.androidx.base.utils.CtxUtil.showToast
import com.zp.androidx.common.snackBarToast
import com.zp.androidx.common.widget.SpaceItemDecoration
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.component.ServiceManager
import com.zp.androidx.component.service.BackResult
import com.zp.androidx.component.service.HandleCallBack
import com.zp.androidx.net.NetUtils
import com.zp.androidx.user.CollectionArticle
import com.zp.androidx.user.R
import com.zp.androidx.user.BR
import com.zp.androidx.user.CollectionResponseBody
import kotlinx.android.synthetic.main.user_activity_refresh_layout.*
import timber.log.Timber
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Created by zhaopan on 2018/11/9.
 */

@Route(path = RouterConfig.User.COLLECT_LIST, name = "收藏列表页面")
class CollectListActivity: BaseActivity() {

    companion object {
        const val TAG = "CollectListActivity"
        fun open() {
            ARouter.getInstance().build(RouterConfig.User.COLLECT_LIST).navigation()
        }
    }

    private val vm by viewModel<CollectViewModel>()
    private lateinit var adapter: BaseQuickAdapter<CollectionArticle, DBViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_refresh_layout)

        initView()

        requestCollectList(true, 0)
    }

    private fun initView() {
        toolbar.run {
            title = getString(R.string.nav_my_collect)
            setSupportActionBar(this)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            setNavigationOnClickListener{ finish() }
        }

        swipeRefreshLayout.run {
            isRefreshing = true
            setOnRefreshListener {
                adapter.setEnableLoadMore(false)
                requestCollectList(true, 0)
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CollectListActivity)
            addItemDecoration(SpaceItemDecoration(this@CollectListActivity))
        }.adapter = object : BaseQuickAdapter<CollectionArticle, DBViewHolder>(R.layout.user_item_collect_article) {
            override fun convert(holder: DBViewHolder, item: CollectionArticle) {
                holder.bindTo(BR.item, item)
                holder.addOnClickListener(R.id.iv_like)
            }
        }.apply {
            adapter = this

            setOnItemClickListener { adapter, view, position ->
                (adapter.getItem(position) as? CollectionArticle)?.run {
                    // 打开某知识信息页面
                    WebActivity.open(link, title, id)
                }
            }

            setOnItemChildClickListener { adapter, view, position ->
                (adapter.getItem(position) as? CollectionArticle)?.run {
                    if(view.id == R.id.iv_like) {
                        if (ServiceManager.getUserService().isLogin()) {
                            if (!NetUtils.isNetworkAvailable(CtxUtil.context())) {
                                snackBarToast(recyclerView, CtxUtil.getString(R.string.no_network))
                                return@setOnItemChildClickListener
                            }
                            ServiceManager.getUserService()
                                .collectOrCancelArticle(this.id, false, object : HandleCallBack<String> {
                                    override fun onResult(result: BackResult<String>) {
                                        if (result.isOk()) {
                                            adapter.remove(position)
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

        }

        vm.run {
            events.observe(this@CollectListActivity, Observer { event ->
                when (event) {
                    is LoadingEvent -> { /*显示加载中...*/
                    }
                    is SuccessEvent -> { /*加载完成.*/
                    }
                    is FailedEvent -> {
                        showToast(event.errorMsg)
                    }
                    is ExceptionEvent -> {
                        Timber.e(event.error)
                    }
                }
            })

            collectBody.observe(this@CollectListActivity, Observer {
                it?.run {
                    updateCollectList(it)
                }
            })
        }
    }

    fun requestCollectList(isRefresh: Boolean, num: Int) {
        vm.getCollectList(num)
    }

    fun updateCollectList(body: CollectionResponseBody<CollectionArticle>) {
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