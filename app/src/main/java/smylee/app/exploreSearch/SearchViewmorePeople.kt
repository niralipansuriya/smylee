package smylee.app.exploreSearch

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_search_viewmore_people.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.model.SearchPeopleResponse
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

class SearchViewmorePeople : BaseActivity() {
    lateinit var viewModel: SearchViewModel
    private var peopleList = ArrayList<SearchPeopleResponse>()
    private var pageCount = 1
    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var llm: LinearLayoutManager? = null
    private var currentVisibleCount: Int = 0
    var followerMap: HashMap<String, String> = HashMap()
    private var searchResultPeopleAdapter: SearchResultPeopleAdapter? = null

    private var totalItemCount: Int = 0
    private var lastVisibleItem: Int = 0
    private var visibleThreshold = 10
    private var userID: String = ""

    var search: String = ""
    private var paginationCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_viewmore_people)

        avatar.setOnClickListener {
            onBackPressed()
        }
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)

        if (intent != null) {
            search = intent.getStringExtra("search_txt")!!
            name.setText(search)
        }

        //getSearchViewMorePeople()

        userID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()
        Logger.print("USER_ID============$userID")
    }

    /*private fun loadFragment(fragment: Fragment?): Boolean {
        //switching fragment
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
            return true
        }
        return false
    }*/

    override fun onResume() {
        super.onResume()

        pageCount = 1
        searchResultPeopleAdapter = null
        getSearchViewMorePeople()

    }

    private fun getSearchViewMorePeople() {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["search_text"] = search

        val isProgressShow: Boolean = pageCount == 1
//        val apiName: String = APIConstants.SEARCHVIEWMOREPEOPLE
        viewModel.getPeopleResult(this, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                Logger.print("My SEARCHVIEWMOREPEOPLE Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("pagination_count")) {
                        paginationCount = jsonObject.getInt("pagination_count")
                    }
                    if (jsonObject.has("data")) {
                        val jsonobj1 = jsonObject.getJSONObject("data")
                        val jsonArray = jsonobj1.getJSONArray("user_list")

                        val array =
                            Gson().fromJson<ArrayList<SearchPeopleResponse>>(jsonArray.toString(),
                                object : TypeToken<ArrayList<SearchPeopleResponse>>() {}.type
                            )

                        if (pageCount == 1) {
                            peopleList.clear()
                            followerMap.clear()
                        }

                        if (array.isNotEmpty()) {
                            peopleList.addAll(array)
                            for (i in peopleList.indices) {
                                followerMap[peopleList[i].user_id.toString()] =
                                    peopleList[i].is_following.toString()
                            }
                        } else {
                            isDataFinished = true
                        }

                        if (peopleList.size > 0) {
                            rv_viewmore_people.visibility = View.VISIBLE
                            tv_nodata_people.visibility = View.GONE
                            isLoading = false
                            var userID =
                                SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                                    .getStringValue(Constants.USER_ID_PREF, "").toString()

                            setAdapter(userID)
                        } else {
                            rv_viewmore_people.visibility = View.GONE
                            tv_nodata_people.visibility = View.VISIBLE
                        }
                    }

                    if (peopleList.size > 0) {
                        if (peopleList.size < paginationCount) {
                            Logger.print("peopleList *******" + peopleList.size)
                        } else {
                            Logger.print("peopleList *******" + peopleList.size)
                            applyPagination()
                        }
                    }
                }

                if (code == 0) {
                    if (pageCount == 1) {
                        peopleList.clear()
                        rv_viewmore_people.visibility = View.GONE
                        tv_nodata_people.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun setAdapter(userID: String) {
        if (searchResultPeopleAdapter == null) {
            Logger.print("searchResultPeopleAdapter null ==============" + peopleList.size)
            llm = LinearLayoutManager(this)
            rv_viewmore_people.layoutManager = llm
            searchResultPeopleAdapter = SearchResultPeopleAdapter(this, userID,
                peopleList, followerMap, object : SearchResultPeopleAdapter.ManageClick {
                    override fun onClick(User_Id: String?, IS_UNFOLLOW: Boolean) {
                        Logger.print("User_Id ===========$User_Id")
                        if (IS_UNFOLLOW) {
                            unFollowUser(User_Id)
                        }
                        if (!IS_UNFOLLOW) {
                            followUser(User_Id)
                        }
                    }
                })
            rv_viewmore_people.adapter = searchResultPeopleAdapter
        } else {
            Logger.print("searchResultPeopleAdapter notifyDataSetChanged ==============" + peopleList.size)
            searchResultPeopleAdapter?.notifyDataSetChanged()
        }
    }

    private fun followUser(userId: String?) {
        val isFollow = "1"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = userId.toString()

//        val apiName: String = APIConstants.FOLLOWUNFOLLOWUSER
        viewModel.FollowUnfollow(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("Search FollowUser================ $it")
                SharedPreferenceUtils.getInstance(this)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                SharedPreferenceUtils.getInstance(this)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)

                /*val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (jsonObject["code"] == 1) {
                }*/
            }
        })
    }

    private fun unFollowUser(userId: String?) {
        val isFollow = "0"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = userId.toString()

//        val apiName: String = APIConstants.FOLLOWUNFOLLOWUSER
        viewModel.FollowUnfollow(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("Search UnFollowUser================ $it")
                SharedPreferenceUtils.getInstance(this)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                SharedPreferenceUtils.getInstance(this)
                    .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)

                /*val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (jsonObject["code"] == 1) {
                }*/
            }
        })
    }

    private fun applyPagination() {
        rv_viewmore_people.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            Handler().postDelayed({ getSearchViewMorePeople() }, Constants.DELAY)
                        }
                    } else {
                        println("........finished data..........")
                    }
                }
            }
        })
    }
}