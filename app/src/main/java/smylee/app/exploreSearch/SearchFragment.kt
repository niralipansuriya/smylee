//package smylee.app.exploreSearch
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.appcompat.widget.SearchView
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentManager
//import androidx.fragment.app.FragmentTransaction
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProviders
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import kotlinx.android.synthetic.main.fragment_search.*
//import org.json.JSONObject
//import smylee.app.R
//import smylee.app.URFeedApplication
//import smylee.app.discovercategory.ExploreFragment
//import smylee.app.engine.GridLayoutSpacing
//import smylee.app.home.HomeActivity
//import smylee.app.model.ForYouResponse
//import smylee.app.model.SearchPeopleResponse
//import smylee.app.utils.Constants
//import smylee.app.utils.Logger
//import smylee.app.utils.SharedPreferenceUtils
//import smylee.app.utils.Utils
//
//
//class SearchFragment : Fragment() {
//    private var searchResultAdapter: SearchResultVideoAdapter? = null
//    lateinit var activity: HomeActivity
//    var strSearch: String = ""
//    lateinit var viewModel: SearchViewModel
//    private var searchVideoList = java.util.ArrayList<ForYouResponse>()
//    private var searchPeopleList = java.util.ArrayList<SearchPeopleResponse>()
//    private var pageCount = 1
//    private var isDataFinished: Boolean = false
//    private var isLoading: Boolean = false
//    private var llm: LinearLayoutManager? = null
//    var followerMap: HashMap<String, String> = HashMap()
//    var likeVideoHash: HashMap<String, String> = HashMap()
//    var hasLikedMap: HashMap<String, String> = HashMap()
//
//    private var searchTitle: String = ""
//    private var llmVideo: GridLayoutManager? = null
//    private var searchResultPeopleAdapter: SearchResultPeopleAdapter? = null
//    private var userID: String = ""
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_search, container, false)
//    }
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        activity = context as HomeActivity
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
//        ivBack.setOnClickListener {
//            val manager: FragmentManager = getActivity()!!.supportFragmentManager
//            val trans: FragmentTransaction = manager.beginTransaction()
//            trans.remove(this)
//            trans.commit()
//            manager.popBackStack()
//        }
//
//        userID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
//            .getStringValue(Constants.USER_ID_PREF, "").toString()
//
//        search.setIconifiedByDefault(false)
//
//        search.isFocusable = true
//        Utils.showKeyboard(view, context!!)
//        val gridLayoutSpacing = GridLayoutSpacing()
//        rvSearchResult.addItemDecoration(gridLayoutSpacing)
//        search.setOnQueryTextFocusChangeListener { _, _ -> }
//
//        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String): Boolean {
//                strSearch = query + ""
//                Utils.hideKeyboard(getActivity()!!)
//
//                pageCount = 1
//                getSearchResults(strSearch)
//                return true
//            }
//
//            override fun onQueryTextChange(s: String?): Boolean {
//                if (search.query.isEmpty()) {
//                    tvPeopleResult.visibility = View.GONE
//                    tvSearchResult.visibility = View.GONE
//                    tv_viewmore_videos.visibility = View.GONE
//                    tv_viewmore_people.visibility = View.GONE
//                    searchPeopleList.clear()
//                    searchVideoList.clear()
//                    if (searchResultAdapter != null) {
//                        searchResultAdapter!!.notifyDataSetChanged()
//                    }
//                    if (searchResultPeopleAdapter != null) {
//                        searchResultPeopleAdapter!!.notifyDataSetChanged()
//                    }
//                    strSearch = ""
//                    pageCount = 1
//                }
//                return false
//            }
//        })
//    }
//
//    override fun onResume() {
//        super.onResume()
//        pageCount = 1
//        searchResultPeopleAdapter = null
//        searchResultAdapter = null
//        getSearchResults(strSearch)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Logger.print("onDestroy===================")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        Logger.print("onStop===================")
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        ExploreFragment.isSearchAdded = false
//        Logger.print("onDetach===================")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Logger.print("onPause===================")
//    }
//
//    private fun getSearchResults(STR_SEARCH: String) {
//        val hashMap = HashMap<String, String>()
//        hashMap["page_no"] = pageCount.toString()
//        hashMap["search_text"] = STR_SEARCH
//        searchTitle = STR_SEARCH
////        val apiName: String = APIConstants.SEARCHVIDEOLIST
//        val isProgressShow: Boolean = pageCount == 1
//
//        viewModel.getSearchResults(activity, hashMap, isProgressShow).observe(this, Observer {
//            if (it != null) {
//                search.clearFocus()
//                Utils.hideKeyboardActivity(activity)
//                Logger.print("SEARCH RESULTS response : $it")
//
//                val jsonObject = JSONObject(it.toString())
//                val code = jsonObject.getInt("code")
//                if (code == 1) {
//                    if (jsonObject.has("data")) {
//                        val jsonobj1 = jsonObject.getJSONObject("data")
//                        val jsonArray = jsonobj1.getJSONArray("user_video_list")
//                        val jsonArrayPeople = jsonobj1.getJSONArray("user_list")
//
//                        val array = Gson().fromJson<ArrayList<ForYouResponse>>(jsonArray.toString(),
//                                object : TypeToken<ArrayList<ForYouResponse>>() {}.type)
//
//                        val arrayPeople = Gson().fromJson<ArrayList<SearchPeopleResponse>>(jsonArrayPeople.toString(),
//                                object : TypeToken<ArrayList<SearchPeopleResponse>>() {}.type)
//                        Logger.print("array ********************$array")
//                        Logger.print("arrayPeople ********************$arrayPeople")
//
//                        val userID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
//                                .getStringValue(Constants.USER_ID_PREF, "").toString()
//                        if (pageCount == 1) {
//                            searchVideoList.clear()
//                            searchPeopleList.clear()
//                            followerMap.clear()
//                            hasLikedMap.clear()
//                            likeVideoHash.clear()
//                        }
//
//                        if (arrayPeople.isNotEmpty()) {
//                            searchPeopleList.addAll(arrayPeople)
//                            for (i in searchPeopleList.indices) {
//                                followerMap[searchPeopleList[i].user_id.toString()] = searchPeopleList[i].is_following.toString()
//                            }
//                        } else {
//                            isDataFinished = true
//                        }
//                        if (array.isNotEmpty()) {
//                            searchVideoList.addAll(array)
//                            for (i in searchVideoList.indices) {
//                                followerMap[searchVideoList[i].user_id.toString()] = searchVideoList[i].is_following.toString()
//                                likeVideoHash[searchVideoList[i].post_id.toString()] = searchVideoList[i].post_like_count.toString()
//                                hasLikedMap[searchVideoList[i].post_id.toString()] = searchVideoList[i].has_liked.toString()
//                            }
//                        } else {
//                            isDataFinished = true
//                        }
//
//                        if (searchPeopleList.size > 0) {
//                            tvPeopleResult.visibility = View.VISIBLE
//                            rvPeopleResult.visibility = View.VISIBLE
//
//                            if (searchPeopleList.size == 6) {
//                                tv_viewmore_people.visibility = View.VISIBLE
//                            } else {
//                                tv_viewmore_people.visibility = View.INVISIBLE
//                            }
//
//                            tv_viewmore_people.setOnClickListener {
//                                activity.isAttachFragmentAgain = false
//                                val intent = Intent(activity, SearchViewmorePeople::class.java)
//                                intent.putExtra("search_txt", STR_SEARCH)
//                                startActivity(intent)
//                            }
//                            isLoading = false
//                            setAdapterPeople(userID)
//                        } else {
//                            tvPeopleResult.visibility = View.GONE
//                            tv_viewmore_people.visibility = View.GONE
//                            rvPeopleResult.visibility = View.GONE
//                        }
//                        if (searchVideoList.size > 0) {
//                            rvSearchResult.visibility = View.VISIBLE
//                            tvSearchResult.visibility = View.VISIBLE
//
//                            if (searchVideoList.size == 6) {
//                                tv_viewmore_videos.visibility = View.VISIBLE
//                            } else {
//                                tv_viewmore_videos.visibility = View.INVISIBLE
//                            }
//
//                            tv_viewmore_videos.setOnClickListener {
//                                activity.isAttachFragmentAgain = false
//                                val intent = Intent(activity, SearchViewmoreVideos::class.java)
//                                intent.putExtra("search_txt", STR_SEARCH)
//                                startActivity(intent)
//                            }
//                            isLoading = false
//                            setAdapter(userID)
//                        } else {
//                            if (searchVideoList.size == 0 && searchPeopleList.size == 0) {
//                                no_data_found.visibility = View.VISIBLE
//                                svMainClubHours.visibility = View.GONE
//                            } else {
//                                no_data_found.visibility = View.GONE
//                                svMainClubHours.visibility = View.VISIBLE
//                            }
//
//                            tvSearchResult.visibility = View.GONE
//                            tv_viewmore_videos.visibility = View.GONE
//                            rvSearchResult.visibility = View.GONE
//                        }
//                        Logger.print("serchVideoList===========" + searchVideoList.size)
//                        Logger.print("searchPeople===========" + searchPeopleList.size)
//                    }
//                }
//            }
//        })
//    }
//
//    private fun setAdapterPeople(userID: String) {
//        if (searchResultPeopleAdapter == null) {
//            llm = LinearLayoutManager(context)
//            rvPeopleResult.layoutManager = llm
//            searchResultPeopleAdapter = SearchResultPeopleAdapter(activity, userID, searchPeopleList, followerMap,
//                object : SearchResultPeopleAdapter.ManageClick {
//                    override fun onClick(User_Id: String?, IS_UNFOLLOW: Boolean) {
//                        if (User_Id != null && !User_Id.contentEquals("")) {
//                            if (IS_UNFOLLOW) {
//                                unFollowUser(User_Id)
//                            }
//                            if (!IS_UNFOLLOW) {
//                                followUser(User_Id)
//                            }
//                        }
//                    }
//                }
//            )
//            rvPeopleResult.adapter = searchResultPeopleAdapter
//        } else {
//            searchResultPeopleAdapter?.notifyDataSetChanged()
//        }
//    }
//
//    private fun followUser(User_Id: String) {
//        val isFollow = "1"
//        val hashMap = HashMap<String, String>()
//        hashMap["is_follow"] = isFollow
//        hashMap["other_user_id"] = User_Id
//
//        viewModel.FollowUnfollow(activity, hashMap).observe(this, Observer {
//            if (it != null) {
//                Logger.print("Search FollowUser================ $it")
//            }
//        })
//    }
//
//    private fun unFollowUser(User_Id: String) {
//        val isFollow = "0"
//        val hashMap = HashMap<String, String>()
//        hashMap["is_follow"] = isFollow
//        hashMap["other_user_id"] = User_Id
//
//        viewModel.FollowUnfollow(activity, hashMap).observe(this, Observer {
//            if (it != null) {
//                Logger.print("Search UnFollowUser================ $it")
//            }
//        })
//    }
//
//    private fun setAdapter(userID: String) {
//        if (searchResultAdapter == null) {
//            llmVideo = GridLayoutManager(context, 3)
//            rvSearchResult.layoutManager = llmVideo
//            searchResultAdapter = SearchResultVideoAdapter(activity,userID,followerMap,hasLikedMap,likeVideoHash,searchVideoList)
//            rvSearchResult.adapter = searchResultAdapter
//        } else {
//            searchResultAdapter?.notifyDataSetChanged()
//        }
//    }
//
//    companion object {
//        @JvmStatic
//        fun newInstance() = SearchFragment().apply {}
//    }
//}