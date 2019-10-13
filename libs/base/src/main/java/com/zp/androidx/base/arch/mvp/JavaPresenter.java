package com.zp.androidx.base.arch.mvp;


import org.jetbrains.annotations.Nullable;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by zhaopan on 2018/11/19.
 */
public abstract class JavaPresenter<V> implements BasePresenter<V> {

    protected V view = null;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void subscribe(V view) {
        setView(view);
    }

    @Override
    public void unSubscribe() {
        disposables.clear();
        setView(null);
    }

    protected void launch(Disposable disposable) {
        disposables.add(disposable);
    }

    @Nullable
    @Override
    public V getView() {
        return this.view;
    }

    @Override
    public void setView(@Nullable V v) {
        this.view = v;
    }
}
