package com.zp.androidx.project.ui

import com.zp.androidx.base.arch.mvp.RxPresenter
import com.zp.androidx.base.utils.RxUtil
import com.zp.androidx.project.ServerAPI

/**
 * Created by zhaopan on 2018/11/17.
 */

class CategoryTabPresenter(
    val serverAPI: ServerAPI
) : RxPresenter<CategoryTabContract.View>(), CategoryTabContract.Presenter {

    override fun getCategoryList() {
        launch {
            serverAPI.getProjectTree()
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe({
                    if (it.isSuccess()) {
                        view?.setCategoryList(it.data)
                    } else {
                        view?.setCategoryList(null)
                    }
                }, { error ->
                    view?.showError(error)
                })
        }
    }

}