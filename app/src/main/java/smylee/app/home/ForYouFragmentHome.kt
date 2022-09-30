package smylee.app.home

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.urfeed.model.Reaction
import com.urfeed.model.Topic
import kotlinx.android.synthetic.main.activity_for_you_fragment.*
import org.json.JSONObject
import smylee.app.CallBacks.DoubleTapCallBacks
import smylee.app.CallBacks.ExtractAudioCallbacks
import smylee.app.CallBacks.ExtractListner
import smylee.app.Profile.MyProfileFragment
import smylee.app.Profile.OtherUserProfileActivity
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.adapter.PlayerViewPagerAdapter
import smylee.app.custom.Circle
import smylee.app.listener.*
import smylee.app.login.LoginActivity
import smylee.app.model.ForYouResponse
import smylee.app.ui.Activity.CameraVideoRecording
import smylee.app.ui.Activity.HashTagDetailOrTrendingDetail
import smylee.app.ui.base.BaseActivity
import smylee.app.ui.base.BaseFragment_x
import smylee.app.utils.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ForYouFragmentHome : BaseFragment_x(), InitNextVideoListener, OnVideoPlayingListener,
    OnShareVideoPrepareListener, DoubleTapCallBacks, ExtractAudioCallbacks, ExtractListner,
    OnCommentAdded, OnSpamDialogListener {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var topic: Topic? = null
    var position: Int? = 0
    private var reaction: Reaction? = null
    private var adapter1: PlayerViewPagerAdapter? = null
    var lastPosition = -1
    var previousPosition = -1
    var isLogin: Boolean = false
    private var cursorPagination: String = ""

    lateinit var viewModel: HomeViewModel
    private var pageCountForYou = 1
    private var isDataFinished: Boolean = false
    private var categoryVideo = ArrayList<ForYouResponse>()
    private var commentCountMap: HashMap<String, String> = HashMap()

    private var llMainComment: LinearLayout? = null
    private var llReportSpam: LinearLayout? = null
    private var rvComments: RecyclerView? = null
    private var tvNoData: TextView? = null
    private var ivClose: AppCompatImageView? = null
    private var tvLike: AppCompatTextView? = null
    var comments: AppCompatTextView? = null
    private var ivSend: AppCompatImageView? = null
    private var llSend: LinearLayout? = null
    private var edtComment: AppCompatEditText? = null
    private var likeCount: Int = 0
    private var currentPosition: Int = 0
    private var postId: Int? = null
    private var postCommentCount: String? = null
    var userId: String = ""
    var otherUserId: String = ""
    private val mRandom = Random()
    private val mTimer = Timer()
    var followerMap: HashMap<String, String> = HashMap()

    private var viewpagerposition: Int = 0

    lateinit var activity: HomeActivity
    var array: ArrayList<Topic>? = null

    var likeVideoHash: HashMap<String, String> = HashMap()
    var hasLikedMap: HashMap<String, String> = HashMap()
    private var bottomTabHeight: Int = 0

    private var lastAPICallTime: Long = 0
    private var sharingLink: String = ""
    private var postIdLink: String = ""
    private var isOnScrollCall = true
    private var isLoadingData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topic = arguments?.getParcelable("topic")
        position = arguments?.getInt("position", 0)
        reaction = arguments?.getParcelable("reaction")
    }

    private fun randomColor(): Int {
        return Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as HomeActivity
    }

    fun setTabHeight(bottomTabHeight: Int) {
        this.bottomTabHeight = bottomTabHeight
        if (llUserDetails != null) {
            val layoutParams = llUserDetails.layoutParams as RelativeLayout.LayoutParams
            layoutParams.bottomMargin = bottomTabHeight
            val layoutParamsSideMenu = side_menu.layoutParams as RelativeLayout.LayoutParams
            layoutParamsSideMenu.bottomMargin = bottomTabHeight
            val layoutParamOverLayTab = viewOverLayTab.layoutParams as RelativeLayout.LayoutParams
            layoutParamOverLayTab.height = bottomTabHeight
            viewOverLayTab.layoutParams = layoutParamOverLayTab
        }
    }

    private fun getDiscoverCategory(isProgress: Boolean, postIdLink: String) {
        isLoadingData = true
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCountForYou.toString()
        if (postIdLink != "") {
            hashMap["post_id"] = postIdLink
        }
        if (pageCountForYou == 1) {
            hashMap["cursor_pagination"] = "true"
        }
        if (pageCountForYou > 1) {
            hashMap["cursor_pagination"] = cursorPagination
        }
        viewModel.getdiscovercategory(activity, hashMap, isProgress).observe(this, Observer {
            if (it != null) {
                // llGif.visibility =View.GONE

                // swipeRefresh?.isRefreshing = false
                Logger.print("getdiscovercategory=========${it.toString()}")
                swipeRefresh?.setRefreshing(false)
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    frame.visibility = View.VISIBLE
                    tv_nodata_foryou.visibility = View.GONE
                    if (jsonObject.has("data")) {
                        val jsonObj1 = jsonObject.getJSONObject("data")
                        val jsonArray = jsonObj1.getJSONArray("user_video_list")
                        cursorPagination = jsonObj1.getString("cursor_pagination")

                        val array = Gson().fromJson<ArrayList<ForYouResponse>>(
                            jsonArray.toString(),
                            object : TypeToken<ArrayList<ForYouResponse>>() {}.type
                        )
                        if (pageCountForYou == 1) {
                            categoryVideo.clear()
                            commentCountMap.clear()
                            followerMap.clear()
                            if (adapter1 != null) {
                                adapter1!!.removeAllFragment()
                            }
                            adapter1 = null
                        }

                        if (array.isNotEmpty()) {
                            if (array.size < jsonObject.getInt("pagination_count")) {
                                isDataFinished = true
                            }
                            categoryVideo.addAll(array)
                            for (i in categoryVideo.indices) {
                                if (categoryVideo[i].post_id != null) {
                                    commentCountMap[categoryVideo[i].post_id.toString()] =
                                        categoryVideo[i].post_comment_count.toString()
                                }
                                followerMap[categoryVideo[i].user_id.toString()] =
                                    categoryVideo[i].is_following.toString()
                            }

                            if (pageCountForYou == 1) {
                                setViewPagerAdapter1()
                            } else {
                                val oldSize = adapter1?.addFragment1(array)
                                adapter1?.notifyItemRangeInserted(oldSize!!, array.size)
                            }
                        } else {
                            isDataFinished = true
                        }
                    } else {
                        isDataFinished = true
                    }
                } else {
                    isDataFinished = true
                    if (code == 0) {
                        if (pageCountForYou == 1) {
                            frame.visibility = View.GONE
                            tv_nodata_foryou.visibility = View.VISIBLE
                        }
                    }
                }
                isLoadingData = false
            }
        })
    }

    private fun setViewPagerAdapter1() {
        viewPager.offscreenPageLimit = 1
        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        if (adapter1 == null) {
            adapter1 = null
            adapter1 = PlayerViewPagerAdapter(this, this, this)
            adapter1?.addFragment1(categoryVideo)
            viewPager.adapter = adapter1
        } else {
            Handler().run {
                adapter1?.addFragment1(categoryVideo)
                adapter1?.notifyDataSetChanged()
            }
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Logger.print("onPageSelected $position")
                if (position < categoryVideo.size) {
                    if (position == 1) {
                        ll_swipe_hint.visibility = View.GONE
                    }
                    super.onPageSelected(position)
                    currentPosition = position
                    if (lastPosition != -1) {
                        val lastFragment = adapter1?.fragments?.get(lastPosition)
                        lastFragment?.stopPlayer()
                    }
                    val lastFragment = adapter1?.fragments?.get(position)
                    lastFragment?.setVolumeOfVideo()
                    previousPosition = lastPosition
                    lastPosition = position
                    if (commentBottomSheet != null && commentBottomSheet?.isVisible!!) {
                        commentBottomSheet?.dismiss()
                    }

                    if (categoryVideo.size > 0) {
                        viewpagerposition = position
                        side_menu.visibility = View.VISIBLE
                        iv_user_profile.setBackgroundResource(R.drawable.profile_thumb)
                        if (categoryVideo[position].no_of_followers != null && categoryVideo[position].no_of_followers != "") {
                            llView.visibility = View.VISIBLE
                            tv_follower_counts.visibility = View.VISIBLE
                            Methods.showFollowerCounts(
                                categoryVideo[position].no_of_followers.toString(),
                                tv_follower_counts,
                                context
                            )
                        } else {
                            llView.visibility = View.GONE
                            tv_follower_counts.visibility = View.GONE
                        }
                        if (userId.contentEquals(categoryVideo[position].user_id.toString())) {
                            flag_layout.visibility = View.GONE
                        } else {
                            flag_layout.visibility = View.VISIBLE
                        }

                        otherUserId = categoryVideo[position].user_id.toString()
                        val currentUserId = SharedPreferenceUtils.getInstance(activity)
                            .getStringValue(Constants.USER_ID_PREF, "")!!
                        if (!otherUserId.contentEquals("")) {
                            iv_user_profile.setOnClickListener {
                                if (!userId.contentEquals(otherUserId)) {
                                    if (!otherUserId.contentEquals("")) {
                                        val intent =
                                            Intent(activity, OtherUserProfileActivity::class.java)
                                        intent.putExtra("OTHER_USER_ID", otherUserId)
                                        startActivity(intent)
                                    }
                                } else {
                                    activity.hideTabBar()
                                    activity.setTabUI(false)
                                    activity.setSelected()
                                    val fragment: Fragment
                                    fragment = MyProfileFragment()
//                                    childFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
                                    fragmentManager!!.beginTransaction()
                                        .replace(R.id.container, fragment).commit()
                                }
                            }
                        }

                        iv_user_profile.visibility = View.VISIBLE
                        if (categoryVideo[lastPosition].profile_pic != null && !categoryVideo[lastPosition].profile_pic!!.contentEquals(
                                ""
                            )
                        ) {
                            Glide.with(context!!)
                                .load(categoryVideo[lastPosition].profile_pic)
                                .error(R.drawable.profile_thumb)
                                .centerCrop()
                                .into(iv_user_profile)
                        } else {
                            iv_user_profile.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            iv_user_profile.setImageResource(R.drawable.profile_thumb)
                        }
                        postId = categoryVideo[position].post_id
                        postCommentCount = categoryVideo[position].post_comment_count
                        forward_layout.setOnClickListener {
                            createLinks(position)
                        }
                        imgDownload.setOnClickListener {
                            val videoWidth = categoryVideo[position].post_width
                            val videoHeight = categoryVideo[position].post_height
                            if (videoHeight != null && videoWidth != null) {
                                requestPermission(
                                    categoryVideo[position].post_video!!,
                                    videoWidth,
                                    videoHeight,
                                    true
                                )
                            } else {
                                requestPermission(categoryVideo[position].post_video!!, 0, 0, true)
                            }
                        }



                        imgExtractAudio.setOnClickListener {
                            isLogin = SharedPreferenceUtils.getInstance(activity)
                                .getBoolanValue(Constants.IS_LOGIN, false)
                            if (isLogin) {
                                if (categoryVideo[position].post_video != null && categoryVideo[position].post_video != "") {
                                    requestPermission(
                                        categoryVideo[position].post_video!!,
                                        0,
                                        0,
                                        false
                                    )
                                }
                            } else {
                                if (lastPosition != -1 && lastPosition < adapter1?.fragments?.size!!) {
                                    val lastPosFragment = adapter1?.fragments?.get(lastPosition)
                                    lastPosFragment?.stopPlayer()
                                }
                                lastAPICallTime = 0
                                activity.isCallLogin = true
                                val intent = Intent(getActivity(), LoginActivity::class.java)
                                intent.putExtra("screen_name", "startPost")
                                startActivityForResult(
                                    intent,
                                    Constants.LOGIN_ACTIVITY_REQUEST_CODE
                                )
                            }
                        }
                        tv_userName.setOnClickListener {
                            if (!userId.contentEquals(otherUserId)) {
                                val intent = Intent(activity, OtherUserProfileActivity::class.java)
                                intent.putExtra("OTHER_USER_ID", otherUserId)
                                startActivity(intent)
                            } else {
                                activity.hideTabBar()
                                activity.setTabUI(false)
                                activity.setSelected()
                                val fragment: Fragment
                                fragment = MyProfileFragment()
//                                childFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
                                fragmentManager!!.beginTransaction()
                                    .replace(R.id.container, fragment).commit()
                            }
                        }
                        if (categoryVideo[position].profile_name != null && !categoryVideo[position].profile_name!!.contentEquals(
                                ""
                            )
                        ) {
                            tv_userName.text =
                                categoryVideo[position].profile_name.toString().trim()
                        }
                        if (categoryVideo[position].category_name != null && !categoryVideo[position].category_name!!.contentEquals(
                                ""
                            )
                        ) {
                            tv_cat_name.text = categoryVideo[position].category_name
                        }

                        if (categoryVideo[position].mark_as_verified_badge != null && categoryVideo[position].mark_as_verified_badge!! == 1) {
                            ivVerified?.visibility = View.VISIBLE
                        } else {
                            ivVerified?.visibility = View.GONE

                        }

                        if (categoryVideo[position].post_title != null && categoryVideo[position].post_title != "") {
                            Logger.print("categoryVideo[position].post_title============${categoryVideo[position].post_title!!.length}")
                            if (categoryVideo[position].post_title!!.toString().contains("#")) {
                                if (categoryVideo[position].post_title!!.toString().trim()
                                        .contains(" ")
                                ) {
                                    val str = categoryVideo[position].post_title!!.toString()
                                    val splited = str.split(" ").toTypedArray()
                                    val builder = SpannableStringBuilder()
                                    for (word in splited) {
                                        builder.append(word).append(" ")
                                            .setSpan(object : ClickableSpan() {
                                                override fun onClick(@NonNull view: View) {
                                                    if (word.startsWith("#")) {
                                                        val intent = Intent(
                                                            activity,
                                                            HashTagDetailOrTrendingDetail::class.java
                                                        )
                                                        intent.putExtra("hashTag", word)
                                                        startActivity(intent)
                                                    }
                                                } // optional - for styling the specific text

                                                override fun updateDrawState(ds: TextPaint) {
                                                    super.updateDrawState(ds)
                                                    ds.color = ContextCompat.getColor(
                                                        activity,
                                                        R.color.hash_tag
                                                    )
                                                    ds.isUnderlineText = false
                                                }
                                            }, builder.length - word.length, builder.length, 0)
                                    }
                                    //  ss.setSpan(clickableSpan1,startindex,endIndex,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    tvPostTitle.setText(builder, TextView.BufferType.SPANNABLE)
                                    tvPostTitle.movementMethod = LinkMovementMethod.getInstance()
                                    tvPostTitle.setTextColor(
                                        ContextCompat.getColor(
                                            activity,
                                            R.color.hash_tag
                                        )
                                    )
                                    tvPostTitle.setOnClickListener {}
                                } else {
                                    tvPostTitle.text = categoryVideo[position].post_title
                                    tvPostTitle.setTextColor(
                                        ContextCompat.getColor(
                                            activity,
                                            R.color.hash_tag
                                        )
                                    )
                                    tvPostTitle.setOnClickListener {
                                        val intent = Intent(
                                            activity,
                                            HashTagDetailOrTrendingDetail::class.java
                                        )
                                        intent.putExtra(
                                            "hashTag",
                                            categoryVideo[position].post_title.toString()
                                        )
                                        startActivity(intent)
                                    }
                                }
                            } else {
                                tvPostTitle.text = categoryVideo[position].post_title
                                tvPostTitle.setTextColor(
                                    ContextCompat.getColor(
                                        activity,
                                        R.color.white
                                    )
                                )
                                tvPostTitle.setOnClickListener {}
                            }
                        }

                        var isFollowingUser = ""
                        for (key in followerMap.keys) {
                            isFollowingUser =
                                followerMap[categoryVideo[position].user_id.toString()]!!
                        }
                        if (isFollowingUser.contentEquals("1")) {
                            tv_follow_.visibility = View.GONE
                            tv_following_user.visibility = View.VISIBLE
                        }
                        if (isFollowingUser.contentEquals("0")) {
                            tv_follow_.visibility = View.VISIBLE
                            tv_following_user.visibility = View.INVISIBLE
                        }
                        if (categoryVideo[position].user_id.toString() == currentUserId) {
                            tv_follow_.visibility = View.GONE
                            tv_following_user.visibility = View.INVISIBLE
                        }

                        //commented for streaming
                        tv_follow_.setOnClickListener {
                            isLogin = SharedPreferenceUtils.getInstance(activity)
                                .getBoolanValue(Constants.IS_LOGIN, false)
                            if (isLogin) {
                                tv_follow_.visibility = View.GONE
                                tv_following_user.visibility = View.VISIBLE
                                followerMap[categoryVideo[position].user_id.toString()] = "1"
                                followUser(categoryVideo[position].user_id.toString())
                            } else {
                                if (lastPosition != -1 && lastPosition < adapter1?.fragments?.size!!) {
                                    val lastPosFragment = adapter1?.fragments?.get(lastPosition)
                                    lastPosFragment?.stopPlayer()
                                }
                                lastAPICallTime = 0
                                activity.isCallLogin = true
                                val intent = Intent(getActivity(), LoginActivity::class.java)
                                intent.putExtra("screen_name", "forYou")
                                activity.startActivityForResult(
                                    intent,
                                    Constants.LOGIN_ACTIVITY_REQUEST_CODE
                                )
                            }
                        }

                        tv_following_user.setOnClickListener {
                            isLogin = SharedPreferenceUtils.getInstance(activity)
                                .getBoolanValue(Constants.IS_LOGIN, false)
                            if (isLogin) {
                                tv_following_user.visibility = View.INVISIBLE
                                tv_follow_.visibility = View.VISIBLE
                                followerMap[categoryVideo[position].user_id.toString()] = "0"
                                unFollowUser(categoryVideo[position].user_id.toString())
                            } else {
                                if (lastPosition != -1 && lastPosition < adapter1?.fragments?.size!!) {
                                    val lastPosFragment = adapter1?.fragments?.get(lastPosition)
                                    lastPosFragment?.stopPlayer()
                                }
                                lastAPICallTime = 0
                                activity.isCallLogin = true
                                val intent = Intent(getActivity(), LoginActivity::class.java)
                                intent.putExtra("screen_name", "forYou")
                                activity.startActivityForResult(
                                    intent,
                                    Constants.LOGIN_ACTIVITY_REQUEST_CODE
                                )
                            }
                        }

                        if (categoryVideo[position].post_view_count != null && categoryVideo.toString() != "") {
                            Methods.showViewCounts(
                                categoryVideo[position].post_view_count.toString(),
                                tvpostCount
                            )
                        }
                        if (categoryVideo[position].has_liked.toString().contentEquals("1")) {
                            like_image.setImageResource(R.drawable.like_active)
                        }
                        if (categoryVideo[position].has_liked.toString().contentEquals("0")) {
                            like_image.setImageResource(R.drawable.like_new)
                        }
                        if (categoryVideo[position].post_like_count != null && !categoryVideo[position].post_like_count!!.contentEquals(
                                ""
                            )
                        ) {
                            Methods.showLikeCounts(
                                categoryVideo[position].post_like_count.toString(),
                                like_txt
                            )
                        }

                        like_image.setOnClickListener {
                            if (categoryVideo[position].has_liked == 0) {
                                likeCount = 1
                                like_image.setImageResource(R.drawable.like_active)
                                heart_layout.addHeart(randomColor())
                                heart_layout.addHeart(
                                    randomColor(),
                                    R.drawable.like_active,
                                    R.drawable.like_new
                                )
                                heart_layout.addHeart(randomColor())
                                heart_layout.addHeart(randomColor())
                                heart_layout.addHeart(randomColor())
                                heart_layout.addHeart(randomColor())
                                val likeCounts =
                                    Integer.parseInt(categoryVideo[position].post_like_count!!) + 1

                                Methods.showViewCounts(likeCounts.toString(), like_txt)
                                likeUnlikeVideo(categoryVideo[position].post_id, likeCount)
                                categoryVideo[position].post_like_count = "$likeCounts"
                                categoryVideo[position].has_liked = 1
                            } else {
                                likeCount = 0
                                like_image.setImageResource(R.drawable.like_new)
                                if (!categoryVideo[position].post_like_count.toString()
                                        .contentEquals("0")
                                ) {
                                    val likeCounts =
                                        Integer.parseInt(categoryVideo[position].post_like_count!!) - 1
                                    Methods.showViewCounts(likeCounts.toString(), like_txt)
                                    likeUnlikeVideo(categoryVideo[position].post_id, likeCount)
                                    categoryVideo[position].post_like_count = "$likeCounts"
                                    categoryVideo[position].has_liked = 0
                                }
                            }
                        }

                        if (categoryVideo[position].post_comment_count != null && !categoryVideo[position].post_comment_count!!.contentEquals(
                                ""
                            )
                        ) {
                            Methods.showCommentCounts(
                                categoryVideo[position].post_comment_count.toString(),
                                comment_txt
                            )
                        }
                        if (categoryVideo[position].post_comment_count != null && !categoryVideo[position].post_comment_count!!.contentEquals(
                                ""
                            )
                        ) {
                            Methods.showCommentCountsDialog(
                                categoryVideo[position].post_comment_count!!.toString(),
                                comments!!,
                                context
                            )
                        }
                    }
                    if (!isDataFinished && position == categoryVideo.size - 2 && !isLoadingData) {
                        pageCountForYou += 1
                        Logger.print("pageCountForYou======$pageCountForYou")
                        getDiscoverCategory(false, "")
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == 1) {
                    isOnScrollCall = true
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (isOnScrollCall) {
                    isOnScrollCall = false
                    if (positionOffset < 0.2f) {
                        if (position < categoryVideo.size - 1) {
                            val lastFragment = adapter1?.fragments?.get(position + 1)
                            lastFragment?.playVideo(false)
                        }
                    } else {
                        if (position > 0) {
                            val lastFragment = adapter1?.fragments?.get(position - 1)
                            lastFragment?.playVideo(false)
                        }
                    }
                }
            }
        })

        val lastFragment = adapter1?.fragments?.get(0)
        lastFragment?.playVideo(true)
        viewPager.setCurrentItem(0, false)
    }

    private fun unFollowUser(userID: String) {
        val isFollow = "0"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = userID
        viewModel.FollowUnfollow(activity, hashMap).observe(this, Observer {
            if (it != null) {
                SharedPreferenceUtils.getInstance(activity)
                    .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
            }
        })
    }

    private fun followUser(userId: String) {
        val isFollow = "1"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = userId
        viewModel.FollowUnfollow(activity, hashMap).observe(this, Observer {
            if (it != null) {
                SharedPreferenceUtils.getInstance(activity)
                    .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mTimer.cancel()
    }

    fun callForBlock() {
        if (SharedPreferenceUtils.getInstance(activity).getBoolanValue(
                Constants.IS_ANYTHING_CHANGE_FORYOU_FOR_BLOCK,
                false
            ) || SharedPreferenceUtils.getInstance(activity)
                .getBoolanValue(Constants.IS_ANYTHING_CHANGE_FOR_You, false) || adapter1 == null
        ) {
            Logger.print("getDiscoverCategory callForBlock!!!!!!!!!!!!!!!!!!")
            lastAPICallTime = System.currentTimeMillis()
            pageCountForYou = 1
            getDiscoverCategory(true, postIdLink)
            SharedPreferenceUtils.getInstance(activity)
                .setValue(Constants.IS_ANYTHING_CHANGE_FORYOU_FOR_BLOCK, false)
            SharedPreferenceUtils.getInstance(activity)
                .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, false)
        }
    }

    fun onBackPressedCall(activity: HomeActivity) {
        if (spamBottomSheet != null && spamBottomSheet!!.isVisible) {
            Utils.hideKeyboard(activity)
            spamBottomSheet?.dismiss()
        } else if (commentBottomSheet != null && commentBottomSheet!!.isVisible) {
            Utils.hideKeyboard(activity)
            commentBottomSheet?.dismiss()
        } else {
            activity.onBackPressed()
        }
    }

    fun playVideo(isLoadData: Boolean, postId: String) {
        //  postIdLink = postId
        if (isLoadData) {
            Logger.print("playVideo isLoadData $position")
            initFragment()
        } else {
            if (lastPosition != viewPager.currentItem) {
                Logger.print("playVideo lastPosition $lastPosition view pager current pos ${viewPager.currentItem}")
                viewPager.setCurrentItem(lastPosition, false)
            } else {
                val lastFragment = adapter1?.fragments?.get(lastPosition)
                lastFragment?.playVideo(true)
            }
        }
    }

    fun getPostId(postId: String) {
        postIdLink = postId
        Logger.print("postIdLink=====$postIdLink")
    }

    override fun onPause() {
        mTimer.cancel()
        if (spamBottomSheet != null && spamBottomSheet?.isVisible != null && spamBottomSheet?.isVisible!! && commentBottomSheet != null && commentBottomSheet?.isVisible != null && commentBottomSheet?.isVisible!!) {
            spamBottomSheet?.dismiss()
            commentBottomSheet?.dismiss()
            Utils.hideKeyboard(activity)
        } else if (spamBottomSheet != null && spamBottomSheet?.isVisible != null && spamBottomSheet?.isVisible!!) {
            spamBottomSheet?.dismiss()
        } else if (commentBottomSheet != null && commentBottomSheet?.isVisible != null && commentBottomSheet?.isVisible!!) {
            commentBottomSheet?.dismiss()
        }
        super.onPause()
    }

    override fun onStop() {
        activity.showTabBar()
        if (adapter1 != null) {
            if (lastPosition != -1 && lastPosition < adapter1?.fragments?.size!!) {
                val lastFragment = adapter1?.fragments?.get(lastPosition)
                lastFragment?.stopPlayer()
            } else {
                for (i in 0 until adapter1?.fragments?.size!!) {
                    adapter1?.fragments?.get(i)?.stopPlayer()
                }
            }
        }
        super.onStop()
    }

    override fun onDetach() {
        adapter1 = null
        mTimer.cancel()
        super.onDetach()
    }

    override fun onDestroy() {
        mTimer.cancel()
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)

        super.onDestroy()
    }

    private fun createLinks(position: Int) {
        val iOSParams = DynamicLink.IosParameters.Builder("com.smylee.app")
        iOSParams.appStoreId = Constants.APP_STORE_ID
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(createShareUri(categoryVideo[position].post_id.toString()))
            .setDomainUriPrefix("https://link.smylee.app")
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder("smylee.app").build())
            .setIosParameters(iOSParams.build())
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters
                    .Builder()
                    .setTitle(categoryVideo[position].post_title!!)
                    .setImageUrl(Uri.parse(categoryVideo[position].post_video_thumbnail))
                    .build()
            )
            .buildShortDynamicLink()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Logger.print("DEEPLINK==========${it.result!!.toString()}")
                    sharingLink = it.result!!.shortLink.toString()

                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        "Checkout this interesting video from Smylee App : $sharingLink"
                    )
                    sendIntent.type = "text/plain"
                    startActivity(sendIntent)
                } else {
                    it.exception?.printStackTrace()
                }
            }
    }

    private fun createShareUri(postId: String): Uri {
        val builder = Uri.Builder()
        builder.scheme("https").authority("link.smylee.app").appendPath("video")
            .appendQueryParameter("postId", postId)
        return builder.build()
    }

    private fun likeUnlikeVideo(post_id: Int?, like_unlike: Int) {
        val hashMap = HashMap<String, String>()
        hashMap["is_like"] = like_unlike.toString()
        hashMap["post_id"] = post_id.toString()

        viewModel.likeUnlikeVideo(activity, hashMap, false).observe(this, Observer {
            if (it != null) {
                Logger.print("LIKE VIDEO OR UNLIKE VIDEO!!!!!!!!!!!!!!!!!!!$it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 0) {
                    Utils.showAlert(activity, "", jsonObject["message"].toString())
                }
            }
        })
    }

    private fun requestPermission(
        videoLink: String,
        videoWidth: Int,
        videoHeight: Int,
        isDownload: Boolean
    ) {
        HomePagerAdapter.isCallOnResume = 1
        activity.isAttachFragmentAgain = false
        Dexter.withActivity(activity).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (isDownload) {
                    if (report.areAllPermissionsGranted()) {
                        activity.showProgressDialog()
                        ShareVideoUtils.shareVideoFile(
                            context!!,
                            videoLink,
                            videoWidth,
                            videoHeight,
                            this@ForYouFragmentHome
                        )
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog()
                    }
                } else {
                    if (report.areAllPermissionsGranted()) {
                        activity.showProgressDialog()
                        val rootDirectory = File(Constants.EXTRACT_PATH)
                        if (rootDirectory.exists()) {
                            Methods.deleteFilesFromDirectory(Constants.EXTRACT_PATH)
                        }
                        ShareVideoUtils.extractVideoFile(
                            context!!,
                            videoLink,
                            this@ForYouFragmentHome
                        )
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog()
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: List<PermissionRequest>,
                token: PermissionToken
            ) {
                token.continuePermissionRequest()
            }
        }).withErrorListener {
            Toast.makeText(context, "Error occurred! ", Toast.LENGTH_SHORT).show()
        }.onSameThread().check()
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.message_need_permission))
        builder.setMessage(getString(R.string.message_permission))
        builder.setPositiveButton(getString(R.string.title_go_to_setting)) { dialog, _ ->
            dialog.cancel()
            openSettings()
        }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        firebaseAnalytics = FirebaseAnalytics.getInstance(activity)
        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()

        // Create a callbackManager to handle the login responses.

        Methods.setImageRotation(imgExtractAudio)
        if (!SharedPreferenceUtils.getInstance(activity)
                .getBoolanValue(Constants.FIRST_TIME_INSTALL, false)
        ) {
            SharedPreferenceUtils.getInstance(activity).setValue(Constants.FIRST_TIME_INSTALL, true)
            ll_swipe_hint.visibility = View.VISIBLE
            Glide.with(activity).load(R.raw.swipe_gif_final).into(ivSwipe)
        }


        firebaseAnalytics.setUserId(userId)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, userId)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        tvLike = view.findViewById(R.id.like_txt)

        if (bottomTabHeight > 0) {
            val layoutParams = llUserDetails.layoutParams as RelativeLayout.LayoutParams
            layoutParams.bottomMargin = bottomTabHeight
            val layoutParamsSideMenu = side_menu.layoutParams as RelativeLayout.LayoutParams
            layoutParamsSideMenu.bottomMargin = bottomTabHeight
            val layoutParamOverLayTab = viewOverLayTab.layoutParams as RelativeLayout.LayoutParams
            layoutParamOverLayTab.height = bottomTabHeight
        }

        llMainComment = view.findViewById(R.id.llMainComment) as LinearLayout
        llReportSpam = view.findViewById(R.id.ll_report_spam) as LinearLayout
        rvComments = view.findViewById<View>(R.id.rv_comments) as RecyclerView
        tvNoData = view.findViewById<View>(R.id.tv_nodata) as TextView
        comments = view.findViewById<View>(R.id.comments) as AppCompatTextView
        ivClose = view.findViewById<View>(R.id.iv_close) as AppCompatImageView
        ivSend = view.findViewById<View>(R.id.iv_send) as AppCompatImageView
        llSend = view.findViewById<View>(R.id.ll_send) as LinearLayout
        edtComment = view.findViewById<View>(R.id.edt_comment) as AppCompatEditText

        rvComments!!.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        ivFlag.setOnClickListener {
            if (postId != null) {
                if (adapter1?.fragments != null) {
                    val lastFragment = adapter1?.fragments?.get(lastPosition)
                    lastFragment?.stopPlayer()
                }
                if (spamBottomSheet == null || !spamBottomSheet?.isVisible!!) {
                    spamBottomSheet =
                        SpamBottomSheet.newInstance(userId, "", postId!!, this@ForYouFragmentHome)
                    spamBottomSheet?.show(fragmentManager!!, "SpamBottomSheet")
                }
            }
        }

        comment.setOnClickListener {
            if (adapter1?.fragments != null) {
                val lastFragment = adapter1?.fragments?.get(lastPosition)
                lastFragment?.stopPlayer()
            }
            if (commentBottomSheet == null || !commentBottomSheet?.isVisible!!) {
                if (postCommentCount != null) {
                    commentBottomSheet = CommentBottomSheet.newInstance(
                        userId,
                        postId!!,
                        postCommentCount!!,
                        this@ForYouFragmentHome
                    )
                }
                commentBottomSheet?.show(fragmentManager!!, "Comment_Bottom")
            }
        }

        //   swipeRefresh?.setProgressViewOffset(false, 0, 130)
        //val cc = CircleProgressBar(activity)

        // cc.setSize(50)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )

        /*layoutParams.setMargins(0, -30, 0, 0)
        Circle(activity).setPadding(0,-50,0,0)
        Circle(activity).layoutParams = layoutParams*/
        swipeRefresh?.setCustomBar(Circle(activity))
        swipeRefresh?.setRefreshListener {

            lastAPICallTime = System.currentTimeMillis()
            pageCountForYou = 1
            getDiscoverCategory(false, postIdLink)
        }
/*
 swipeRefresh?.setOnRefreshListener {
            Logger.print("swipeRefresh!!!!!!!!!!!!!!${swipeRefresh.isRefreshing}")
            */
/*llGif.visibility =View.VISIBLE
            Glide.with(context!!)
                .load(R.raw.emoji)
                .placeholder(R.drawable.profile_thumb)
                .into(ivEmojiGIF)*//*

            lastAPICallTime = System.currentTimeMillis()
            pageCountForYou = 1
            getDiscoverCategory(false, postIdLink)
        }
*/

        like_image.setOnClickListener {}
        initFragment()
        /*LocalBroadcastManager.getInstance(activity)
            .registerReceiver(receiver, IntentFilter("notificationBroadCast"))*/
    }


    var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {
                val bundle = intent.extras
                if (bundle != null) {
                    Logger.print(
                        "post id BroadcastReceiver =========${bundle.getString("post_id")
                            .toString()}"


                    )

                    if (bundle.getString("post_id") != null) {

                        postIdLink = bundle.getString("post_id").toString()
                        if (HomePagerAdapter.currentTab == 0 && HomeActivity.tabIndex == 0) {
                            pageCountForYou = 1
                            getDiscoverCategory(true, postIdLink)
                        }

                    }
                }
            }
        }
    }

    private fun initFragment() {
        if (activity.currentTabIndex == 0) {
            if (HomePagerAdapter.isCallOnResume == 0) {
                if (adapter1 != null && lastPosition != -1) {
                    val currentTime = System.currentTimeMillis()
                    if ((currentTime - lastAPICallTime) > 300000) {
                        lastAPICallTime = System.currentTimeMillis()
                        pageCountForYou = 1
                        if (BaseActivity.forYouListResponse != "") {
                            readDataFromAPIResponse()
                        }
                        /*else {
                            getDiscoverCategory(true, postIdLink)
                        }*/
                    } else {
                        val lastFragment = adapter1?.fragments?.get(lastPosition)
                        lastFragment?.playVideo(true)
                    }
                } else {
                    lastAPICallTime = System.currentTimeMillis()
                    pageCountForYou = 1
                    if (BaseActivity.forYouListResponse != "") {
                        readDataFromAPIResponse()
                    }
                    /*else {
                        getDiscoverCategory(true, postIdLink)
                    }*/
                }
            } else if (HomePagerAdapter.isCallOnResume == 1 && lastPosition != -1) {
                val lastFragment = adapter1?.fragments?.get(lastPosition)
                lastFragment?.playVideo(true)
            }
        }
    }

    private fun readDataFromAPIResponse() {
        val jsonObject = JSONObject(BaseActivity.forYouListResponse)
        val code = jsonObject.getInt("code")
        if (code == 1) {
            frame.visibility = View.VISIBLE
            tv_nodata_foryou.visibility = View.GONE
            if (jsonObject.has("data")) {
                val jsonObj1 = jsonObject.getJSONObject("data")
                val jsonArray = jsonObj1.getJSONArray("user_video_list")

                val array = Gson().fromJson<ArrayList<ForYouResponse>>(
                    jsonArray.toString(),
                    object : TypeToken<ArrayList<ForYouResponse>>() {}.type
                )
                if (pageCountForYou == 1) {
                    categoryVideo.clear()
                    commentCountMap.clear()
                    followerMap.clear()
                }

                if (array.isNotEmpty()) {
                    if (array.size < jsonObject.getInt("pagination_count")) {
                        isDataFinished = true
                    }
                    categoryVideo.addAll(array)
                    for (i in categoryVideo.indices) {
                        commentCountMap[categoryVideo[i].post_id.toString()] =
                            categoryVideo[i].post_comment_count.toString()
                        followerMap[categoryVideo[i].user_id.toString()] =
                            categoryVideo[i].is_following.toString()
                    }

                    if (pageCountForYou == 1) {
                        setViewPagerAdapter1()
                    } else {
                        val oldSize = adapter1?.addFragment1(array)
                        adapter1?.notifyItemRangeInserted(oldSize!!, array.size)
                        /*adapter1?.addFragment1(array)
                        adapter1?.notifyDataSetChanged()*/
                    }
                } else {
                    isDataFinished = true
                }
            } else {
                isDataFinished = true
            }
        } else {
            isDataFinished = true
            if (code == 0) {
                if (pageCountForYou == 1) {
                    frame.visibility = View.GONE
                    tv_nodata_foryou.visibility = View.VISIBLE
                }
            }
        }
        BaseActivity.forYouListResponse = ""
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(activity)
            .registerReceiver(receiver, IntentFilter("notificationBroadCast"))
        val isAnythingChangeFollowingForBlock = SharedPreferenceUtils.getInstance(activity)
            .getBoolanValue(Constants.IS_ANYTHING_CHANGE_FORYOU_FOR_BLOCK, false)
        val isAnythingChangeFollowingFor = SharedPreferenceUtils.getInstance(activity)
            .getBoolanValue(Constants.IS_ANYTHING_CHANGE_FOR_You, false)

        if ((isAnythingChangeFollowingForBlock && HomePagerAdapter.currentTab == 0 && HomeActivity.tabIndex == 0) || (isAnythingChangeFollowingFor && HomePagerAdapter.currentTab == 0 && HomeActivity.tabIndex == 0) || adapter1 == null) {
            lastAPICallTime = System.currentTimeMillis()
            pageCountForYou = 1
            getDiscoverCategory(true, postIdLink)
            SharedPreferenceUtils.getInstance(activity)
                .setValue(Constants.IS_ANYTHING_CHANGE_FORYOU_FOR_BLOCK, false)
            SharedPreferenceUtils.getInstance(activity)
                .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, false)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_for_you_fragment, container, false)
    }

    override fun onVideoStartPlaying(position: Int) {
        /*if(previousPosition > -1) {
            val previousFragment = adapter1!!.fragments[previousPosition]
            previousFragment.stopPlayer()
        }*/
    }

    override fun onVideoViewCountUpdate(position: Int, videoViewCount: String) {
        categoryVideo[position].isViewed = true
    }

    override fun onInitNextCall(currentPos: Int) {
        /*if ((currentPos + 1) < categoryVideo.size && (currentPos + 1) < (lastPosition + 6)) {
            activity.preDownload(categoryVideo[currentPos + 1].post_video,
                null, currentPos + 1, this@ForYouFragmentHome)
        }*/
    }

    override fun onVideoReadyToShare(videoPath: String) {
        activity.hideProgressDialog()
        Methods.deleteFileWithZeroKB(Constants.DOWNLOADED_PATH)
        Utils.showToastMessage(activity, getString(R.string.video_downloaded))
        val gifFile = File(Constants.ROOT_PATH, "smylee_gif_new.gif")
        if (gifFile.exists()) {
            gifFile.delete()
        }

        val videoFinalPath = Methods.filePathFromDir(Constants.DOWNLOADED_PATH)
        Logger.print("videoFinalPath===============$videoFinalPath")
        // sharePostToFacebook("",videoFinalPath!!,"This is Video")
    }

    override fun onDoubleTAP(clickState: Int) {
        when (clickState) {
            1 ->
                if (spamBottomSheet != null && spamBottomSheet!!.isVisible) {
                    spamBottomSheet?.dismiss()
                } else if (commentBottomSheet != null && commentBottomSheet!!.isVisible) {
                    commentBottomSheet?.dismiss()
                }
            2 ->
                if ((commentBottomSheet == null || !commentBottomSheet?.isVisible!!) && (spamBottomSheet == null || !spamBottomSheet?.isVisible!!)) {
                    imgLikeDoubleTap.visibility = View.VISIBLE
                    Methods.animateHeart(imgLikeDoubleTap)
                    if (categoryVideo[currentPosition].has_liked == 0) {
                        likeCount = 1
                        like_image.setImageResource(R.drawable.like_active)
                        val likeCounts =
                            Integer.parseInt(categoryVideo[currentPosition].post_like_count!!) + 1
                        Methods.showLikeCounts(likeCounts.toString(), like_txt)
                        likeUnlikeVideo(categoryVideo[currentPosition].post_id, likeCount)
                        categoryVideo[currentPosition].post_like_count = "$likeCounts"
                        categoryVideo[currentPosition].has_liked = 1
                    }
                }
        }
    }

    companion object {
        var commentBottomSheet: CommentBottomSheet? = null
        var spamBottomSheet: SpamBottomSheet? = null
    }

    override fun OnExtractFinish() {}

    override fun onExtractFail(errorMassage: String) {
        Utils.showAlert(activity, "", errorMassage)
    }

    override fun onExtractFinished() {
        activity.hideProgressDialog()
        Methods.deleteFilesFromDirectory(Constants.EXTRACT_PATH)
        var nameFile = Methods.fileFromDir(Constants.Audio_PATH)
        if (nameFile!!.contains(".mp4")) {
            nameFile = nameFile.replace(".mp4", "")
        }

        val isLogin =
            SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_LOGIN, false)
        if (isLogin) {
            val intent = Intent(getActivity(), CameraVideoRecording::class.java)
            intent.putExtra("ISAUDIOMIX", true)
            intent.putExtra("extractAudio", true)
            intent.putExtra("audioFileNameFinal", nameFile)
            activity.startActivity(intent)
        } else {
            if (lastPosition != -1 && lastPosition < adapter1?.fragments?.size!!) {
                val lastFragment = adapter1?.fragments?.get(lastPosition)
                lastFragment?.stopPlayer()
            }
            lastAPICallTime = 0
            activity.isCallLogin = true
            val intent = Intent(getActivity(), LoginActivity::class.java)
            intent.putExtra("screen_name", "startPost")
            startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
        }
    }

    override fun onCommentAdded(postId: Int, tvCommnetCountBottomSheet: AppCompatTextView) {
        if (lastPosition != -1 && postId == categoryVideo[lastPosition].post_id) {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "AddCommentForYou")
            bundle.putString(
                FirebaseAnalytics.Param.ITEM_ID,
                categoryVideo[lastPosition].post_id.toString()
            )
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

            val commentsCount =
                Integer.parseInt(categoryVideo[lastPosition].post_comment_count!!) + 1
            postCommentCount = "$commentsCount"
            categoryVideo[lastPosition].post_comment_count = "$commentsCount"
            adapter1?.notifyItemChanged(lastPosition)
            if (commentsCount.toString() != "") {
                Logger.print("commentsCount!!!!!!!!!!!!!!$commentsCount")
                Methods.showCommentCounts(commentsCount.toString(), comment_txt)
                Methods.showCommentCountsDialog(
                    commentsCount.toString(),
                    tvCommnetCountBottomSheet,
                    context
                )
            }
        }
    }

    override fun onDialogDismiss() {
        Utils.hideKeyboard(activity)
        activity.showTabBar()
        if (adapter1?.fragments != null) {
            adapter1?.fragments!![currentPosition].playVideo(true)
        }
    }

    override fun onSpamComment(commentId: String) {
        if (spamBottomSheet == null || !spamBottomSheet?.isVisible!!) {
            spamBottomSheet =
                SpamBottomSheet.newInstance(userId, commentId, postId!!, this@ForYouFragmentHome)
//            spamBottomSheet?.show(childFragmentManager, "SpamBottomSheet")
            spamBottomSheet?.show(fragmentManager!!, "SpamBottomSheet")
        }
    }

    override fun onSelfProfileClick() {
        activity.hideTabBar()
        activity.setTabUI(false)
        activity.setSelected()
        val fragment: Fragment
        fragment = MyProfileFragment()
//                                    childFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        fragmentManager!!.beginTransaction().replace(R.id.container, fragment).commit()
    }

    override fun onSpamDialogDismiss() {
        Utils.hideKeyboard(activity)
        activity.showTabBar()
        if (commentBottomSheet != null && commentBottomSheet?.isVisible!!) {
            if (adapter1?.fragments != null) {
                adapter1?.fragments!![currentPosition].stopPlayer()
            }
        } else {
            if (adapter1?.fragments != null) {
                adapter1?.fragments!![currentPosition].playVideo(true)
            }
        }
    }
}