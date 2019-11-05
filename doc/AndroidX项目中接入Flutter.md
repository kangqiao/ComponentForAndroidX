

## [AndroidX项目中接入Flutter](https://juejin.im/post/5d4002b4f265da03925a2165)


### 创建flutter module
进入当前android项目，在根目录运行如下命令：

```
flutter create -t module my_flutter
cd my_flutter
cd .android/
./gradlew flutter:assembleDebug
```
在 android项目 根目录下的 settings.gradle 中添加如下代码
```
setBinding(new Binding([gradle: this]))
evaluate(new File(
        rootDir.path + '/my_flutter/.android/include_flutter.groovy'
))
```
到这里，基本上就可以开始接入flutter的内容了
不过这时候还有一个问题需要注意，如果你的android项目已经迁移到了androidx，可能你会遇到下面的这种问题

![flutter_load_androidx_failed](doc/image/flutter_load_androidx_failed.jpg)

这种问题明显是因为flutter创建moudle时，并未做到androidx的转换，因为创建moudle的命令还不支持androidx

参考[Generated Flutter Module Files Do Not Use AndroidX](https://link.juejin.im/?target=https%3A%2F%2Fgithub.com%2Fflutter%2Fflutter%2Fissues%2F28805)

### 解决androidx带来的问题
首先，如果你原先的android项目已经迁移到了androidx，那么在根目录下的 grale.properties 一定有如下内容
```
# 表示使用 androidx
android.useAndroidX=true
# 表示将第三方库迁移到 androidx
android.enableJetifier=true
```
下面进入到 my_flutter 目录下，在 你的android项目/my_flutter/.android/Flutter/build.gradle 中对库的依赖部分进行修改
如果默认的内容如下：
```
dependencies {
    testImplementation 'junit:junit:4.12'
    implementation 'com.android.support:support-v13:27.1.1'
    implementation 'com.android.support:support-annotations:27.1.1'
}
```
复制代码将所有依赖修改为androidx的版本：
```
dependencies {
    testImplementation 'junit:junit:4.12'
    implementation 'androidx.legacy:legacy-support-v13:1.0.0'
    implementation 'androidx.annotation:annotation:1.0.0'
}
```
复制代码在android studio上点击完 Sync Now 同步之后
再进入下面的目录 你的android项目/my_flutter/.android/Flutter/src/main/java/io/flutter/facade/ 目录下，对 Flutter.java 和 FlutterFragment.java 分别进行修改
##### 修改FlutterFragment.java
将报错部分替换为androidx的版本
```
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
```
##### 修改Flutter.java
将报错部分替换为androidx的版本
```
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
```
复制代码那么现在，androidx带来的问题就解决了，下面就开始准备正式接入Flutter