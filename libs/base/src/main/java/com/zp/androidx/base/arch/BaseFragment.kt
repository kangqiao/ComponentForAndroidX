package com.zp.androidx.base.arch

import android.os.Bundle
import android.view.View
import me.yokeyword.fragmentation.SupportFragment

/**
 * Created by zhaopan on 2018/8/28.
 */

open abstract class BaseFragment : SupportFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    open fun initView(view: View) {}
}