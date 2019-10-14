package com.zp.androidx.home.ui

import androidx.lifecycle.MutableLiveData
import com.zp.androidx.base.arch.mvvm.*
import com.zp.androidx.base.utils.RxUtil
import com.zp.androidx.home.ArticleResponseBody
import com.zp.androidx.home.BannerItem
import com.zp.androidx.home.HomeApi
import com.zp.androidx.base.arch.mvvm.RxViewModel

/**
 * Created by zhaopan on 2018/10/21.
 */

class HomeViewModel(
    val homeApi: HomeApi
) : RxViewModel() {

    val events =  SingleLiveEvent<ViewModelEvent>()
    val articleData = MutableLiveData<ArticleResponseBody>()
    val bannerList = MutableLiveData<List<BannerItem>>()

    fun getArticleData(num: Int){
        if (0 == num) events.value = LoadingEvent
        launch {
            homeApi.getArticles(num)
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe({
                    if(it.isSuccess()){
                        events.value = SuccessEvent
                        articleData.value = it.data
                    } else {
                        events.value = FailedEvent(it.errorMsg)
                    }
                }, {
                    events.value = ExceptionEvent(it)
                })
        }
    }

    fun getBannerList(){
        launch {
            homeApi.getBanners()
                //.retryWhen(RxUtil.retryAndDelay())
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe({
                    if(it.isSuccess()){
                        bannerList.value = it.data
                    }
                }, {
                    events.value = ExceptionEvent(it)
                })
        }
    }


}