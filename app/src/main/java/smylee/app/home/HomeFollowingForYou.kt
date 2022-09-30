package smylee.app.home

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_home_following_for_you.*
import smylee.app.R


class HomeFollowingForYou : Fragment() {

    var adapter: HomePagerAdapter? = null
    private var postId: String = ""
    private var bottomNavigationBarHeight = 0
    var currentTab: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tab_home.addTab(tab_home.newTab().setText("  For You"))
        tab_home.addTab(tab_home.newTab().setText("Following   "))

        Log.i("HomeFollowingForYou", "postId $postId")
        tab_home.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                adapter?.onLoad(currentTab)
            }
        })
        adapter = HomePagerAdapter(fragmentManager, tab_home.tabCount, resources.getString(R.string.for_you), resources.getString(R.string.following), postId)
        pager_forYou_following.offscreenPageLimit = 2
        retainInstance = true
        pager_forYou_following.adapter = adapter
        pager_forYou_following.requestTransparentRegion(pager_forYou_following)
        if (adapter != null) {
            adapter!!.notifyDataSetChanged()
        }
        adapter?.setTabHeight(bottomNavigationBarHeight)
        pager_forYou_following.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                Log.i("HomeFollowingForYou", "onPageSelected $position")
                if (position == 0) {
                    adapter?.onStop(1)
                    adapter?.playVideo(position, false)
                    adapter?.onLoad(position)
                } else {
                    adapter?.onStop(0)
                    adapter?.playVideo(position, false)
                    adapter?.onLoad(position)
                }
            }
        })

        tab_home.setupWithViewPager(pager_forYou_following)
        val root: View = tab_home.getChildAt(0)

        if (root is LinearLayout) {
            root.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            val drawable = GradientDrawable()
            drawable.setColor(ContextCompat.getColor(context!!, R.color.white))
            drawable.setSize(1, 0)
            root.dividerPadding = 40
            root.dividerDrawable = drawable
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_following_for_you, container, false)
    }

    fun onStopAllVideo() {
        if (adapter != null) {
            adapter?.onStop()
        }
    }

    fun blockResume() {
        if (pager_forYou_following != null) {
            adapter?.callForBlock(pager_forYou_following.currentItem)
        }
    }

    fun callOnBackPressed(activity: HomeActivity) {
        if (pager_forYou_following != null) {
            adapter?.callOnBackPressed(pager_forYou_following.currentItem, activity)
        }
    }

    fun resumeVideoPlay(isLoadData: Boolean) {
        HomeActivity.tabIndex = 0
        if (adapter != null && pager_forYou_following != null) {
            Log.i("HomeFollowingForYou", "resumeVideoPlay $isLoadData")
            adapter?.playVideo(pager_forYou_following.currentItem, isLoadData)
        } else {
            Log.i("HomeFollowingForYou", "Adapter is null")
        }
    }

    override fun onPause() {
        Log.i("HomeFollowingForYou", "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.i("HomeFollowingForYou", "onStop")
        adapter?.onStop()
        super.onStop()
    }

    override fun onDetach() {
        Log.i("HomeFollowingForYou", "onDetach")
        super.onDetach()
    }

    override fun onDestroy() {
        Log.i("HomeFollowingForYou", "onDestroy")
        adapter = null
        super.onDestroy()
    }

    fun setTabHeight(tabHeight: Int) {
        Log.i("HomeFollowingForYou", "Tab bar height $tabHeight")
        bottomNavigationBarHeight = tabHeight
        if (adapter != null) {
            adapter?.setTabHeight(bottomNavigationBarHeight)
        }
    }

    fun setPostIdLink(postId: String) {
        this.postId = postId
    }

    companion object {
        fun newInstance(): HomeFollowingForYou {
            return HomeFollowingForYou()
        }
    }
}