package smylee.app.Profile

import smylee.app.ui.base.BaseActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import smylee.app.FollowUnfollowUser.FollowUnfollowViewModel
import smylee.app.model.FollowerResponse
import smylee.app.URFeedApplication
import kotlinx.android.synthetic.main.activity_unfollow_other_profile.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.utils.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.indices
import kotlin.collections.isNotEmpty
import kotlin.collections.set

class UnfollowActivityOtherProfile : BaseActivity() {

    lateinit var viewModel: FollowUnfollowViewModel
    private var list = ArrayList<FollowerResponse>()
//    private var FollowerIdList = ArrayList<String>()
    private var pageCount = 1
    var followerMap: HashMap<String, String> = HashMap()
    var cmpagination: CustomPaginationDialog? = null

    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var llm: LinearLayoutManager? = null
    private var currentVisibleCount: Int = 0

    var STR_SEARCH: String = ""
    private var totalItemCount: Int = 0

    private var lastVisibleItem: Int = 0

    private var visibleThreshold = 10
    var OTHER_USER_ID: String = ""
    var SEARCH_Unfollow: String = ""
    var USER_ID: String = ""
    private var UnfollowAdapterOtherProfile: UnfollowAdapterOtherProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unfollow_other_profile)

        viewModel = ViewModelProviders.of(this).get(FollowUnfollowViewModel::class.java)
        if (intent != null) {
            OTHER_USER_ID = intent.getStringExtra("OTHER_USER_ID")!!
        }
        Log.d("OTHER_USER_ID==========", OTHER_USER_ID)

        USER_ID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()
        Logger.print("USER_ID============$USER_ID")

        avatar_unfollow_other_profile.setOnClickListener {
            onBackPressed()
        }
        search_unfollow_other_profile.setIconifiedByDefault(false)

        search_unfollow_other_profile.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                SEARCH_Unfollow = query + ""
                Utils.hideKeyboard(this@UnfollowActivityOtherProfile)
                pageCount = 1
                list.clear()
                getFollowerList(SEARCH_Unfollow.trim())
                return true
            }

            override fun onQueryTextChange(s: String?): Boolean {
                if (search_unfollow_other_profile.query.isEmpty()) {
                    list.clear()
                    if (UnfollowAdapterOtherProfile != null) {
                        println("call notifyDataSetChanged after search unfollow onQueryTextChange")
                        UnfollowAdapterOtherProfile!!.notifyDataSetChanged()
                    }
                    SEARCH_Unfollow = ""
                    pageCount = 1
                    getFollowerList("")
                }
                return false
            }
        })
    }

    private fun removeLoaderView() {
        if (cmpagination != null && cmpagination!!.isShowing) {
            cmpagination!!.hide()
        }
    }

    private fun addViewLoadingView() {
        if (cmpagination == null)
            cmpagination = CustomPaginationDialog(this)
        cmpagination!!.show(false)
    }

    override fun onResume() {
        super.onResume()
        //for guest flow
        pageCount = 1
        UnfollowAdapterOtherProfile = null
        USER_ID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()

        getFollowerList("")
        applyPagination()
    }

    private fun applyPagination() {
        rv_unFollow_other_user_profile.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
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
                            Handler().postDelayed({getFollowerList(STR_SEARCH)}, Constants.DELAY)
                        }
                    } else {
                        println("........finished data..........")
                    }
                }
            }
        })
    }

    private fun getFollowerList(search_txt: String) {
//        var apiName = ""
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["is_my_following"] = "1"
        if (!OTHER_USER_ID.contentEquals("")) {
            hashMap["other_user_id"] = OTHER_USER_ID
        }
        if (!search_txt.contentEquals("")) {
            hashMap["search_text"] = search_txt
        }

        val isProgressShow: Boolean = pageCount == 1
        /*if (Is_progress) {
        } else {
            isProgressShow = false
        }*/
        viewModel.FollowerList(this, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                removeLoaderView()
                Logger.print("My Follower List Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")

                if (code == 1) {
                    //  list.clear()
                    if (jsonObject.has("data")) {
                        val array = Gson().fromJson<ArrayList<FollowerResponse>>(
                            jsonObject.getJSONArray("data").toString(),
                            object : TypeToken<ArrayList<FollowerResponse>>() {}.type
                        )
                        if (pageCount == 1) {
                            list.clear()
                            followerMap.clear()
                        }

                        if (array.isNotEmpty()) {
                            list.addAll(array)
                            for (i in list.indices) {
                                followerMap[list[i].other_user_id] = list[i].is_following.toString()
                            }
                        } else {
                            isDataFinished = true
                        }

                        if (list.size > 0) {
                            rv_unFollow_other_user_profile.visibility = View.VISIBLE
                            search_unfollow_other_profile.visibility = View.VISIBLE

                            tv_nodata_unfollow_other_user.visibility = View.GONE
                            isLoading = false
                            val USERID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                                    .getStringValue(Constants.USER_ID_PREF, "").toString()

                            setAdapter(USERID)
                        } else {
                            rv_unFollow_other_user_profile.visibility = View.GONE
                            search_unfollow_other_profile.visibility = View.GONE
                            //tv_nodata_follow.visibility = View.VISIBLE
                        }
                    }
                }

                if (code == 0) {
                    if (pageCount == 1) {
//                        list.clear()
//                        setAdapter()
                        rv_unFollow_other_user_profile.visibility = View.GONE
                        tv_nodata_unfollow_other_user.visibility = View.VISIBLE

                        if (!STR_SEARCH.contentEquals("")) {
                            search_unfollow_other_profile.visibility = View.VISIBLE
                        } else {
                            search_unfollow_other_profile.visibility = View.GONE
                        }
                    }
                }
            }
        })
    }

    private fun setAdapter(USERID: String) {
        if (UnfollowAdapterOtherProfile == null) {
            Logger.print("UnfollowAdapterOtherProfile null ==============" + list.size)
            llm = LinearLayoutManager(this)
            rv_unFollow_other_user_profile.layoutManager = llm
            UnfollowAdapterOtherProfile = UnfollowAdapterOtherProfile(false,this,list,USERID,followerMap,
                object : UnfollowAdapterOtherProfile.ManageClick {
                    override fun managebuttonclick(IDp: String?, IS_UNFOLLOW: Boolean) {
                        Logger.print("UnfollowAdapterOtherProfile before execute========" + IDp.toString())

                        if (!IS_UNFOLLOW) {
                            if (IDp != null && !IDp.contentEquals("")) {
                                Logger.print("UnfollowAdapterOtherProfile========$IDp")
                                followUser(IDp)
                            }
                        }

                        if (IS_UNFOLLOW) {
                            if (IDp != null && !IDp.contentEquals("")) {
                                unFollowUser(IDp)
                            }
                        }
                    }
                })
            rv_unFollow_other_user_profile.adapter = UnfollowAdapterOtherProfile
        } else {
            Logger.print("followUnFollowAdapter notifyDataSetChanged ==============" + list.size)
            UnfollowAdapterOtherProfile?.notifyDataSetChanged()
        }
    }

    private fun followUser(iDp: String) {
        val isFollow = "1"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = iDp
        Logger.print("other_user_id=============$iDp")

        viewModel.FollowUnfollow(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("FOLLOWUNFOLLOWUSER Response : $it")
                SharedPreferenceUtils.getInstance(applicationContext)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                SharedPreferenceUtils.getInstance(this)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)

            }
        })
    }

    private fun unFollowUser(iDp: String) {
        val isFollow = "0"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = iDp
        viewModel.FollowUnfollow(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("Unfollowuser Whom im following Response : $it")
                SharedPreferenceUtils.getInstance(applicationContext)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                SharedPreferenceUtils.getInstance(this)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)

            }
        })
    }
}