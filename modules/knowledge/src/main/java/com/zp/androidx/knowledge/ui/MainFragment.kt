package com.zp.androidx.knowledge.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import com.alibaba.android.arouter.facade.annotation.Route
import com.zp.androidx.base.arch.BaseFragment
import com.zp.androidx.base.arch.mvvm.ExceptionEvent
import com.zp.androidx.base.arch.mvvm.FailedEvent
import com.zp.androidx.base.arch.mvvm.LoadingEvent
import com.zp.androidx.base.arch.mvvm.SuccessEvent
import com.zp.androidx.base.common.DataBindingQuickAdapter
import com.zp.androidx.base.common.DataBindingViewHolder
import com.zp.androidx.base.utils.CtxUtil.showToast
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.knowledge.BR
import com.zp.androidx.knowledge.KnowledgeTreeBody
import com.zp.androidx.knowledge.R
import com.zp.androidx.lib.statusview.StatusView
import com.zp.androidx.lib.statusview.StatusViewBuilder
import com.zp.androidx.lib.statusview.initStatusView
import kotlinx.android.synthetic.main.knowledge_fragment_refresh_layout.*
import me.yokeyword.fragmentation.SupportFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * Created by zhaopan on 2018/10/28.
 */

@Route(path = RouterConfig.Knowledge.HOME, name = "知识体系首页入口")
class MainFragment : BaseFragment() {

    companion object {
        const val TAG: String = "MainFragment"
        fun newInstance(): SupportFragment {
            return MainFragment().apply {
                arguments = Bundle()
            }
        }
    }

    private val viewModel by viewModel<ViewModel>()
    private lateinit var adapter: DataBindingQuickAdapter<KnowledgeTreeBody>
    private lateinit var statusView: StatusView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.knowledge_fragment_refresh_layout, container, false)
    }

    override fun initView(view: View) {
        statusView = initStatusView(R.id.recyclerView)
        statusView.config(StatusViewBuilder.Builder()
            .setOnEmptyRetryClickListener {
                viewModel.loadKnowledgeTree()
            }
            .setOnErrorRetryClickListener {
                viewModel.loadKnowledgeTree()
            }
            .build())

        swipeRefreshLayout.setOnRefreshListener {
            onSupportVisible()
        }

        recyclerView.apply {
            //layoutManager = LinearLayoutManager(_mActivity)
            addItemDecoration(DividerItemDecoration(_mActivity, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(_mActivity, R.drawable.base_divider_line)?.let { setDrawable(it) }
            })
        }.adapter = object : DataBindingQuickAdapter<KnowledgeTreeBody>(R.layout.knowledge_item_tree_list) {
            override fun convert(holder: DataBindingViewHolder, item: KnowledgeTreeBody) {
                holder.bindTo(BR.item, item)
            }
        }.apply {
            adapter = this
            setOnItemClickListener { adapter, view, position ->
                (adapter.getItem(position) as? KnowledgeTreeBody)?.run {
                    // 打开知识体系
                    DetailListActivity.open(this)
                }
            }
        }

        viewModel.run {
            events.observe(this@MainFragment, Observer { event ->
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

            knowledgeTreeList.observe(this@MainFragment, Observer {
                it?.run {
                    if (swipeRefreshLayout.isRefreshing) {
                        swipeRefreshLayout.isRefreshing = false
                    }
                    adapter.loadMoreComplete()
                    adapter.replaceData(this)
                }
            })
        }
    }

    override fun onSupportVisible() {
        viewModel.loadKnowledgeTree()
    }
}
