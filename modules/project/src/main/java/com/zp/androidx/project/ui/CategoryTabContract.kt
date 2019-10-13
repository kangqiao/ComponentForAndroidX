package com.zp.androidx.project.ui

import com.zp.androidx.base.mvp.BasePresenter
import com.zp.androidx.base.mvp.BaseView
import com.zp.androidx.project.ProjectTreeBean

/**
 * Created by zhaopan on 2018/11/17.
 */

interface CategoryTabContract{

    interface View : BaseView<Presenter> {
        fun setCategoryList(list: List<ProjectTreeBean>?)
    }

    interface Presenter : BasePresenter<View> {
        fun getCategoryList()
    }
}