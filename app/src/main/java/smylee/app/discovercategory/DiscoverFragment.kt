package smylee.app.discovercategory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_discover.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.engine.GridLayoutSpacing
import smylee.app.home.HomeActivity
import smylee.app.model.ForYouResponse
import smylee.app.model.UserCategoryWise
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.Methods
import smylee.app.utils.SharedPreferenceUtils


class DiscoverFragment : Fragment() {
    private var pageCount = 1
    private lateinit var discoverViewModel: DiscoverViewModel
    private var userCategoryWise = ArrayList<UserCategoryWise>()
    private var categoryVideo = ArrayList<ForYouResponse>()
    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var userId: String = ""
    lateinit var activity: HomeActivity
    private var lastAPICallTime: Long = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as HomeActivity
        Logger.print("onAttach=================")
    }

    override fun onResume() {

        val isAnythingChangedDiscover = SharedPreferenceUtils.getInstance(activity)
            .getBoolanValue(Constants.IS_ANYTHING_CHANGE_DISCOVER, false)

        if (isAnythingChangedDiscover) {
            pageCount = 1
            getDiscoverCategory()

        }
        SharedPreferenceUtils.getInstance(activity)
            .setValue(Constants.IS_ANYTHING_CHANGE_DISCOVER, false)

        super.onResume()
        Logger.print("onResume=================")
//        getDiscoverCategory()
    }


    fun loadData() {
        val isAnythingChangedDiscover = SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_ANYTHING_CHANGE_DISCOVER, false)
        if ((System.currentTimeMillis() - lastAPICallTime) > 1500000 || isAnythingChangedDiscover) {
            pageCount = 1
            getDiscoverCategory()
        }
        SharedPreferenceUtils.getInstance(activity).setValue(Constants.IS_ANYTHING_CHANGE_DISCOVER, false)
    }

    private fun getDiscoverCategory() {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        val isProgressShow = pageCount == 1

        discoverViewModel.getdiscovercategory(activity, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                lastAPICallTime = System.currentTimeMillis()
                Logger.print("Discovercategory response : $it")

                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    //rl_tab.visibility =View.VISIBLE
                    if (jsonObject.has("data")) {
                        val jsonobj1 = jsonObject.getJSONObject("data")
                        val jsonArray = jsonobj1.getJSONArray("userCategoryWiseList")
                        val array = Gson().fromJson<ArrayList<UserCategoryWise>>(jsonArray.toString(),
                                object : TypeToken<ArrayList<UserCategoryWise>>() {}.type)

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
                            nsMainCategory.visibility = View.VISIBLE
                            tv_nodata.visibility = View.GONE
                            isLoading = false
                        } else {
                            nsMainCategory.visibility = View.GONE
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
            if(userCategoryWise[i].category_video_list.isNotEmpty()) {
                Log.d("uservideo", userCategoryWise[i].category_video_list.toString())
                val v = LayoutInflater.from(context).inflate(R.layout.adapter_main_category, null, false)
                val tvCatName = v.findViewById(R.id.tv_cat_name) as AppCompatTextView
                val llViewAll = v.findViewById(R.id.ll_ViewAll) as LinearLayout
                val rvCat = v.findViewById(R.id.rv_cat) as RecyclerView
                val tvViewAll = v.findViewById(R.id.tv_viewall) as AppCompatTextView

                if (userCategoryWise[i].category_count != null && !userCategoryWise[i].category_count!!.contentEquals("null")) {
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discoverViewModel = ViewModelProviders.of(this).get(DiscoverViewModel::class.java)
        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()
        getDiscoverCategory()
    }
}