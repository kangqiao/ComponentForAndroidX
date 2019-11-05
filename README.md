# ComponentForAndroidX
Android轻量级组件化架构实现(基于AndroidX, Kotlin, MVVM)



### MVVM 之 LiveData 
###### LiveData的优点：
- 确保UI符合数据状态
LiveData遵循观察者模式。 当生命周期状态改变时，LiveData会向Observer发出通知。 您可以把更新UI的代码合并在这些Observer对象中。不必去考虑导致数据变化的各个时机，每次数据有变化，Observer都会去更新UI。
- 没有内存泄漏
Observer会绑定具有生命周期的对象，并在这个绑定的对象被销毁后自行清理。
- 不会因停止Activity而发生崩溃
如果Observer的生命周期处于非活跃状态，例如在后退堆栈中的Activity，就不会收到任何LiveData事件的通知。
- 不需要手动处理生命周期
UI组件只需要去观察相关数据，不需要手动去停止或恢复观察。LiveData会进行自动管理这些事情，因为在观察时，它会感知到相应组件的生命周期变化。
- 始终保持最新的数据
如果一个对象的生命周期变到非活跃状态，它将在再次变为活跃状态时接收最新的数据。 例如，后台Activity在返回到前台后立即收到最新数据。
- 正确应对配置更改
如果一个Activity或Fragment由于配置更改（如设备旋转）而重新创建，它会立即收到最新的可用数据。
- 共享资源
您可以使用单例模式扩展LiveData对象并包装成系统服务，以便在应用程序中进行共享。LiveData对象一旦连接到系统服务，任何需要该资源的Observer都只需观察这个LiveData对象。
###### 要点概述
- LiveData的观察者可以联动生命周期, 也可以不联动
- LiveData的观察者只能与一个LifecycleOwner绑定, 否则会抛出异常
- 当观察者的active状态变更的时候
  - active->inactive : 如果LiveCycler通知OnDestroy, 则移除对应的观察者, 切当所有观察者都非活跃的状态下时, 会触发onInactive
  - inactive->active: 会通知观察者最近的数据更新(粘性消息)
- 除了观察者状态变更时, 会接收到数据更新的通知外, 还有一种就是在活跃的情况下, 通过开发者主动更新数据, 会接收到数据更新的通知.


## [AndroidX项目中接入Flutter](./doc/AndroidX项目中接入Flutter.md)


## 参考

[MVVM 架构演进(二) —— DataBinding 实现原理](https://www.jianshu.com/p/5496a2e62842)

