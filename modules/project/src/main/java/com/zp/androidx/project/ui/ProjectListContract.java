package com.zp.androidx.project.ui;

import com.zp.androidx.base.mvp.BasePresenter;
import com.zp.androidx.base.mvp.BaseView;
import com.zp.androidx.project.ArticleResponseBody;

/**
 * Created by zhaopan on 2018/11/17.
 */
public interface ProjectListContract {

    interface View extends BaseView<Presenter> {
        void showProjectList(ArticleResponseBody body);
    }

    interface Presenter extends BasePresenter<View> {
        void getProjectList(int page, int cid);
    }
}
