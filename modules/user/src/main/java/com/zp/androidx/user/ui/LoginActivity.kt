package com.zp.androidx.user.ui

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.zp.androidx.base.arch.BaseActivity
import com.zp.androidx.base.RxBus
import com.zp.androidx.base.arch.mvvm.ExceptionEvent
import com.zp.androidx.base.arch.mvvm.FailedEvent
import com.zp.androidx.base.arch.mvvm.LoadingEvent
import com.zp.androidx.base.arch.mvvm.SuccessEvent
import com.zp.androidx.base.utils.CtxUtil.showToast
import com.zp.androidx.component.RouterConfig
import com.zp.androidx.component.event.LoginSuccessEvent
import com.zp.androidx.user.R
import com.zp.androidx.user.databinding.UserActivityLoginBinding
import timber.log.Timber
import org.koin.androidx.viewmodel.dsl.viewModel

/**
 * Created by zhaopan on 2018/11/7.
 */

@Route(path = RouterConfig.User.LOGIN, name = "登录")
class LoginActivity : BaseActivity() {

    companion object {
        const val TAG = "LoginActivity"
        fun open() {
            ARouter.getInstance().build(RouterConfig.User.LOGIN).navigation()
        }
    }

    private lateinit var binding: UserActivityLoginBinding
    private val vm by viewModel<UserViewModel>()
    private val onClickListener by lazy { initClickListener() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.user_activity_login)
        binding.run {
            viewModel = vm
            clickListener = onClickListener
            setLifecycleOwner(this@LoginActivity)
        }

        vm.events.observe(this, Observer { event ->
            when(event){
                is LoadingEvent -> {
                    //toast(getString(R.string.login_ing))
                }
                is SuccessEvent -> {
                    showToast(getString(R.string.login_success))
                    RxBus.postSticky(LoginSuccessEvent)
                    finish()
                }
                is FailedEvent -> {
                    showToast(event.errorMsg)
                }
                is ExceptionEvent -> {
                    Timber.e(event.error)
                }
            }
        })
    }

    private fun initClickListener() = View.OnClickListener {
        when(it.id){
            R.id.btn_login -> { //登录
                if (vm.username.value.isNullOrBlank()) {
                    binding.etUsername.error = getString(R.string.username_not_empty)
                    return@OnClickListener
                }
                if (vm.password.value.isNullOrBlank()) {
                    binding.etPassword.error = getString(R.string.password_not_empty)
                    return@OnClickListener
                }
                vm.loginWanAndroid()
            }
            R.id.tv_sign_up -> { //注册
                RegisterActivity.open()
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

}