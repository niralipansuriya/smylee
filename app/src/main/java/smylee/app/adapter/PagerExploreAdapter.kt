package smylee.app.adapter

import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import smylee.app.discovercategory.DiscoverFragment
import smylee.app.trending.TrendingNewFragment
import smylee.app.utils.Logger

class PagerExploreAdapter(fm: FragmentManager?, private var tabCount: Int, private var trending_frg: String, private var discover_frg: String,
        private var rl_tab: RelativeLayout) : FragmentStatePagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var discoverFragment: DiscoverFragment? = null
    private var trendingFragment: TrendingNewFragment? = null

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                trendingFragment = TrendingNewFragment()
                trendingFragment?.displayTab(rl_tab)
                trendingFragment
            }
            1 -> {
                discoverFragment = DiscoverFragment()
                discoverFragment
            }
            else -> null
        }!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return trending_frg
            1 -> return discover_frg
        }
        return null
    }

    override fun getCount(): Int {
        return tabCount
    }

    fun loadData(index: Int) {
        if (trendingFragment != null && index == 0) {
            Logger.print("trendingFragment not null loadData")
            trendingFragment?.loadData()
            trendingFragment?.displayTab(rl_tab)

        } else if (discoverFragment != null) {
            Logger.print("discoverFragment not null loadData")

            discoverFragment?.loadData()
        }

    }
}