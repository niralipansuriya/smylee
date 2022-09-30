package smylee.app.exploreSearch

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_search_viewmore_videos.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.engine.GridLayoutSpacing
import smylee.app.model.ForYouResponse
import smylee.app.playvideo.VideoDetailActivity
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

class SearchViewmoreVideos : BaseActivity() {

    var followerMap: HashMap<String, String> = HashMap()
    var likeVideoHash: HashMap<String, String> = HashMap()
    private var hasLikedMap: HashMap<String, String> = HashMap()

    lateinit var viewModel: SearchViewModel
    private var videosList = ArrayList<ForYouResponse>()
    private var pageCount = 1
    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var llm: GridLayoutManager? = null
    private var currentVisibleCount: Int = 0
    private var searchResultVideoAdapter: SearchResultVideoAdapter? = null

    private var totalItemCount: Int = 0
    private var lastVisibleItem: Int = 0
    private var visibleThreshold = 10
    var userId: String = ""
    private var paginationCount: Int = 0
    var search: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_viewmore_videos)
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)

        iv_back.setOnClickListener {
            onBackPressed()
        }

        if (intent != null) {
            search = intent.getStringExtra("search_txt")!!
            name.setText(search)
        }
        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()

        val gridLayoutSpacing = GridLayoutSpacing()
        rv_viewmore_videos.addItemDecoration(gridLayoutSpacing)

        getViewMoreVideoListing()
    }

    private fun applyPagination() {
        rv_viewmore_videos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentVisibleCount = llm?.childCount!!
                totalItemCount = llm?.itemCount!!
                lastVisibleItem = llm?.findFirstVisibleItemPosition()!!
                if (!isLoading) {
                    if (!isDataFinished) {
                        if (totalItemCount - currentVisibleCount <= lastVisibleItem + visibleThreshold) {
                            isLoading = true
                            pageCount++
                            Handler().postDelayed({ getViewMoreVideoListing() }, Constants.DELAY)
                        }
                    } else {
                        println("........finished data..........")
                    }
                }
            }
        })
    }

    private fun getViewMoreVideoListing() {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["search_text"] = search
        val isProgressShow: Boolean = pageCount == 1

//        val apiName: String = APIConstants.SEARCHVIEWMOREVIDEOS
        viewModel.getVideoResult(this, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                Logger.print("My SEARCHVIEWMOREVIDEOS Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("pagination_count")) {
                        paginationCount = jsonObject.getInt("pagination_count")
                    }

                    if (jsonObject.has("data")) {
                        val jsonObj1 = jsonObject.getJSONObject("data")
                        val jsonArray = jsonObj1.getJSONArray("user_video_list")

                        val array = Gson().fromJson<ArrayList<ForYouResponse>>(
                            jsonArray.toString(),
                            object : TypeToken<ArrayList<ForYouResponse>>() {}.type
                        )

                        if (pageCount == 1) {
                            videosList.clear()
                            followerMap.clear()
                            hasLikedMap.clear()
                            likeVideoHash.clear()
                        }

                        if (array.isNotEmpty()) {
                            videosList.addAll(array)
                            for (i in videosList.indices) {
                                followerMap[videosList[i].user_id.toString()] =
                                    videosList[i].is_following.toString()
                                likeVideoHash[videosList[i].post_id.toString()] =
                                    videosList[i].post_like_count.toString()
                                hasLikedMap[videosList[i].post_id.toString()] =
                                    videosList[i].has_liked.toString()
                            }
                        } else {
                            isDataFinished = true
                        }

                        if (videosList.size > 0) {
                            rv_viewmore_videos.visibility = View.VISIBLE
                            tv_nodata_videos.visibility = View.GONE
                            isLoading = false
                            setAdapter()
                        } else {
                            rv_viewmore_videos.visibility = View.GONE
                            tv_nodata_videos.visibility = View.VISIBLE
                        }
                    }

                    if (videosList.size > 0) {
                        if (videosList.size < paginationCount) {
                            Logger.print("videosList *******" + videosList.size)
                        } else {
                            Logger.print("videosList *******" + videosList.size)
                            applyPagination()
                        }
                    }
                }

                if (code == 0) {
                    if (pageCount == 1) {
                        videosList.clear()
                        rv_viewmore_videos.visibility = View.GONE
                        tv_nodata_videos.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    /*private fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
            return true
        }
        return false
    }*/

    private fun setAdapter() {
        if (searchResultVideoAdapter == null) {
            Logger.print("followUnFollowAdapter null ==============" + videosList.size)
            llm = GridLayoutManager(this, 3)
            rv_viewmore_videos.layoutManager = llm
            searchResultVideoAdapter = SearchResultVideoAdapter(
                this,
                userId,
                followerMap,
                hasLikedMap,
                likeVideoHash,
                videosList, object : SearchResultVideoAdapter.ManageClick {

                    override fun managePlayVideoClick(position: Int?) {
                        val intent =
                            Intent(this@SearchViewmoreVideos, VideoDetailActivity::class.java)
                        intent.putExtra("position", position)
                        intent.putExtra("cat_ID", "")
                        intent.putExtra("screen", "searchDetail")
                        intent.putExtra("hashtag", "")
                        intent.putExtra("searchTxt", search)
                        intent.putExtra("userIDApi", "")
                        intent.putExtra("responseData", videosList)
                        intent.putExtra("pageNo", pageCount)
                        startActivity(intent)
                    }
                }
            )
            rv_viewmore_videos.adapter = searchResultVideoAdapter
        } else {
            Logger.print("followUnFollowAdapter notifyDataSetChanged ==============" + videosList.size)
            searchResultVideoAdapter?.notifyDataSetChanged()
        }
    }
}