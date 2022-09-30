package smylee.app.discovercategory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_explore.*
import org.json.JSONObject
import smylee.app.Profile.MyProfileFragment
import smylee.app.R
import smylee.app.adapter.PagerExploreAdapter
import smylee.app.adapter.SliderAdapter
import smylee.app.exploreSearch.SearchActivity
import smylee.app.home.HomeActivity
import smylee.app.model.BannerResponse
import smylee.app.trending.TrendingNewFragment
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.utils.Utils
import java.util.*
import kotlin.collections.HashMap


class ExploreFragment : Fragment() {

    private lateinit var discoverViewModel: DiscoverViewModel
    lateinit var activity: BaseActivity
    private val sliderList = ArrayList<BannerResponse>()
    var currentTab: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as BaseActivity
        Logger.print("onAttach ExploreFragment=================")
    }

    override fun onResume() {
        super.onResume()
        Logger.print("onResume ExploreFragment=================")

        if (TrendingNewFragment.isClickSelfProfile) {
            TrendingNewFragment.isClickSelfProfile = false

            if (activity is HomeActivity) {
                val homeActivity = activity as HomeActivity
                //homeActivity.hideTabBar()
                homeActivity.setTabUI(false)
                homeActivity.setSelected()
                //homeActivity.loadFragment(MyProfileFragment())
            }
            val fragment: Fragment
            fragment = MyProfileFragment()
            val ft =
                fragmentManager!!.beginTransaction()
            val backStateName: String = fragment.javaClass.name
            // ft.hide(this)
            //ft.show(fragment)
            ft.commit()
            ft.replace(R.id.container, MyProfileFragment(), "MyProfileFragment")
            // ft.addToBackStack(backStateName)

        }
    }

    override fun onStart() {
        super.onStart()
        Logger.print("onStart ExploreFragment=================")
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.print("onDestroy ExploreFragment=================")
    }

    override fun onDetach() {
        super.onDetach()
        Logger.print("onDetach ExploreFragment=================")
    }

    var adapter: PagerExploreAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discoverViewModel = ViewModelProviders.of(this).get(DiscoverViewModel::class.java)

        tabLayout.addTab(tabLayout.newTab().setText("Discover"))
        tabLayout.addTab(tabLayout.newTab().setText("Trending"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                Logger.print("currentTab***************$currentTab")
                if (tab.position == 0) {
                    setTabBG(R.drawable.tab_left_select, R.drawable.right_tab_unselect)
                } else {
                    setTabBG(R.drawable.left_tab_unselect, R.drawable.right_selector)
                }
            }
        })

        adapter = PagerExploreAdapter(fragmentManager, tabLayout.tabCount, resources.getString(R.string.trending), resources.getString(R.string.discover), rl_tab)
        pager.adapter = adapter
        tabLayout.setupWithViewPager(pager)
        tabLayout.setSelectedTabIndicatorHeight(0)
        setTabBG(R.drawable.tab_left_select, R.drawable.right_tab_unselect)
        setSlider()
        exploreviewpager.setBackgroundColor(ContextCompat.getColor(context!!, R.color.purple))
        exploreviewpager.setPagingEnabled(true)

        exploreviewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                currentPage = position
                handler.removeCallbacks(update)
                handler.postDelayed(update, PERIOD_MS)
            }
        })

        iv_search.setOnClickListener {
            isSearchAdded = true
            val intent = Intent(activity, SearchActivity::class.java)
            intent.putExtra("currentTab", currentTab)
            startActivity(intent)
        }
    }

    fun loadData() {
        if (adapter != null) {
            Logger.print("adapter not null=========")
            if (pager != null) {
                Logger.print("pager not null=========${pager.currentItem}")

                adapter?.loadData(pager.currentItem)
            } else {
                Handler().postDelayed({
                    if (pager != null) {
                        Logger.print("pager not null postDelayed=========${pager.currentItem}")

                        adapter?.loadData(pager.currentItem)

                    }

                }, 200)
            }
        }
    }

    private fun setTabBG(tab1: Int, tab2: Int) {
        val tabStrip: ViewGroup = tabLayout.getChildAt(0) as ViewGroup
        val tabView1: View = tabStrip.getChildAt(0)
        val tabView2: View = tabStrip.getChildAt(1)
        val paddingStart: Int = tabView1.paddingStart
        val paddingTop: Int = tabView1.paddingTop
        val paddingEnd: Int = tabView1.paddingEnd
        val paddingBottom: Int = tabView1.paddingBottom
        ViewCompat.setBackground(tabView1, AppCompatResources.getDrawable(tabView1.context, tab1))
        ViewCompat.setPaddingRelative(tabView1, paddingStart, paddingTop, paddingEnd, paddingBottom)
        val paddingStartSec: Int = tabView2.paddingStart
        val paddingTopSec: Int = tabView2.paddingTop
        val paddingEndSec: Int = tabView2.paddingEnd
        val paddingBottomSec: Int = tabView2.paddingBottom
        ViewCompat.setBackground(tabView2, AppCompatResources.getDrawable(tabView2.context, tab2))
        ViewCompat.setPaddingRelative(tabView2, paddingStartSec, paddingTopSec, paddingEndSec, paddingBottomSec)
    }

    private fun setSlider() {
        val hashMap = HashMap<String, String>()
        discoverViewModel.getBanner(activity, hashMap, false).observe(this, Observer {
            if (it != null) {
                Logger.print("getBanner Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val jsonArray = jsonObject.getJSONArray("data")
                        val array = Gson().fromJson<ArrayList<BannerResponse>>(jsonArray.toString(),
                            object : TypeToken<ArrayList<BannerResponse>>() {}.type)

                        if (array.isNotEmpty()) {
                            sliderList.addAll(array)
                            exploreviewpager.adapter = SliderAdapter(activity, sliderList)
                            indicator.setViewPager(exploreviewpager)
                        }

                        update = Runnable {
                            if (exploreviewpager != null) {
                                currentPage += 1
                                if (currentPage == sliderList.size) {
                                    currentPage = 0
                                }
                                exploreviewpager.currentItem = currentPage
                            }
                        }
                        handler.postDelayed(update, PERIOD_MS)
                    }
                } else {
                    Utils.showToastMessage(activity, jsonObject["message"].toString())
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    companion object {
        var isSearchAdded: Boolean = false
        val handler = Handler()
        const val PERIOD_MS: Long = 5000 // time in milliseconds between successive task executions.
        var currentPage = 0
        /*val DELAY_MS: Long = 600
        val swipeTimer = Timer()*/
        var update = Runnable { }
    }
}