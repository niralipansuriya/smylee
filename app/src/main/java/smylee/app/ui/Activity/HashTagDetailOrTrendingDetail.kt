package smylee.app.ui.Activity

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
import kotlinx.android.synthetic.main.activity_hashtag_detail_or_trending_detail.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.discovercategory.HashTagDetailAdapter
import smylee.app.engine.GridLayoutSpacing
import smylee.app.model.ForYouResponse
import smylee.app.playvideo.VideoDetailActivity
import smylee.app.trending.TrendingViewModel
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*

class HashTagDetailOrTrendingDetail : BaseActivity() {
    lateinit var viewModel: TrendingViewModel
    private var totalItemCount: Int = 0
    private var lastVisibleItem: Int = 0
    private var visibleThreshold = 10
//    private var minPublicId: Int = 0
    private var paginationCount: Int = 0
    private var llm: GridLayoutManager? = null
    private var videoList = ArrayList<ForYouResponse>()
    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var hashTagDetailAdapter: HashTagDetailAdapter? = null
    private var currentVisibleCount: Int = 0
    private var pageCount = 1
    var userId: String = ""
    var hashTagName: String = ""
    private var cmPagination: CustomPaginationDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hashtag_detail_or_trending_detail)
        viewModel = ViewModelProviders.of(this).get(TrendingViewModel::class.java)
        avatar.setOnClickListener {
            onBackPressed()
        }

        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!).getStringValue(Constants.USER_ID_PREF, "").toString()
        Logger.print("USER_ID===============$userId")
        val gridLayoutSpacing = GridLayoutSpacing()
        rvhashtagDetails.addItemDecoration(gridLayoutSpacing)

        if (intent != null) {
            if (intent.getStringExtra("hashTag") != null) {
                hashTagName = intent.getStringExtra("hashTag")!!
                page_title.text = hashTagName
            }
        }

        if (!hashTagName.contentEquals("")) {
            Logger.print("hashTagName trendingList=========$hashTagName")
            hashTagDetail(hashTagName)
        }
    }

    private fun removeLoaderView() {
        if (cmPagination != null && cmPagination!!.isShowing) {
            cmPagination!!.hide()
        }
    }

    private fun addViewLoadingView() {
        if (cmPagination == null)
            cmPagination = CustomPaginationDialog(this)
        cmPagination!!.show(false)
    }

    private fun applyPagination() {
        rvhashtagDetails.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

                            if (dy > 0) {
                                addViewLoadingView()
                            }
                            Handler().postDelayed({ hashTagDetail(hashTagName) }, Constants.DELAY)
                        }
                    } else {
                        println("........finished data..........")
                    }
                }
            }
        })
    }

    private fun hashTagDetail(hashTagName: String) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["hashtag"] = hashTagName.trim()

        val isProgressShow = pageCount == 1
        viewModel.observeHashTagDetail(this, hashMap, isProgressShow).observe(
            this, Observer {
                if (it != null) {
                    Logger.print("getDiscoverDetails view all response : $it")
                    removeLoaderView()

                    val jsonObject = JSONObject(it.toString())
                    val code = jsonObject.getInt("code")
                    if (code == 1) {
                        /*if (pageCount == 1) {
                            Utils.showToastMessage(
                                this,
                                jsonObject["message"].toString()
                            )
                        }*/

                        if (jsonObject.has("data")) {
                            val jsonObj1 = jsonObject.getJSONObject("data")

                            val jsonArray = jsonObj1.getJSONArray("user_video_list")
                            val array = Gson().fromJson<ArrayList<ForYouResponse>>(jsonArray.toString(),
                                    object : TypeToken<ArrayList<ForYouResponse>>() {}.type)

                            if (pageCount == 1) {
                                videoList.clear()

                            }

                            if (array.isNotEmpty()) {
                                videoList.addAll(array)

                            } else {
                                isDataFinished = true
                            }

                            if (videoList.size > 0) {
                                rvhashtagDetails.visibility = View.VISIBLE
                                tv_nodata_.visibility = View.GONE
                                isLoading = false
                                setAdapter()
                            } else {
                                rvhashtagDetails.visibility = View.GONE
                                tv_nodata_.visibility = View.VISIBLE
                            }
                        }

                        if (jsonObject.has("pagination_count")) {
                            paginationCount = jsonObject.getInt("pagination_count")
                        }

                        if (videoList.size > 0) {
                            if (videoList.size < paginationCount) {
                                Logger.print("userCategoryWise *******" + videoList.size)
                            } else {
                                Logger.print("ApplyPagination *******" + videoList.size)
                                applyPagination()
                            }
                        }
                    } else {
                        if (pageCount == 1) {
                            Utils.showToastMessage(this, jsonObject["message"].toString())
                        }
                    }
                }
            }
        )
    }

    private fun setAdapter() {
        if (hashTagDetailAdapter == null) {
            llm = GridLayoutManager(this, 3)
            rvhashtagDetails.layoutManager = llm
            hashTagDetailAdapter = HashTagDetailAdapter(
                this,
                videoList,
                userId,
                object : HashTagDetailAdapter.ManageClick {
                    override fun managePlayVideoClick(position: Int?) {
                        val intent = Intent(
                            this@HashTagDetailOrTrendingDetail,
                            VideoDetailActivity::class.java
                        )
                        intent.putExtra("position", position)
                        intent.putExtra("cat_ID", "")
                        intent.putExtra("screen", "hashtagDetail")
                        intent.putExtra("hashtag", hashTagName)
                        intent.putExtra("searchTxt", "")
                        intent.putExtra("userIDApi", "")
                        intent.putExtra("responseData", videoList)
                        intent.putExtra("pageNo", pageCount)
                        startActivity(intent)
                    }
                }
            )
            rvhashtagDetails.adapter = hashTagDetailAdapter
        } else {
            hashTagDetailAdapter?.notifyDataSetChanged()
        }
    }

}