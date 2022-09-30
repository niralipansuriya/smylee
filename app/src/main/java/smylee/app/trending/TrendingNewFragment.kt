package smylee.app.trending

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.trending_new_layout.*
import org.json.JSONObject
import smylee.app.Profile.MyProfileFragment
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.engine.GridLayoutSpacing
import smylee.app.home.HomeActivity
import smylee.app.model.ForYouResponse
import smylee.app.model.TrendingResponse
import smylee.app.playvideo.VideoDetailActivity
import smylee.app.ui.Activity.HashTagDetailOrTrendingDetail
import smylee.app.ui.base.BaseActivity
import smylee.app.ui.base.BaseFragment_x
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.Methods
import smylee.app.utils.SharedPreferenceUtils

class TrendingNewFragment : BaseFragment_x() {

    lateinit var viewModel: TrendingViewModel
    private var pageCount = 1
    lateinit var activity: BaseActivity
    private var trendingList = ArrayList<TrendingResponse>()
    private var trendingListFinal = ArrayList<ForYouResponse>()
    private var isDataFinished: Boolean = false

    private var userID: String = ""
    var followerMap: HashMap<String, String> = HashMap()
    var likeVideoHash: HashMap<String, String> = HashMap()
    private var lastAPICallTime: Long = 0
    private var rlTab: RelativeLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TrendingViewModel::class.java)
        userID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()

        getHashTagTrendingList()
    }

    fun displayTab(rl_tab: RelativeLayout) {
        rlTab = rl_tab
    }

    override fun onResume() {
        super.onResume()
        val isAnythingChangedTrending = SharedPreferenceUtils.getInstance(activity)
            .getBoolanValue(Constants.IS_ANYTHING_CHANGE_TRENDING, false)

        if (isAnythingChangedTrending) {
            pageCount = 1
            getHashTagTrendingList()
        }
        SharedPreferenceUtils.getInstance(activity)
            .setValue(Constants.IS_ANYTHING_CHANGE_TRENDING, false)
    }

    fun loadData() {
        val isAnythingChangedTrending = SharedPreferenceUtils.getInstance(activity)
            .getBoolanValue(Constants.IS_ANYTHING_CHANGE_TRENDING, false)

        if ((System.currentTimeMillis() - lastAPICallTime) > 1500000 || isAnythingChangedTrending) {
            Logger.print("trendingFragment not null loadData==============")

            pageCount = 1
            getHashTagTrendingList()
        }
        SharedPreferenceUtils.getInstance(activity)
            .setValue(Constants.IS_ANYTHING_CHANGE_TRENDING, false)
    }

    private fun getHashTagTrendingList() {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        val isProgressShow = pageCount == 1

        viewModel.observeHashTagTrendingList(activity, hashMap, isProgressShow)
            .observe(this, Observer {
                if (it != null) {
                    rlTab?.visibility = View.VISIBLE
                    lastAPICallTime = System.currentTimeMillis()
                    Logger.print("TrendingNewList Home response ::::::::: $it")
                    val jsonObject = JSONObject(it.toString())
                    val code = jsonObject.getInt("code")
                    if (code == 1) {
                        tv_nodata.visibility = View.GONE
                        var paginationCount = 0
                        if (jsonObject.has("pagination_count")) {
                            paginationCount = jsonObject.getInt("pagination_count")
                        }

                        if (jsonObject.has("data")) {
                            val jsonObj1 = jsonObject.getJSONObject("data")
                            val jsonArray = jsonObj1.getJSONArray("userHashtagWiseList")
                            val array =
                                Gson().fromJson<ArrayList<TrendingResponse>>(
                                    jsonArray.toString(),
                                    object : TypeToken<ArrayList<TrendingResponse>>() {}.type
                                )

                            if (pageCount == 1) {
                                trendingList.clear()
                            }
                            if (array.isNotEmpty()) {
                                trendingList.addAll(array)
                            } else {
                                isDataFinished = true
                            }
                            setAdapterData()
                        }

                        if (trendingList.size > 0) {
                            if (trendingList.size < paginationCount) {
                                Logger.print("trendingList =======" + trendingList.size)
                            } else {
                                Logger.print("trendingList applyPagination=======" + trendingList.size)
                            }
                        }
                    } else if (code == 0) {
                        if (pageCount == 1) {
                            tv_nodata.visibility = View.VISIBLE
                            isDataFinished = true
                        }
                    }
                }
            })
    }

    private fun setAdapterData() {
        llMainTrending.removeAllViews()
        val followerMap: HashMap<String, String> = HashMap()
        val likeVideoHash: HashMap<String, String> = HashMap()
        val hasLikedMap: HashMap<String, String> = HashMap()
        for (i in trendingList.indices) {
            if (trendingList[i].video_list.isNotEmpty()) {
                Log.d("uservideo", trendingList[i].video_list.toString())
                val v =
                    LayoutInflater.from(context).inflate(R.layout.adapter_trending_new, null, false)
                val tvHashTag = v.findViewById(R.id.tvHashTag) as AppCompatTextView
                val llMainHeader = v.findViewById(R.id.ll_main_header) as LinearLayout
                val rvCat = v.findViewById(R.id.rv_cat) as RecyclerView
                val tvViewAll = v.findViewById(R.id.tv_viewall) as AppCompatTextView

                llMainHeader.setOnClickListener {
                    val intent = Intent(context, HashTagDetailOrTrendingDetail::class.java)
                    intent.putExtra("hashTag", trendingList[i].hashTagName)
                    context!!.startActivity(intent)
                }

                ViewCompat.setNestedScrollingEnabled(rvCat, false)
                val gridLayoutSpacing = GridLayoutSpacing()
                for (j in trendingList[i].video_list.indices) {
                    followerMap[trendingList[i].video_list[j].user_id.toString()] =
                        trendingList[i].video_list[j].is_following.toString()
                    likeVideoHash[trendingList[i].video_list[j].post_id.toString()] =
                        trendingList[i].video_list[j].post_like_count.toString()
                    hasLikedMap[trendingList[i].video_list[j].post_id.toString()] =
                        trendingList[i].video_list[j].has_liked.toString()
                }

                tvHashTag.text = trendingList[i].hashTagName
                if (trendingList[i].hashTagCount != null && !trendingList[i].hashTagCount!!.contentEquals(
                        "null"
                    )
                ) {
                    //    showViewCounts(trendingList[i].hashTagCount!!, tvViewAll
                    Methods.showViewCounts(trendingList[i].hashTagCount!!, tvViewAll)
                }

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
                    adapter = TrendingFinalAdapter(
                        context!!,
                        trendingList[i].video_list,
                        userID,
                        followerMap,
                        likeVideoHash,
                        hasLikedMap,
                        trendingList[i].hashTagName!!,
                        object : TrendingFinalAdapter.ManageClick {
                            override fun managePlayVideoClick(position: Int?) {

                                val intent = Intent(context, VideoDetailActivity::class.java)
                                intent.putExtra("position", position)
                                intent.putExtra("cat_ID", "")
                                intent.putExtra("screen", "trending")
                                intent.putExtra("hashtag", trendingList[i].hashTagName!!)
                                intent.putExtra("searchTxt", "")
                                intent.putExtra("userIDApi", "")
                                intent.putExtra("responseData", trendingList)
                                intent.putExtra("pageNo", 1)
                                // startActivity(intent)
                                startActivityForResult(intent, 1011)
                            }
                        }
                    )
                }
                llMainTrending.addView(v)
                trendingListFinal.addAll(trendingList[i].video_list)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1011 && resultCode == Activity.RESULT_OK) {
            isClickSelfProfile = true
            /* Logger.print("call onActivityResult ===========")

             if (activity is HomeActivity) {
                 val homeActivity = activity as HomeActivity
                 //homeActivity.hideTabBar()
                 homeActivity.setTabUI(false)
                 homeActivity.setSelected()
             }
             val fragment: Fragment
             fragment = MyProfileFragment()
             val ft =
                 fragmentManager!!.beginTransaction()
             val backStateName: String = fragment.javaClass.name

             ft.replace(R.id.container, MyProfileFragment())
             ft.commit()
             isClickSelfProfile =false*/


            /*  if (activity is HomeActivity) {
                  val homeActivity = activity as HomeActivity
                  //homeActivity.hideTabBar()
                  homeActivity.setTabUI(false)
                  homeActivity.setSelected()
              }
              val ft =
                  fragmentManager!!.beginTransaction()
              ft.replace(R.id.container, MyProfileFragment(), "MyProfileFragment")
              ft.commit()*/
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as BaseActivity
    }

    companion object {
        var isClickSelfProfile = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.trending_new_layout, container, false)
    }
}