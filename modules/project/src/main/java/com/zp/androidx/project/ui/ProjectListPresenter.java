package com.zp.androidx.project.ui;

import com.zp.androidx.base.arch.mvp.JavaPresenter;
import com.zp.androidx.base.utils.RxUtil;
import com.zp.androidx.project.ServerAPI;
import io.reactivex.functions.Action;
import kotlin.Lazy;
import org.koin.java.KoinJavaComponent;


/**
 * Created by zhaopan on 2018/11/17.
 */
public class ProjectListPresenter extends JavaPresenter<ProjectListContract.View> implements ProjectListContract.Presenter {

    Lazy<ServerAPI> serverAPI = KoinJavaComponent.inject(ServerAPI.class);

    @Override
    public void getProjectList(int page, int cid) {
        launch(serverAPI.getValue().getProjectList(page, cid)
                //.doOnSubscribe(disposable -> launch(disposable))
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe(result -> {
                    if (null != getView()) {
                        if (result.isSuccess()) {
                            getView().showProjectList(result.getData());
                        } else {
                            getView().showError(null);
                        }
                    }
                }, throwable -> {
                    if (null != view) view.showError(throwable);
                }));
    }
}
