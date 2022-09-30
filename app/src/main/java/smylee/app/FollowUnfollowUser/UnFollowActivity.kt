package smylee.app.FollowUnfollowUser

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
import smylee.app.retrofitclient.APIConstants
import smylee.app.URFeedApplication
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.utils.Utils
import kotlinx.android.synthetic.main.activity_un_follow.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.model.FollowerResponse


class UnFollowActivity : BaseActivity() {

    lateinit var viewModel: FollowUnfollowViewModel
    private var list = ArrayList<FollowerResponse>()
    private var followerIdList = ArrayList<String>()
    private var pageCount = 1
    var followerMap: HashMap<String, String> = HashMap()

    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var llm: LinearLayoutManager? = null
    private var currentVisibleCount: Int = 0
    private var unFollowAdapter: UnFollowAdapter? = null

    private var totalItemCount: Int = 0
    private var lastVisibleItem: Int = 0
    private var visibleThreshold = 10
    private var otherUserId: String = ""
    var searchUnFollow: String = ""
    private var userID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_un_follow)

        viewModel = ViewModelProviders.of(this).get(FollowUnfollowViewModel::class.java)

        avatar.setOnClickListener {
            onBackPressed()
        }

        if (intent != null) {
            otherUserId = intent.getStringExtra("OTHER_USER_ID")!!
        }
        Log.d("OTHER_USER_ID==========", otherUserId)

        userID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()

        Logger.print("USER_ID============$userID")

        search_unfollow.setIconifiedByDefault(false)
        search_unfollow.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchUnFollow = query + ""
                Utils.hideKeyboard(this@UnFollowActivity)
                pageCount = 1
                list.clear()
                getUnFollowList(searchUnFollow.toString().trim())
                return true
            }

            override fun onQueryTextChange(s: String?): Boolean {
                if (search_unfollow.query.isEmpty()) {
                    list.clear()
                    if (unFollowAdapter != null) {
                        println("call notifyDataSetChanged after search unfollow onQueryTextChange")
                        unFollowAdapter!!.notifyDataSetChanged()
                    }
                    searchUnFollow = ""
                    pageCount = 1
                    getUnFollowList("")
                }
                return false
            }
        })
    }

    override fun onResume() {
        super.onResume()
        pageCount = 1
        getUnFollowList("")
        applyPagination()
    }

    private fun applyPagination() {
        rv_unFollow.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            Handler().postDelayed(
                                { getUnFollowList(searchUnFollow) },
                                Constants.DELAY
                            )
                        }
                    } else {
                        println("........finished data..........")
                    }
                }
            }
        })
    }

    private fun getUnFollowList(search: String) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["is_my_following"] = "1"
        if (!search.contentEquals("")) {
            hashMap["search_text"] = search
        }
        if (!otherUserId.contentEquals("")) {
            hashMap["other_user_id"] = otherUserId
        }
        val isShowProgress = pageCount == 1

        val apiName: String = APIConstants.FOLLOWERLISTUSER
        viewModel.FollowerList(this, hashMap, isShowProgress).observe(this, Observer {
            if (it != null) {
                Logger.print("My Follower List Response : $it")
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
                            followerIdList.clear()
                            followerMap.clear()
                        }

                        if (array.isNotEmpty()) {
                            list.addAll(array)
                            for (i in list.indices) {
                                followerIdList.add(list[i].other_user_id)
                                followerMap[list[i].other_user_id] = list[i].is_following.toString()
                            }
                        } else {
                            isDataFinished = true
                        }

                        if (list.size > 0) {
                            rv_unFollow.visibility = View.VISIBLE
                            search_unfollow.visibility = View.VISIBLE
                            tv_nodata_unfollow.visibility = View.GONE
                            isLoading = false
                            setAdapter()
                        } else {
                            rv_unFollow.visibility = View.GONE
                            search_unfollow.visibility = View.GONE
                            tv_nodata_unfollow.visibility = View.VISIBLE
                            //setAdapter()
                        }
                        Log.d("list serch====", "$pageCount>>>$list")
                    }
                }
                if (code == 0) {
                    if (pageCount == 1) {
                        list.clear()
                        rv_unFollow.visibility = View.GONE
                        setAdapter()

                        tv_nodata_unfollow.visibility = View.VISIBLE
                        if (!search.contentEquals("")) {
                            search_unfollow.visibility = View.VISIBLE
                        } else {
                            search_unfollow.visibility = View.GONE
                        }
                    }
                }
            }
        })
    }

    /*private fun FollowUser(IDp: String) {

        var is_follow: String = "1"
        var apiName = ""

        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = is_follow.toString()
        hashMap["other_user_id"] = IDp

        apiName = APIConstants.FOLLOWUNFOLLOWUSER
        viewModel.FollowUnfollow(this, hashMap, apiName).observe(this, Observer {
            if (it != null) {
                Logger.print("FOLLOWUNFOLLOWUSER Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (jsonObject["code"] == 1) {
                }
            }
        })
    }*/

    private fun setAdapter() {
        if (unFollowAdapter == null) {
            Logger.print("unFollowAdapter null==========" + list.size)
            llm = LinearLayoutManager(this)
            rv_unFollow.layoutManager = llm
            unFollowAdapter = UnFollowAdapter(
                false,
                this,
                list,
                followerIdList,
                userID,
                otherUserId,
                followerMap,
                object : UnFollowAdapter.ManageClick {
                    override fun managebuttonclick(
                        IDp: String?,
                        follwerList: ArrayList<FollowerResponse>?,
                        OTHER_USER_ID: String,
                        IS_UNFOLLOW: Boolean
                    ) {
                        Logger.print("follwerList managebuttonclick===============" + follwerList!!.size)
                        if (unFollowAdapter != null) {
                            unFollowAdapter?.notifyDataSetChanged()
                            if (searchUnFollow.contentEquals("")) {
                                if (follwerList.size == 0) {
                                    rv_unFollow.visibility = View.GONE
                                    tv_nodata_unfollow.visibility = View.VISIBLE
                                }
                            }

                            if (IDp != null && !IDp.contentEquals("")) {
                                Logger.print("FOLLOWER_ID=====$IDp")
                                unFollowUser(IDp)
                            }
                        }
                        Logger.print("Follower Ids========" + IDp.toString())
                    }
                })
            rv_unFollow.adapter = unFollowAdapter
        } else {
            Logger.print("unFollowAdapter notifyDataSetChanged==========" + list.size)
            unFollowAdapter?.notifyDataSetChanged()
        }
    }

    private fun unFollowUser(ID: String) {
        val isFollow = "0"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = ID
//        val apiName: String = APIConstants.FOLLOWUNFOLLOWUSER
        viewModel.FollowUnfollow(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("UNFOLLOWER USER Response : $it")
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