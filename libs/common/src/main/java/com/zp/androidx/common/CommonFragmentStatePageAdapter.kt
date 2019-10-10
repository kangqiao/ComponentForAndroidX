package com.zp.androidx.common

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter


/**
 * Created by zhaopan on 2018/10/30.
 * https://www.jianshu.com/p/3d68d6ec9468
 * FragmentStatePagerAdapter，在切换不同的Fragment的时候，会把前面的Fragment销毁，
 * 而我们系统在销毁前，会把我们的我们Fragment的Bundle在我们的onSaveInstanceState(Bundle)保存下来。
 * 等用户切换回来的时候，我们的Fragment就会根据我们的instance state恢复出来。
 */

@SuppressLint("WrongConstant")
class CommonFragmentStatePageAdapter(fm: FragmentManager, val tabList: List<String>, val fragmentList: List<Fragment>): FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return fragmentList.get(position % fragmentList.size)
    }

    override fun getCount(): Int {
        return tabList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabList.get(position % tabList.size)
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}