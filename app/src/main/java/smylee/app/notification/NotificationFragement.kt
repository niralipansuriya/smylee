package smylee.app.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_notification_fragement.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.home.HomeActivity
import smylee.app.login.LoginActivity
import smylee.app.model.NotificationResponse
import smylee.app.utils.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set


class NotificationFragement : Fragment() {
    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var llm: LinearLayoutManager? = null
    private var currentVisibleCount: Int = 0
    lateinit var activity: HomeActivity
    private var list = ArrayList<NotificationResponse>()
    private var cmpagination: CustomPaginationIndicator? = null
    var pageCount = 1

    private var totalItemCount: Int = 0
    private var lastVisibleItem: Int = 0
    private var isLogin: Boolean = false

    private var visibleThreshold = 10
    lateinit var viewModel: NotificationViewModel
    private var notificationAdapter: NotificationAdapter? = null
    private var userID: String = ""
    private var paginationCount: Int = 0

    private var lastAPICallTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NotificationViewModel::class.java)

        userID = SharedPreferenceUtils.getInstance(URFeedApplication.context!!).getStringValue(Constants.USER_ID_PREF, "").toString()
        isLogin = SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_LOGIN, false)

        btnSignUp.setOnClickListener {
            activity.isCallLogin = true
            val intent = Intent(getActivity(), LoginActivity::class.java)
            intent.putExtra("screen_name", "notification")
            activity.startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
        }
    }

    fun removeData() {
        list.clear()
        notificationAdapter?.notifyDataSetChanged()
        notificationAdapter = null
        pageCount = 1
    }

    fun loadData() {
        Logger.print("loadData!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        if (ll_with_login != null) {
            Logger.print("ll_with_login not null!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            loadNotifications()
        } else {
            Handler().postDelayed({
                Logger.print("ll_with_login postDelayed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                if (ll_with_login != null) {
                    Logger.print("ll_with_login postDelayed not null!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    loadNotifications()
                }
            }, 300)
        }
    }

    override fun onResume() {
        super.onResume()
        isLogin = SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_LOGIN, false)
        if (SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_ANYTHING_CHANGE_NOTIFICATION, false)) {
            SharedPreferenceUtils.getInstance(activity).setValue(Constants.IS_ANYTHING_CHANGE_NOTIFICATION, false)
            if (isLogin) {
                ll_with_login.visibility = View.VISIBLE
                ll_without_login.visibility = View.GONE

                rv_notification.removeOnScrollListener(onScrollListener)
                list.clear()
                if(notificationAdapter != null) {
                    notificationAdapter?.notifyDataSetChanged()
                }
                pageCount = 1
                getNotificationList(true)
            } else {
                ll_with_login.visibility = View.GONE
                ll_without_login.visibility = View.VISIBLE
            }
        } else {
            if (isLogin) {
                ll_with_login.visibility = View.VISIBLE
                ll_without_login.visibility = View.GONE

                rv_notification.removeOnScrollListener(onScrollListener)
                list.clear()
                if(notificationAdapter != null) {
                    notificationAdapter?.notifyDataSetChanged()
                }
                pageCount = 1
                getNotificationList(true)
            } else {
                ll_with_login.visibility = View.GONE
                ll_without_login.visibility = View.VISIBLE
            }
        }
    }

    private fun removeLoaderView() {
        try {
            if (cmpagination != null && cmpagination!!.isShowing) {
                cmpagination!!.hide()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addViewLoadingView() {
        try {
            if (cmpagination == null)
                cmpagination = CustomPaginationIndicator(getActivity())
            cmpagination!!.show(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadNotifications() {
        Logger.print("loadNotifications !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        isLogin = SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_LOGIN, false)
        if (isLogin) {
            Logger.print("loadNotifications isLogin!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            ll_with_login.visibility = View.VISIBLE
            ll_without_login.visibility = View.GONE
            rv_notification.removeOnScrollListener(onScrollListener)
            list.clear()
            if(notificationAdapter != null) {
                notificationAdapter?.notifyDataSetChanged()
            }
            pageCount = 1
            getNotificationList(true)
        } else {
            Logger.print("loadNotifications else!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            ll_with_login.visibility = View.GONE
            ll_without_login.visibility = View.VISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as HomeActivity
    }

    companion object {
        fun newInstance() = NotificationFragement()
    }

    private fun applyPagination() {
        rv_notification.addOnScrollListener(onScrollListener)
    }

    private val onScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)


            currentVisibleCount = llm?.childCount!!
            totalItemCount = llm?.itemCount!!
            //lastVisibleItem = llm?.findFirstVisibleItemPosition()!!
            lastVisibleItem = llm?.findFirstVisibleItemPosition()!!
            if (!isLoading && list.size > 0) {
                if (!isDataFinished) {
                    // if (totalItemCount - currentVisibleCount <= lastVisibleItem + visibleThreshold)
                    Logger.print("currentVisibleCount===$currentVisibleCount=====lastVisibleItem$lastVisibleItem==========totalItemCount$totalItemCount")
                    if (currentVisibleCount + lastVisibleItem >= totalItemCount && lastVisibleItem >= 0) {
                        isLoading = true
                        pageCount++

                        Logger.print("Scrolled ===$dy=======$dx")
                        Logger.print("NotificationFragment Will call API with page count $pageCount")
                        if (dy > 0) {
                            addViewLoadingView()
                        }
                        Handler().postDelayed({
                            Logger.print("NotificationFragment Calling API with page count $pageCount")
                            getNotificationList(false)
                        }, Constants.DELAY)
                    }
                } else {
                    println("........finished data..........")
                }
            }
        }
    }

    private fun getNotificationList(isprogress: Boolean) {
        isLoading = true
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        var isProgressShow = false
        if (isprogress) {
            isProgressShow = pageCount == 1
        }
        Logger.print("pageCount !!!!$pageCount")
        viewModel.observeNotificationList(activity, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                removeLoaderView()
                Logger.print("NOTIFICATIONLISTING Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")

                lastAPICallTime = System.currentTimeMillis()
                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val array = Gson().fromJson<ArrayList<NotificationResponse>>(
                            jsonObject.getJSONArray("data").toString(),
                            object : TypeToken<ArrayList<NotificationResponse>>() {}.type)

                        Logger.print("array notification================$array")
                        if (pageCount == 1) {
                            list.clear()
                        }

                        isDataFinished = if (array.isNotEmpty()) {
                            list.addAll(array)
                            false
                        } else {
                            true
                        }

                        Logger.print("Notification list size===================" + list.size)
                        if (list.size > 0) {
                            rv_notification.visibility = View.VISIBLE
                            tv_nodata_notification.visibility = View.GONE
                            isLoading = false
                            isDataFinished = false
                            setAdapter()
                        } else {
                            isDataFinished = true
                            rv_notification.visibility = View.GONE
                            tv_nodata_notification.visibility = View.VISIBLE
                        }

                        if (jsonObject.has("pagination_count")) {
                            paginationCount = jsonObject.getInt("pagination_count")
                            Logger.print("paginationCount!!!!!!$paginationCount")
                        }

                        if (list.size > 0) {
                            if (list.size < paginationCount) {
                                Logger.print("userCategoryWise *******" + list.size)
                            } else {
                                Logger.print("ApplyPagination *******" + list.size)
                                rv_notification.removeOnScrollListener(onScrollListener)
                                applyPagination()
                            }
                        }
                    }
                } else if (code == 0) {
                    if (pageCount == 1) {
                        rv_notification.visibility = View.GONE
                        tv_nodata_notification.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun setAdapter() {
        if (notificationAdapter == null) {
            llm = LinearLayoutManager(activity)
            rv_notification.layoutManager = llm
            notificationAdapter = NotificationAdapter(activity, list, userID)
            rv_notification.adapter = notificationAdapter
        } else {
            notificationAdapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification_fragement, container, false)
    }
}