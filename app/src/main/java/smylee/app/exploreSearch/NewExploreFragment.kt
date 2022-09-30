package smylee.app.exploreSearch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.item_tablayout.view.*
import kotlinx.android.synthetic.main.new_explore_screen.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.adapter.SliderAdapter
import smylee.app.discovercategory.DebatesAdapter
import smylee.app.discovercategory.DiscoverCategoryDetails
import smylee.app.discovercategory.DiscoverViewModel
import smylee.app.engine.GridLayoutSpacing
import smylee.app.model.BannerResponse
import smylee.app.model.ForYouResponse
import smylee.app.model.SearchPeopleResponse
import smylee.app.model.UserCategoryWise
import smylee.app.playvideo.VideoDetailActivity
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

class NewExploreFragment : Fragment() {
    private lateinit var discoverViewModel: DiscoverViewModel
    private val sliderList = ArrayList<BannerResponse>()
    lateinit var activity: BaseActivity
    private var pageCount = 1
    private var lastAPICallTime: Long = 0
    private var userCategoryWise = ArrayList<UserCategoryWise>()
    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var categoryVideo = ArrayList<ForYouResponse>()
    private var userId: String = ""
    private var searchResultAdapter: SearchResultVideoAdapter? = null
    var strSearch: String = ""
    private var llm: LinearLayoutManager? = null
    private var searchTitle: String = ""
    var followerMap: HashMap<String, String> = HashMap()
    var likeVideoHash: HashMap<String, String> = HashMap()
    var hasLikedMap: HashMap<String, String> = HashMap()
    lateinit var viewModel: SearchViewModel
    private var searchVideoList = java.util.ArrayList<ForYouResponse>()
    private var searchPeopleList = java.util.ArrayList<SearchPeopleResponse>()
    private var llmVideo: GridLayoutManager? = null
    private var searchResultPeopleAdapter: SearchResultPeopleAdapter? = null
    private var userID: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.new_explore_screen, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discoverViewModel = ViewModelProviders.of(this).get(DiscoverViewModel::class.java)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)

        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()

        svExplore?.setQueryHint(Html.fromHtml("<font color = #4E586E>" + getResources().getString(R.string.search) + "</font>"))
        htab_collapse_toolbar?.isTitleEnabled = false
        exploreviewpager.setBackgroundColor(ContextCompat.getColor(context!!, R.color.purple))
        exploreviewpager.setPagingEnabled(true)

        exploreviewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currentPage = position
                handler.removeCallbacks(update)
                handler.postDelayed(update, PERIOD_MS)
            }
        })
        svExplore?.setIconifiedByDefault(false)

        svExplore.isFocusable = false

        Utils.hideKeyboard(svExplore, activity)
        val gridLayoutSpacing = GridLayoutSpacing()
        rvSearchResult.addItemDecoration(gridLayoutSpacing)


        svExplore?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                strSearch = query.trim() + ""
                if (strSearch != "") {
                    isSearchAvailable = true
                }
                nestedScrollView.visibility = View.GONE
                llMainCategory.visibility = View.GONE
                // htab_appbar.visibility = View.GONE
                // htab_appbar.visibility = View.GONE
                exploreviewpager.visibility = View.GONE
                svMainClubHours.visibility = View.VISIBLE
                Utils.hideKeyboard(activity)
                pageCount = 1
                getSearchResults(strSearch)
                return true
            }

            override fun onQueryTextChange(s: String?): Boolean {
                if (svExplore?.query!!.isEmpty()) {
                    //isSearchAvailable =false
                    //clearSearch()
                    Logger.print("onQueryTextChange query isEmpty=============$s")
                    getDiscoverCategory(false)
                    tvPeopleResult.visibility = View.GONE
                    tvSearchResult.visibility = View.GONE
                    tv_viewmore_videos.visibility = View.GONE
                    tv_viewmore_people.visibility = View.GONE
                    svMainClubHours.visibility = View.GONE
                    no_data_found.visibility = View.GONE
                    searchPeopleList.clear()
                    searchVideoList.clear()
                    if (searchResultAdapter != null) {
                        searchResultAdapter!!.notifyDataSetChanged()
                    }
                    if (searchResultPeopleAdapter != null) {
                        searchResultPeopleAdapter!!.notifyDataSetChanged()
                    }
                    strSearch = ""
                    pageCount = 1
                    nestedScrollView.visibility = View.VISIBLE
                    llMainCategory.visibility = View.VISIBLE
                    // htab_appbar.visibility = View.VISIBLE
                    exploreviewpager.visibility = View.VISIBLE
                }
                return false
            }
        })
        setSlider()
        getDiscoverCategory(true)

    }

    fun loadData() {
        val isAnythingChangedDiscover = SharedPreferenceUtils.getInstance(activity)
            .getBoolanValue(Constants.IS_ANYTHING_CHANGE_DISCOVER, false)
        if ((System.currentTimeMillis() - lastAPICallTime) > 1500000 || isAnythingChangedDiscover) {
            pageCount = 1
            getDiscoverCategory(true)
        }
        SharedPreferenceUtils.getInstance(activity)
            .setValue(Constants.IS_ANYTHING_CHANGE_DISCOVER, false)
    }

    override fun onResume() {
        super.onResume()
        val isAnythingChangedDiscover = SharedPreferenceUtils.getInstance(activity)
            .getBoolanValue(Constants.IS_ANYTHING_CHANGE_DISCOVER, false)

        if (isAnythingChangedDiscover) {
            pageCount = 1
            getDiscoverCategory(true)

        }
        SharedPreferenceUtils.getInstance(activity)
            .setValue(Constants.IS_ANYTHING_CHANGE_DISCOVER, false)

    }

    fun clearSearch() {
        Logger.print("clearSearch================")
        strSearch = ""
        svExplore?.setQuery(strSearch, false)
        svExplore?.clearFocus()
        isSearchAvailable = false
        getDiscoverCategory(false)
        tvPeopleResult.visibility = View.GONE
        tvSearchResult.visibility = View.GONE
        tv_viewmore_videos.visibility = View.GONE
        tv_viewmore_people.visibility = View.GONE
        svMainClubHours.visibility = View.GONE
        no_data_found.visibility = View.GONE
        searchPeopleList.clear()
        searchVideoList.clear()
        if (searchResultAdapter != null) {
            searchResultAdapter!!.notifyDataSetChanged()
        }
        if (searchResultPeopleAdapter != null) {
            searchResultPeopleAdapter!!.notifyDataSetChanged()
        }

        pageCount = 1
        nestedScrollView.visibility = View.VISIBLE
        llMainCategory.visibility = View.VISIBLE
        // htab_appbar.visibility = View.VISIBLE
        exploreviewpager.visibility = View.VISIBLE
    }

    private fun getDiscoverCategory(isProgress: Boolean) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        var isProgressShow = false
        if (isProgress) {
            isProgressShow = pageCount == 1

        }

        discoverViewModel.getdiscovercategory(activity, hashMap, isProgressShow)
            .observe(this, Observer {
                if (it != null) {
                    svExplore?.clearFocus()
                    svExplore?.isFocusable = false
                    Utils.hideKeyboard(activity)
                    Utils.hideKeyboard(svExplore!!, activity)
                    lastAPICallTime = System.currentTimeMillis()
                    Logger.print("Discovercategory response : $it")

                    val jsonObject = JSONObject(it.toString())
                    val code = jsonObject.getInt("code")
                    if (code == 1) {
                        //rl_tab.visibility =View.VISIBLE
                        if (jsonObject.has("data")) {
                            val jsonobj1 = jsonObject.getJSONObject("data")
                            val jsonArray = jsonobj1.getJSONArray("userCategoryWiseList")
                            val array =
                                Gson().fromJson<ArrayList<UserCategoryWise>>(
                                    jsonArray.toString(),
                                    object : TypeToken<ArrayList<UserCategoryWise>>() {}.type
                                )

                            if (pageCount == 1) {
                                userCategoryWise.clear()
                            }
                            if (array.isNotEmpty()) {
                                userCategoryWise.addAll(array)
                            } else {
                                isDataFinished = true
                            }
                            setArrayData()
                            if (categoryVideo.size == 0) {
                                isDataFinished = true
                            }
                            if (userCategoryWise.size > 0) {
                                nestedScrollView?.visibility = View.VISIBLE
                                //  htab_appbar?.visibility = View.VISIBLE
                                exploreviewpager?.visibility = View.VISIBLE
                                tv_nodata.visibility = View.GONE
                                isLoading = false
                            } else {
                                nestedScrollView?.visibility = View.GONE
                                tv_nodata.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            })
    }

    private fun setArrayData() {
        llMainCategory.removeAllViews()

        for (i in userCategoryWise.indices) {
            if (userCategoryWise[i].category_video_list.isNotEmpty()) {
                Log.d("uservideo", userCategoryWise[i].category_video_list.toString())
                val v = LayoutInflater.from(context)
                    .inflate(R.layout.new_explore_adapter, null, false)
                val tvCatName = v.findViewById(R.id.tv_cat_name) as AppCompatTextView
                val llViewAll = v.findViewById(R.id.ll_ViewAll) as LinearLayout
                val rvCat = v.findViewById(R.id.rv_cat) as RecyclerView
                val tvViewAll = v.findViewById(R.id.tv_viewall) as AppCompatTextView

                if (userCategoryWise[i].category_count != null && !userCategoryWise[i].category_count!!.contentEquals(
                        "null"
                    )
                ) {
                    Methods.showViewCounts(userCategoryWise[i].category_count!!, tvViewAll)
                }

                if (userCategoryWise[i].category_video_list.size == 3) {
                    llViewAll.visibility = View.VISIBLE
                } else {
                    llViewAll.visibility = View.INVISIBLE
                }

                ViewCompat.setNestedScrollingEnabled(rvCat, false)
                var catID = ""
                val gridLayoutSpacing = GridLayoutSpacing()
                for (j in userCategoryWise[i].category_video_list.indices) {
                    catID = userCategoryWise[i].category_video_list[j].category_id.toString()

                }

                llViewAll.setOnClickListener {
                    Logger.print("cat_ID for detail***&&&=======$catID")
                    val intent = Intent(context, DiscoverCategoryDetails::class.java)
                    intent.putExtra("cat_name", userCategoryWise[i].category_name)
                    intent.putExtra("cat_Id", catID)
                    context!!.startActivity(intent)
                }

                tvCatName.text = userCategoryWise[i].category_name
                Logger.print("USER_ID============$userId")
                rvCat.apply {
                    isNestedScrollingEnabled = false
                    addItemDecoration(gridLayoutSpacing)
                    layoutManager = object : GridLayoutManager(context, 3) {
                        override fun canScrollHorizontally(): Boolean {
                            return false
                        }

                        override fun canScrollVertically(): Boolean {
                            return false
                        }
                    }
                    adapter =
                        DebatesAdapter(context!!, userCategoryWise[i].category_video_list, userId)
                }
                llMainCategory.addView(v)
                categoryVideo.addAll(userCategoryWise[i].category_video_list)
            }
        }
    }

    private fun getSearchResults(STR_SEARCH: String) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["search_text"] = STR_SEARCH
        searchTitle = STR_SEARCH
//        val apiName: String = APIConstants.SEARCHVIDEOLIST
        val isProgressShow: Boolean = pageCount == 1

        viewModel.getSearchResults(activity, hashMap, isProgressShow).observe(
            this, Observer {
                if (it != null) {
                    svExplore?.clearFocus()
                    Utils.hideKeyboardActivity(activity)

                    Logger.print("SEARCH RESULTS response : $it")

                    val jsonObject = JSONObject(it.toString())
                    val code = jsonObject.getInt("code")
                    if (code == 1) {
                        if (jsonObject.has("data")) {
                            val jsonobj1 = jsonObject.getJSONObject("data")
                            val jsonArray = jsonobj1.getJSONArray("user_video_list")
                            val jsonArrayPeople = jsonobj1.getJSONArray("user_list")

                            val array =
                                Gson().fromJson<ArrayList<ForYouResponse>>(
                                    jsonArray.toString(),
                                    object : TypeToken<ArrayList<ForYouResponse>>() {}.type
                                )

                            val arrayPeople = Gson().fromJson<ArrayList<SearchPeopleResponse>>(
                                jsonArrayPeople.toString(),
                                object : TypeToken<ArrayList<SearchPeopleResponse>>() {}.type
                            )

                            val userID =
                                SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                                    .getStringValue(Constants.USER_ID_PREF, "").toString()
                            if (pageCount == 1) {
                                searchVideoList.clear()
                                searchPeopleList.clear()
                                followerMap.clear()
                                hasLikedMap.clear()
                                likeVideoHash.clear()
                            }

                            if (arrayPeople.isNotEmpty()) {
                                searchPeopleList.addAll(arrayPeople)
                                for (i in searchPeopleList.indices) {
                                    followerMap[searchPeopleList[i].user_id.toString()] =
                                        searchPeopleList[i].is_following.toString()
                                }
                            } else {
                                isDataFinished = true
                            }
                            if (array.isNotEmpty()) {
                                searchVideoList.addAll(array)
                                for (i in searchVideoList.indices) {
                                    followerMap[searchVideoList[i].user_id.toString()] =
                                        searchVideoList[i].is_following.toString()
                                    likeVideoHash[searchVideoList[i].post_id.toString()] =
                                        searchVideoList[i].post_like_count.toString()
                                    hasLikedMap[searchVideoList[i].post_id.toString()] =
                                        searchVideoList[i].has_liked.toString()
                                }
                            } else {
                                isDataFinished = true
                            }

                            if (searchPeopleList.size > 0) {

                                /*if (currentTab!=0)
                                {*/
                                tvPeopleResult.visibility = View.VISIBLE
                                rvPeopleResult.visibility = View.VISIBLE

                                if (searchPeopleList.size == 6) {
                                    tv_viewmore_people.visibility = View.VISIBLE
                                } else {
                                    tv_viewmore_people.visibility = View.INVISIBLE
                                }

                                tv_viewmore_people.setOnClickListener {
                                    // activity.isAttachFragmentAgain = false
                                    val intent = Intent(activity, SearchViewmorePeople::class.java)
                                    intent.putExtra("search_txt", STR_SEARCH)
                                    startActivity(intent)
                                }
                                isLoading = false
                                setAdapterPeople(userID)
//                                }

                            } else {
                                tvPeopleResult.visibility = View.GONE
                                tv_viewmore_people.visibility = View.GONE
                                rvPeopleResult.visibility = View.GONE
                            }

                            if (searchVideoList.size > 0) {
                                rvSearchResult.visibility = View.VISIBLE
                                tvSearchResult.visibility = View.VISIBLE

                                if (searchVideoList.size == 6) {
                                    tv_viewmore_videos.visibility = View.VISIBLE
                                } else {
                                    tv_viewmore_videos.visibility = View.INVISIBLE
                                }

                                tv_viewmore_videos.setOnClickListener {
                                    // activity.isAttachFragmentAgain = false
                                    val intent = Intent(activity, SearchViewmoreVideos::class.java)
                                    intent.putExtra("search_txt", STR_SEARCH)
                                    startActivity(intent)
                                }
                                isLoading = false
                                setAdapter(userID)
                            } else {
                                if (searchVideoList.size == 0 && searchPeopleList.size == 0) {
                                    no_data_found.visibility = View.VISIBLE
                                    svMainClubHours.visibility = View.GONE
                                } else {
                                    no_data_found.visibility = View.GONE
                                    svMainClubHours.visibility = View.VISIBLE
                                }

                                tvSearchResult.visibility = View.GONE
                                tv_viewmore_videos.visibility = View.GONE
                                rvSearchResult.visibility = View.GONE
                            }
                            Logger.print("serchVideoList===========" + searchVideoList.size)
                            Logger.print("searchPeople===========" + searchPeopleList.size)
                        }
                    }
                }
            }
        )
    }

    private fun setAdapterPeople(userID: String) {
        if (searchResultPeopleAdapter == null) {
            llm = LinearLayoutManager(activity)
            rvPeopleResult.layoutManager = llm
            searchResultPeopleAdapter =
                SearchResultPeopleAdapter(activity, userID, searchPeopleList, followerMap,
                    object : SearchResultPeopleAdapter.ManageClick {
                        override fun onClick(User_Id: String?, IS_UNFOLLOW: Boolean) {
                            if (User_Id != null && !User_Id.contentEquals("")) {
                                if (IS_UNFOLLOW) {
                                    unFollowUser(User_Id)
                                }

                                if (!IS_UNFOLLOW) {
                                    followUser(User_Id)
                                }
                            }
                        }
                    }
                )
            rvPeopleResult.adapter = searchResultPeopleAdapter
        } else {
            searchResultPeopleAdapter?.notifyDataSetChanged()
        }
    }

    private fun followUser(User_Id: String) {
        val isFollow = "1"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = User_Id

//        val apiName: String = APIConstants.FOLLOWUNFOLLOWUSER
        viewModel.FollowUnfollow(activity, hashMap).observe(this, Observer {
            if (it != null) {
                SharedPreferenceUtils.getInstance(activity)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                SharedPreferenceUtils.getInstance(activity)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)

                Logger.print("Search FollowUser================ $it")
            }
        })
    }

    private fun unFollowUser(User_Id: String) {
        val isFollow = "0"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = User_Id

//        val apiName: String = APIConstants.FOLLOWUNFOLLOWUSER
        viewModel.FollowUnfollow(activity, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("Search UnFollowUser================ $it")
                SharedPreferenceUtils.getInstance(activity)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                SharedPreferenceUtils.getInstance(activity)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)

            }
        })
    }

    private fun setAdapter(userID: String) {
        if (searchResultAdapter == null) {
            llmVideo = GridLayoutManager(activity, 3)
            rvSearchResult.layoutManager = llmVideo
            searchResultAdapter = SearchResultVideoAdapter(
                activity,
                userID,
                followerMap,
                hasLikedMap,
                likeVideoHash,
                searchVideoList, object : SearchResultVideoAdapter.ManageClick {

                    override fun managePlayVideoClick(position: Int?) {
                        val intent = Intent(activity, VideoDetailActivity::class.java)
                        intent.putExtra("position", position)
                        intent.putExtra("cat_ID", "")
                        intent.putExtra("screen", "search")
                        intent.putExtra("hashtag", "")
                        intent.putExtra("searchTxt", strSearch)
                        intent.putExtra("responseData", searchVideoList)
                        intent.putExtra("pageNo", pageCount)
                        intent.putExtra("userIDApi", "")
                        startActivity(intent)
                    }
                }
            )
            rvSearchResult.adapter = searchResultAdapter
        } else {
            searchResultAdapter?.notifyDataSetChanged()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as BaseActivity

    }

    private fun setSlider() {
        val hashMap = HashMap<String, String>()
        discoverViewModel.getBanner(activity, hashMap, false).observe(this, Observer {
            if (it != null) {
                Utils.hideKeyboard(activity)
                //  htab_appbar.visibility = View.VISIBLE
                exploreviewpager.visibility = View.VISIBLE
                Logger.print("getBanner Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val jsonArray = jsonObject.getJSONArray("data")
                        val array = Gson().fromJson<ArrayList<BannerResponse>>(
                            jsonArray.toString(),
                            object : TypeToken<ArrayList<BannerResponse>>() {}.type
                        )

                        if (array.isNotEmpty()) {
                            sliderList.addAll(array)
                            exploreviewpager.adapter = SliderAdapter(activity, sliderList)
                            // indicator.setViewPager(exploreviewpager)
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
                        handler.postDelayed(
                            update,
                            PERIOD_MS
                        )
                    }
                } else {
                    Utils.showToastMessage(activity, jsonObject["message"].toString())
                }
            }
        })
    }

    companion object {
        var isSearchAdded: Boolean = false
        var isSearchAvailable: Boolean = false
        val handler = Handler()
        const val PERIOD_MS: Long = 5000 // time in milliseconds between successive task executions.
        var currentPage = 0

        /*val DELAY_MS: Long = 600
        val swipeTimer = Timer()*/
        var update = Runnable { }
    }
}