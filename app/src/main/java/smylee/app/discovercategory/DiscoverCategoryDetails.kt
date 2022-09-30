package smylee.app.discovercategory

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
import kotlinx.android.synthetic.main.activity_discover_category_details.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.engine.GridLayoutSpacing
import smylee.app.model.ForYouResponse
import smylee.app.playvideo.VideoDetailActivity
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*

class DiscoverCategoryDetails : BaseActivity() {

    private var pageCount = 1
    private lateinit var discoverViewModel: DiscoverViewModel
    private var videoList = ArrayList<ForYouResponse>()
    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var discoverViewAllCatAdapter: DiscoverViewAllCatAdapter? = null
    private var llm: GridLayoutManager? = null
    private var currentVisibleCount: Int = 0

    var cmpagination: CustomPaginationDialog? = null

    private var totalItemCount: Int = 0
    private var lastVisibleItem: Int = 0
    private var visibleThreshold = 10
    private var minPublicId: Int = 0
    private var paginationCount: Int = 0

    var catId: String = ""
    private var catName: String = ""
    var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_category_details)

        avatar.setOnClickListener {
            onBackPressed()
        }
        discoverViewModel = ViewModelProviders.of(this).get(DiscoverViewModel::class.java)

        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()
        Logger.print("USER_ID===============$userId")
        val gridLayoutSpacing = GridLayoutSpacing()
        rv_discover_details.addItemDecoration(gridLayoutSpacing)
        rv_discover_details.isNestedScrollingEnabled = false

        if (intent != null) {
            if (intent.getStringExtra("cat_Id") != null) {
                catId = intent.getStringExtra("cat_Id")!!

            }
            if (intent.getStringExtra("cat_name") != null) {
                catName = intent.getStringExtra("cat_name")!!
            }
        }
        page_title.text = catName

        if (!catId.contentEquals("")) {
            Logger.print("cat_Id getDiscoverDetails=========$catId")
            getDiscoverDetails(catId)
        }
    }

    private fun removeLoaderView() {
        if (cmpagination != null && cmpagination!!.isShowing) {
            cmpagination!!.hide()
        }

/*
        if (videoList.size > 0) {

            if (videoList[videoList.size - 1].rowType == Constants.TYPE_ROW_LOADING) {
                videoList.removeAt(videoList.size - 1)
                if (discoverViewAllCatAdapter != null) {
                    rv_discover_details.post {
                        if (discoverViewAllCatAdapter != null)
                            discoverViewAllCatAdapter?.notifyDataSetChanged()
                    }

                }
            }
        }
*/
    }

    private fun applyPagination() {
        rv_discover_details.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            Handler().postDelayed({ getDiscoverDetails(catId) }, Constants.DELAY)
                        }
                    } else {
                        println("........finished data..........")
                    }
                }
            }
        })
    }

    private fun addViewLoadingView() {
        if (cmpagination == null)
            cmpagination = CustomPaginationDialog(this)
        cmpagination!!.show(false)

/*
        if (videoList != null && videoList.size > 0) {
            val review = ForYouResponse(
                -11, "",
                "11", 0, 0, "", "", "", 0,"","","","","","",0,0,0,0F,0,0,0,"","",0,false,0
            )
            review.rowType = Constants.TYPE_ROW_LOADING
            videoList.add(review)
            rv_discover_details.post {
                if (discoverViewAllCatAdapter != null) {
                    discoverViewAllCatAdapter?.notifyDataSetChanged()
                }
            }
        }
*/
    }

    private fun getDiscoverDetails(cat_Id: String) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["category_id"] = cat_Id.trim()
//        if (pageCount > 1) {
//            hashMap["min_public_id"] = minPublicId.toString()
//        }

//        val apiName: String = APIConstants.DISCOVERVIEWALLCATEGORY
        val isProgressShow = pageCount == 1
        discoverViewModel.getDiscoverDetails(this, hashMap, isProgressShow).observe(
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
                            if (jsonObj1.has("min_public_id")) {
                                minPublicId = jsonObj1.getInt("min_public_id")
                            }


                            val jsonArray = jsonObj1.getJSONArray("user_video_list")
                            val array =
                                Gson().fromJson<ArrayList<ForYouResponse>>(jsonArray.toString(),
                                    object : TypeToken<ArrayList<ForYouResponse>>() {}.type
                                )

                            if (pageCount == 1) {
                                videoList.clear()

                            }

                            if (array.isNotEmpty()) {
                                videoList.addAll(array)

                            } else {
                                isDataFinished = true
                            }

                            if (videoList.size > 0) {
                                rv_discover_details.visibility = View.VISIBLE
                                tv_nodata_.visibility = View.GONE
                                isLoading = false
                                setAdapter()
                            } else {
                                rv_discover_details.visibility = View.GONE
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
        if (discoverViewAllCatAdapter == null) {
            llm = GridLayoutManager(this, 3)
            rv_discover_details.layoutManager = llm
            discoverViewAllCatAdapter = DiscoverViewAllCatAdapter(
                this,
                videoList,
                userId, object : DiscoverViewAllCatAdapter.ManageClick {
                    override fun managePlayVideoClick(position: Int?) {
                        val intent =
                            Intent(this@DiscoverCategoryDetails, VideoDetailActivity::class.java)
                        intent.putExtra("position", position)
                        intent.putExtra("cat_ID", catId)
                        intent.putExtra("screen", "discoverDetail")
                        intent.putExtra("hashtag", "")
                        intent.putExtra("responseData", videoList)
                        intent.putExtra("pageNo", pageCount)
                        intent.putExtra("searchTxt", "")
                        intent.putExtra("userIDApi", "")

                        startActivity(intent)
                    }
                }
            )
            rv_discover_details.adapter = discoverViewAllCatAdapter
        } else {
            discoverViewAllCatAdapter?.notifyDataSetChanged()
        }
    }
}