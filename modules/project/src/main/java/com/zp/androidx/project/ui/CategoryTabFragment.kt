package com.zp.androidx.project.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.zp.androidx.base.arch.BaseFragment
import com.zp.androidx.common.CommonFragmentStatePageAdapter
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.project.ProjectTreeBean
import com.zp.androidx.project.R
import kotlinx.android.synthetic.main.project_fragment_category_tab.*
import me.yokeyword.fragmentation.SupportFragment
import org.koin.android.ext.android.inject

/**
 * Created by zhaopan on 2018/11/17.
 */

@Route(path = RouterConfig.Project.HOME, name = "项目模块分类列表")
class CategoryTabFragment: BaseFragment(), CategoryTabContract.View {

    companion object {
        fun getInstance(): SupportFragment {
            return ARouter.getInstance().build(RouterConfig.Project.HOME).navigation() as SupportFragment
        }
    }

    override val presenter: CategoryTabContract.Presenter by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return  inflater.inflate(R.layout.project_fragment_category_tab, container, false)
    }

    override fun initView(view: View) {
        //ViewUtil.reflex(tabLayout)
        presenter.getCategoryList()
    }

    override fun onStart() {
        super.onStart()
        presenter.subscribe(this)  // Bind View
    }

    override fun onStop() {
        presenter.unSubscribe()
        super.onStop()
    }

    override fun setCategoryList(list: List<ProjectTreeBean>?) {
        if(null != list && list.isNotEmpty()) {
            val tabList = list.map { it.name }
            val fragmentList = list.map { ProjectListFragment.newInstance(it.id) }
            viewPager.adapter = CommonFragmentStatePageAdapter(fragmentManager!!, tabList, fragmentList)
            tabLayout.setupWithViewPager(viewPager)
        } else {
            //viewStub.inflate()
        }
    }
}
