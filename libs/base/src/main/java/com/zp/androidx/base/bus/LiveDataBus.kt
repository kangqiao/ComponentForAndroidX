package com.zp.androidx.base.bus

import androidx.annotation.MainThread
import androidx.lifecycle.*
import java.lang.reflect.InvocationHandler
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy

/**
 * Created by zhaopan on 2019-10-14.
 */

class LiveDataBus {
    companion object{
        private val INSTANCE by lazy { LiveDataBus() }
        @JvmStatic
        fun get() = INSTANCE
    }

    internal val liveDatas by lazy { mutableMapOf<String, LiveData<*>>() }

    @Synchronized
    private fun <T>bus(channel: String): LiveData<T> {
        /*return liveDatas.getOrPut(channel){
            LiveDataEvent<T>(channel)
        } as LiveData<T>*/
        return MutableLiveData()
    }

    fun <T> with(channel: String): LiveData<T> {
        return bus(channel)
    }

    /*fun <E> of(clz: Class<E>): E {
        if(!clz.isInterface){
            throw IllegalArgumentException("API declarations must be interfaces.")
        }
        if(0 < clz.interfaces.size){
            throw IllegalArgumentException("API interfaces must not extend other interfaces.")
        }

        return Proxy.newProxyInstance(clz.classLoader, arrayOf(clz), InvocationHandler { _, method, _->
            return@InvocationHandler get().with(
                // 事件名以集合类名_事件方法名定义
                // 以此保证事件的唯一性
                "${clz.canonicalName}_${method.name}",
                (method.genericReturnType as ParameterizedType).actualTypeArguments[0].javaClass)
        }) as E
    }*/


}


internal class ExternalLiveData<T>(val key: String) : MutableLiveData<T>(){
    internal var mObservers = mutableMapOf<Observer<in T>, ExternalObserverWrapper<T>>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val exist = mObservers.getOrPut(observer){
            LifecycleExternalObserver(observer, this, owner).apply {
                mObservers[observer] = this
                owner.lifecycle.addObserver(this)
            }
        }
        super.observe(owner, exist)
    }

    @MainThread
    override fun observeForever(observer: Observer<in T>) {
        val exist = mObservers.getOrPut(observer){
            AlwaysExternalObserver(observer, this).apply { mObservers[observer] = this }
        }
        super.observeForever(exist)
    }

    @MainThread
    fun observeSticky(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, observer)
    }

    @MainThread
    fun observeStickyForever(observer: Observer<in T>){
        super.observeForever(observer)
    }

    @MainThread
    override fun removeObserver(observer: Observer<in T>) {
        val exist = mObservers.remove(observer) ?: observer
        super.removeObserver(exist)
    }

    @MainThread
    override fun removeObservers(owner: LifecycleOwner) {
        mObservers.iterator().forEach { item->
            if(item.value.isAttachedTo(owner)){
                mObservers.remove(item.key)
            }
        }
        super.removeObservers(owner)
    }

    override fun onInactive() {
        super.onInactive()
        if(!hasObservers()){
            // 当对应liveData没有相关的观察者的时候
            // 就可以移除掉维护的LiveData
            LiveDataBus.get().liveDatas.remove(key)
        }
    }
}

internal open class ExternalObserverWrapper<T>(val observer: Observer<in T>, val liveData: ExternalLiveData<T>): Observer<T>{

    //private var mLastVersion = liveData.version
    override fun onChanged(t: T) {
        /*if(mLastVersion >= liveData.version){
            return
        }
        mLastVersion = liveData.version*/
        observer.onChanged(t)
    }

    open fun isAttachedTo(owner: LifecycleOwner) = false
}

/**
 * always active 的观察者包装类
 * @param T
 * @constructor
 */
internal class AlwaysExternalObserver<T>(observer: Observer<in T>, liveData: ExternalLiveData<T>):
    ExternalObserverWrapper<T>(observer, liveData)

/**
 * 绑定生命周期的观察者包装类
 * @param T
 * @property owner LifecycleOwner
 * @constructor
 */
internal class LifecycleExternalObserver<T>(observer: Observer<in T>, liveData: ExternalLiveData<T>, val owner: LifecycleOwner): ExternalObserverWrapper<T>(
    observer,
    liveData
), LifecycleObserver {
    /**
     * 当绑定的lifecycle销毁的时候
     * 移除掉内部维护的对应观察者
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(){
        liveData.mObservers.remove(observer)
        owner.lifecycle.removeObserver(this)
    }

    override fun isAttachedTo(owner: LifecycleOwner): Boolean {
        return owner == this.owner
    }
}