package smylee.app.FollowUnfollowUser

import smylee.app.ui.base.BaseActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import smylee.app.retrofitclient.APIConstants
import smylee.app.URFeedApplication
import kotlinx.android.synthetic.main.activity_follow.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.model.FollowerResponse
import smylee.app.utils.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

class FollowActivity : BaseActivity() {
    lateinit var viewModel: FollowUnfollowViewModel
    private var list = ArrayList<FollowerResponse>()
    private var pageCount = 1
    private var isDataFinished: Boolean = false
    private var isFromMyProfile: Int = 2
    private var isLoading: Boolean = false
    private var llm: LinearLayoutManager? = null
    private var currentVisibleCount: Int = 0
    private var followUnFollowAdapter: FollowUnFollowAdapter? = null
    var followerMap: HashMap<String, String> = HashMap()
    var cmpagination: CustomPaginationDialog? = null

    private var totalItemCount: Int = 0
    private var lastVisibleItem: Int = 0
    private var visibleThreshold = 10

    private var otherUserID: String = ""
    var strSearch: String = ""
    private var userID: String = ""
    private var sharingLink: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow)

        viewModel = ViewModelProviders.of(this).get(FollowUnfollowViewModel::class.java)

        if (intent != null) {
            otherUserID = intent.getStringExtra("OTHER_USER_ID")!!
            isFromMyProfile = intent.getIntExtra("isFromMyProfile", 2)
        }

        if (isFromMyProfile == 1) {
            tv_nodata_follow.text = resources.getString(R.string.self_follower_msg)
        } else if (isFromMyProfile == 0) {
            tv_nodata_follow.text = resources.getString(R.string.other_follower_msg)

        } else {
            tv_nodata_follow.text = resources.getString(R.string.no_data_found)

        }
        userID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()

        Logger.print("USER_ID============$userID")

        search_follow.setIconifiedByDefault(false)

        search_follow.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                strSearch = query + ""
                Utils.hideKeyboard(this@FollowActivity)
                pageCount = 1
                list.clear()
                getFollowerList(strSearch.toString().trim())
                return true
            }

            override fun onQueryTextChange(s: String?): Boolean {
                if (search_follow.query.isEmpty()) {
                    list.clear()
                    if (followUnFollowAdapter != null) {
                        followUnFollowAdapter!!.notifyDataSetChanged()
                    }
                    strSearch = ""
                    pageCount = 1
                    getFollowerList("")
                }
                return false
            }
        })

        avatar.setOnClickListener {
            onBackPressed()
        }
    }

    private fun applyPagination() {
        rv_follower.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            Handler().postDelayed({ getFollowerList(strSearch) }, Constants.DELAY)
                        }
                    } else {
                        println("........finished data..........")
                    }
                }
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
        pageCount = 1
        followUnFollowAdapter = null
        getFollowerList("")
        applyPagination()
    }

    private fun getFollowerList(search_txt: String) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["is_my_following"] = "0"
        if (!otherUserID.contentEquals("")) {
            hashMap["other_user_id"] = otherUserID
        }

        if (!search_txt.contentEquals("")) {
            hashMap["search_text"] = search_txt
        }
        val isProgressShow: Boolean = pageCount == 1

        val apiName: String = APIConstants.FOLLOWERLISTUSER
        viewModel.FollowerList(this, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                Logger.print("My Follower List Response : $it")

                removeLoaderView()
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")

                if (code == 1) {
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
                            rv_follower.visibility = View.VISIBLE
                            search_follow.visibility = View.VISIBLE

                            tv_nodata_follow.visibility = View.GONE
                            isLoading = false
                            val userID =
                                SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                                    .getStringValue(Constants.USER_ID_PREF, "").toString()
                            setAdapter(userID)
                        } else {
                            rv_follower.visibility = View.GONE
                            search_follow.visibility = View.GONE
                            //tv_nodata_follow.visibility = View.VISIBLE
                        }
                    }
                }

                if (code == 0) {
                    if (pageCount == 1) {
                        list.clear()
                        rv_follower.visibility = View.GONE
                        val userID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                            .getStringValue(Constants.USER_ID_PREF, "").toString()
                        setAdapter(userID)

                        setAdapter(userID)
                        tv_nodata_follow.visibility = View.VISIBLE


                        if (!strSearch.contentEquals("")) {
                            search_follow.visibility = View.VISIBLE
                        } else {
                            search_follow.visibility = View.GONE
                        }
                    }
                }
            }
        })
    }

    private fun setAdapter(USERID: String) {
        if (followUnFollowAdapter == null) {
            Logger.print("followUnFollowAdapter null ==============" + list.size)
            llm = LinearLayoutManager(this)
            rv_follower.layoutManager = llm
            followUnFollowAdapter = FollowUnFollowAdapter(false, this, list, USERID, followerMap,
                object : FollowUnFollowAdapter.ManageClick {
                    override fun managebuttonclick(IDp: String?, IS_UNFOLLOW: Boolean) {
                        Logger.print("FOLLOWER_ID IN FOLLOW ACTIVITY before execute========" + IDp.toString())

                        if (!IS_UNFOLLOW) {
                            if (IDp != null && !IDp.contentEquals("")) {
                                Logger.print("FOLLOWER_ID IN FOLLOW ACTIVITY========$IDp")
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
            rv_follower.adapter = followUnFollowAdapter
        } else {
            Logger.print("followUnFollowAdapter notifyDataSetChanged ==============" + list.size)
            followUnFollowAdapter?.notifyDataSetChanged()
        }
    }

    private fun unFollowUser(IDp: String) {
        val isFollow = "0"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = IDp

        val apiName: String = APIConstants.FOLLOWUNFOLLOWUSER
        viewModel.FollowUnfollow(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("Unfollowuser Whom im following Response : $it")
                SharedPreferenceUtils.getInstance(applicationContext)
                    .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                SharedPreferenceUtils.getInstance(applicationContext)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                SharedPreferenceUtils.getInstance(this)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)

            }
        })
    }

    private fun followUser(IDp: String) {
        val isFollow = "1"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = IDp
        val apiName: String = APIConstants.FOLLOWUNFOLLOWUSER
        viewModel.FollowUnfollow(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("FOLLOWUNFOLLOWUSER Response : $it")
                SharedPreferenceUtils.getInstance(applicationContext)
                    .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                SharedPreferenceUtils.getInstance(applicationContext)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                SharedPreferenceUtils.getInstance(this)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)


            }
        })
    }
}