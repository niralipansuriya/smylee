package smylee.app.notification

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_video_play.*
import kotlinx.android.synthetic.main.notification_detail_activity.*
import kotlinx.android.synthetic.main.notification_detail_activity.avatar
import kotlinx.android.synthetic.main.notification_detail_activity.comment
import kotlinx.android.synthetic.main.notification_detail_activity.comment_txt
import kotlinx.android.synthetic.main.notification_detail_activity.flag_layout
import kotlinx.android.synthetic.main.notification_detail_activity.forward_layout
import kotlinx.android.synthetic.main.notification_detail_activity.heart_layout
import kotlinx.android.synthetic.main.notification_detail_activity.imgDownload
import kotlinx.android.synthetic.main.notification_detail_activity.imgExtractAudio
import kotlinx.android.synthetic.main.notification_detail_activity.imgLikeDoubleTap
import kotlinx.android.synthetic.main.notification_detail_activity.ivFlag
import kotlinx.android.synthetic.main.notification_detail_activity.ivVerified
import kotlinx.android.synthetic.main.notification_detail_activity.iv_user_profile
import kotlinx.android.synthetic.main.notification_detail_activity.like_image
import kotlinx.android.synthetic.main.notification_detail_activity.like_txt
import kotlinx.android.synthetic.main.notification_detail_activity.llView
import kotlinx.android.synthetic.main.notification_detail_activity.side_menu
import kotlinx.android.synthetic.main.notification_detail_activity.tvPostTitle
import kotlinx.android.synthetic.main.notification_detail_activity.tv_cat_name
import kotlinx.android.synthetic.main.notification_detail_activity.tv_follow_
import kotlinx.android.synthetic.main.notification_detail_activity.tv_follower_counts
import kotlinx.android.synthetic.main.notification_detail_activity.tv_following_user
import kotlinx.android.synthetic.main.notification_detail_activity.tv_userName
import kotlinx.android.synthetic.main.notification_detail_activity.tvpostCount
import kotlinx.android.synthetic.main.notification_detail_activity.viewPagerPlayer
import org.json.JSONObject
import smylee.app.CallBacks.DoubleTapCallBacks
import smylee.app.CallBacks.ExtractListner
import smylee.app.Profile.MyProfileFragment
import smylee.app.Profile.OtherUserProfileActivity
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.adapter.PlayerViewPagerAdapter
import smylee.app.home.CommentBottomSheet
import smylee.app.home.HomeViewModel
import smylee.app.home.SpamBottomSheet
import smylee.app.listener.*
import smylee.app.login.LoginActivity
import smylee.app.model.CommentResponse
import smylee.app.model.ForYouResponse
import smylee.app.ui.Activity.CameraVideoRecording
import smylee.app.ui.Activity.HashTagDetailOrTrendingDetail
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NotificationDetailActivity : BaseActivity(), InitNextVideoListener, OnVideoPlayingListener,
    OnShareVideoPrepareListener, DoubleTapCallBacks, ExtractListner, OnCommentAdded,
    OnSpamDialogListener {

    lateinit var viewModel: HomeViewModel
    private lateinit var videoDetailResponse: ForYouResponse
    var lastPosition = -1
    var previousPosition = -1

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var videoList = ArrayList<ForYouResponse>()
    private var adapter1: PlayerViewPagerAdapter? = null
    private var currentPosition: Int = 0
    private var sharingLink: String = ""
    private val mRandom = Random()
    private var llMainComment: LinearLayout? = null
    private var llReportSpam: LinearLayout? = null
    private var rvComments: RecyclerView? = null
    private var ivClose: AppCompatImageView? = null
    var comments: AppCompatTextView? = null
    private var ivSend: AppCompatImageView? = null
    private var edtComment: AppCompatEditText? = null

    var isLogin: Boolean = false
    var otherUserId: String = ""
    private var viewpagerposition: Int = 0
    private var commentsList = ArrayList<CommentResponse>()
    var userId: String = ""
    private var postCommentCount: String? = null
    var followerMap: HashMap<String, String> = HashMap()
    private var likeCount: Int = 0
    var commentBottomSheet: CommentBottomSheet? = null
    private var spamBottomSheet: SpamBottomSheet? = null

    var postID: String = ""
    var notificationType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_detail_activity)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()

        if (intent != null) {
            postID = intent.getStringExtra("postID")!!
            notificationType = intent.getStringExtra("notification_type")!!
        }

        avatar.setOnClickListener {
            onBackPressed()
        }
        llMainComment = findViewById(R.id.llMainComment)
        llReportSpam = findViewById(R.id.ll_report_spam)
        rvComments = findViewById<View>(R.id.rv_comments) as RecyclerView
        comments = findViewById<View>(R.id.comments) as AppCompatTextView
        ivClose = findViewById<View>(R.id.iv_close) as AppCompatImageView
        ivSend = findViewById<View>(R.id.iv_send) as AppCompatImageView
        edtComment = findViewById<View>(R.id.edt_comment) as AppCompatEditText
        rvComments!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        Methods.setImageRotation(imgExtractAudio)
        ivFlag.setOnClickListener {

            if (postID != null) {
                if (adapter1?.fragments != null) {
                    val lastFragment = adapter1?.fragments?.get(lastPosition)
                    lastFragment?.stopPlayer()
                }

                if (spamBottomSheet == null || !spamBottomSheet?.isVisible!!) {
                    spamBottomSheet = SpamBottomSheet.newInstance(
                        userId, "",
                        Integer.parseInt(postID), this@NotificationDetailActivity
                    )
                    spamBottomSheet?.show(supportFragmentManager, "SpamBottomSheet")
                }
            }

        }

        if (postID != "") {
            getVideoForNotification()
        }

        comment.setOnClickListener {
            if (adapter1?.fragments != null) {
                val lastFragment = adapter1?.fragments?.get(lastPosition)
                lastFragment?.stopPlayer()
            }
            commentBottomSheet = CommentBottomSheet.newInstance(
                userId,
                Integer.parseInt(postID), postCommentCount!!, this@NotificationDetailActivity
            )
            commentBottomSheet?.show(supportFragmentManager, "Comment_Bottom")

        }


    }


    private fun getVideoForNotification() {
        Logger.print("post_id=======$postID")
        val hashMap = HashMap<String, String>()
        hashMap["post_id"] = postID

        viewModel.getVideoDetails(this, hashMap, true).observe(
            this, Observer {
                if (it != null) {
                    Logger.print("VIDEO DETAILS ======$it")
                    val jsonObject = JSONObject(it.toString())
                    val code = jsonObject.getInt("code")
                    if (code == 1) {
                        if (jsonObject.has("data")) {

                            try {
                                videoDetailResponse =
                                    Gson().fromJson(
                                        jsonObject.getString("data").toString(),
                                        object : TypeToken<ForYouResponse>() {}.type
                                    )
                            } catch (e: JsonParseException) {
                                e.printStackTrace()
                            }

                            followerMap.clear()
                            if (videoDetailResponse.toString().isNotEmpty()) {
                                videoList.add(videoDetailResponse)
                                for (i in videoList.indices) {
                                    followerMap[videoList[i].user_id.toString()] =
                                        videoList[i].is_following.toString()
                                }
                                setViewPagerAdapter1()
                                Logger.print("videoList!!!!!!!!!!!!!!!!!!!!!!!$videoList")
                            }
                        }
                    } else if (code == 0) {
                        Utils.showAlert(this, "", jsonObject["message"].toString())
                    }
                }
            }
        )
    }

    private fun setViewPagerAdapter1() {
        Log.d("call or not", "yes call")
        viewPagerPlayer.offscreenPageLimit = 2
        viewPagerPlayer.orientation = ViewPager2.ORIENTATION_VERTICAL

        if (adapter1 == null) {
            adapter1 = null
            adapter1 = PlayerViewPagerAdapter(this, this, this)
            adapter1?.addFragment1(videoList)
            viewPagerPlayer.adapter = adapter1
        } else {
            Handler().run {
                adapter1?.addFragment1(videoList)
                adapter1?.notifyDataSetChanged()
            }
        }

        viewPagerPlayer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position < videoList.size) {
                    super.onPageSelected(position)
                    currentPosition = position

                    /*clearReportSelection()
                    setSelection()*/
                    if (userId.contentEquals(videoList[position].user_id.toString())) {
                        flag_layout.visibility = View.GONE
                    } else {
                        flag_layout.visibility = View.VISIBLE
                    }

                    println("==onPageSelected Main $position")
                    if (lastPosition != -1) {
//                        downloadHandler.removeCallbacks(downloadRunnable)
                        val lastFragment = adapter1?.fragments?.get(lastPosition)
                        lastFragment?.stopPlayer()
                    }

                    if (commentBottomSheet != null && commentBottomSheet?.isVisible!!) {
                        commentBottomSheet?.dismiss()
                    }
                    previousPosition = lastPosition
                    lastPosition = position

                    if (videoList.size > 0) {
                        viewpagerposition = position
                        commentsList.clear()
                        side_menu.visibility = View.VISIBLE
                        iv_user_profile.setImageBitmap(null)
                        otherUserId = videoList[position].user_id.toString()
                        val currentUserId =
                            SharedPreferenceUtils.getInstance(this@NotificationDetailActivity)
                                .getStringValue(Constants.USER_ID_PREF, "")!!

                        Logger.print("currentUserId=====================$currentUserId")
                        Logger.print("currentUserId ***********************=====================" + videoList[position].user_id.toString())

                        if (!otherUserId.contentEquals("")) {
                            iv_user_profile.setOnClickListener {
                                if (!userId.contentEquals(otherUserId)) {
                                    val intent = Intent(
                                        this@NotificationDetailActivity,
                                        OtherUserProfileActivity::class.java
                                    )
                                    intent.putExtra("OTHER_USER_ID", otherUserId)
                                    startActivity(intent)

                                } else {
                                    val fragment: Fragment
                                    fragment = MyProfileFragment()
                                    supportFragmentManager.beginTransaction()
                                        .replace(R.id.container, fragment).commit()
                                }
                            }
                        }

                        tv_userName.setOnClickListener {
                            if (otherUserId != "") {
                                if (!userId.contentEquals(otherUserId)) {
                                    val intent = Intent(
                                        this@NotificationDetailActivity,
                                        OtherUserProfileActivity::class.java
                                    )
                                    intent.putExtra("OTHER_USER_ID", otherUserId)
                                    startActivity(intent)

                                } else {
                                    val fragment: Fragment
                                    fragment = MyProfileFragment()
                                    supportFragmentManager.beginTransaction()
                                        .replace(R.id.container, fragment).commit()
                                }
                            }
                        }
                        iv_user_profile.visibility = View.VISIBLE
                        if (videoList[lastPosition].profile_pic_compres != null && !videoList[lastPosition].profile_pic_compres!!.contentEquals(
                                ""
                            )
                        ) {
                            iv_user_profile.setImageResource(R.drawable.userpicfinal)
                            Glide.with(this@NotificationDetailActivity)
                                .load(videoList[lastPosition].profile_pic_compres)
                                .error(R.drawable.profile_thumb)
                                .centerCrop()
                                .into(iv_user_profile)
                        } else {
                            iv_user_profile.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            iv_user_profile.setImageResource(R.drawable.profile_thumb)
                        }

                        if (videoList[position].no_of_followers != null && videoList[position].no_of_followers != "") {
                            llView.visibility = View.VISIBLE
                            tv_follower_counts.visibility = View.VISIBLE
                            Methods.showFollowerCounts(
                                videoList[position].no_of_followers.toString(),
                                tv_follower_counts, this@NotificationDetailActivity
                            )
                        } else {
                            llView.visibility = View.GONE
                            tv_follower_counts.visibility = View.GONE
                        }

                        postID = videoList[position].post_id.toString()
                        postCommentCount = videoList[position].post_comment_count

                        if (notificationType.contentEquals("2") || notificationType.contentEquals("3")) {
                            adapter1?.fragments!![currentPosition].stopPlayer()
                            commentBottomSheet = CommentBottomSheet.newInstance(
                                userId,
                                Integer.parseInt(postID),
                                postCommentCount!!,
                                this@NotificationDetailActivity
                            )
                            commentBottomSheet?.show(supportFragmentManager, "Comment_Bottom")
                            /*pageCount = 1
                            getComments(rvComments!!, postID.toInt())
                            behavior?.state = BottomSheetBehavior.STATE_EXPANDED*/
                        } else {
                            val lastFragment = adapter1?.fragments?.get(position)
                            lastFragment?.setVolumeOfVideo()
                        }

                        Logger.print("post_ID==============$postID")
                        forward_layout.setOnClickListener {
                            createLinks(position)
                        }

                        imgDownload.setOnClickListener {
                            val videoWidth = videoList[position].post_width
                            val videoHeight = videoList[position].post_height
                            if (videoHeight != null && videoWidth != null) {
                                requestPermission(
                                    videoList[position].post_video!!,
                                    videoWidth,
                                    videoHeight,
                                    true
                                )
                            } else {
                                requestPermission(videoList[position].post_video!!, 0, 0, true)
                            }
                        }

                        imgExtractAudio.setOnClickListener {
                            isLogin =
                                SharedPreferenceUtils.getInstance(this@NotificationDetailActivity)
                                    .getBoolanValue(Constants.IS_LOGIN, false)
                            if (isLogin) {
                                if (videoList[position].post_video != null && videoList[position].post_video != "") {
                                    // askPermission(videoList[position].post_video.toString())
                                    requestPermission(videoList[position].post_video!!, 0, 0, false)
                                }
                            } else {
                                val intent = Intent(
                                    this@NotificationDetailActivity,
                                    LoginActivity::class.java
                                )
                                intent.putExtra("screen_name", "startPost")
                                startActivityForResult(
                                    intent,
                                    Constants.LOGIN_ACTIVITY_REQUEST_CODE
                                )
                            }


                        }
                        if (videoList[position].mark_as_verified_badge != null && videoList[position].mark_as_verified_badge!! == 1) {
                            ivVerified?.visibility = View.VISIBLE

                        } else {
                            ivVerified?.visibility = View.GONE

                        }
                        if (videoList[position].profile_name != null && !videoList[position].profile_name!!.contentEquals(
                                ""
                            )
                        ) {
                            tv_userName.text = videoList[position].profile_name
                        }

                        if (videoList[position].category_name != null && !videoList[position].category_name!!.contentEquals(
                                ""
                            )
                        ) {
                            tv_cat_name.text = videoList[position].category_name
                        }

                        if (videoList[position].post_title != null && videoList[position].post_title != "") {
                            if (videoList[position].post_title!!.toString().contains("#")) {
                                if (videoList[position].post_title!!.toString().trim()
                                        .contains(" ")
                                ) {
                                    Logger.print(" contain space only multiple hash tag hash tag @@@@@@@@@@@@@" + videoList[position].post_title!!.toString())
                                    val str = videoList[position].post_title!!.toString()
                                    val splited = str.split(" ").toTypedArray()

                                    Logger.print("splited!!!!!!!!!!!" + splited.size)
                                    Logger.print("splited one value!!!!!!!!!!!" + splited[0])
                                    Logger.print("splited two value!!!!!!!!!!!" + splited[1])

                                    val builder = SpannableStringBuilder()
                                    for (word in splited) {
                                        builder.append(word).append(" ")
                                            .setSpan(object : ClickableSpan() {
                                                override fun onClick(@NonNull view: View) {
                                                    if (word.startsWith("#")) {
                                                        val intent = Intent(
                                                            this@NotificationDetailActivity,
                                                            HashTagDetailOrTrendingDetail::class.java
                                                        )
                                                        intent.putExtra("hashTag", word)
                                                        startActivity(intent)
                                                    }
                                                } // optional - for styling the specific text

                                                override fun updateDrawState(ds: TextPaint) {
                                                    super.updateDrawState(ds)
                                                    ds.color = ContextCompat.getColor(
                                                        this@NotificationDetailActivity,
                                                        R.color.hash_tag
                                                    )
                                                    ds.isUnderlineText = false
                                                }
                                            }, builder.length - word.length, builder.length, 0)
                                    }
                                    tvPostTitle.setText(builder, TextView.BufferType.SPANNABLE)
                                    tvPostTitle.movementMethod = LinkMovementMethod.getInstance()
                                    tvPostTitle.setTextColor(
                                        ContextCompat.getColor(
                                            this@NotificationDetailActivity,
                                            R.color.hash_tag
                                        )
                                    )
                                    tvPostTitle.setOnClickListener {}
                                } else {
                                    Logger.print("not contain space only one hash tag @@@@@@@@@@@@@" + videoList[position].post_title!!.toString())
                                    tvPostTitle.text = videoList[position].post_title
                                    tvPostTitle.setTextColor(
                                        ContextCompat.getColor(
                                            this@NotificationDetailActivity,
                                            R.color.hash_tag
                                        )
                                    )

                                    tvPostTitle.setOnClickListener {
                                        val intent = Intent(
                                            this@NotificationDetailActivity,
                                            HashTagDetailOrTrendingDetail::class.java
                                        )
                                        intent.putExtra(
                                            "hashTag",
                                            videoList[position].post_title.toString()
                                        )
                                        startActivity(intent)
                                    }
                                }
                            } else {
                                tvPostTitle.text = videoList[position].post_title
                                tvPostTitle.setTextColor(
                                    ContextCompat.getColor(
                                        this@NotificationDetailActivity,
                                        R.color.white
                                    )
                                )
                                tvPostTitle.setOnClickListener {}
                            }
                        }

                        var isFollowingUser = ""
                        for (key in followerMap.keys) {
                            isFollowingUser = followerMap[videoList[position].user_id.toString()]!!
                        }
                        if (isFollowingUser.contentEquals("1")) {
                            tv_follow_.visibility = View.GONE
                            tv_following_user.visibility = View.VISIBLE
                        }
                        if (isFollowingUser.contentEquals("0")) {
                            tv_follow_.visibility = View.VISIBLE
                            tv_following_user.visibility = View.INVISIBLE
                        }
                        if (videoList[position].user_id.toString() == currentUserId) {
                            tv_follow_.visibility = View.GONE
                            tv_following_user.visibility = View.INVISIBLE
                        }

                        tv_follow_.setOnClickListener {
                            isLogin =
                                SharedPreferenceUtils.getInstance(this@NotificationDetailActivity)
                                    .getBoolanValue(Constants.IS_LOGIN, false)
                            if (isLogin) {
                                tv_follow_.visibility = View.GONE
                                tv_following_user.visibility = View.VISIBLE
                                followerMap[videoList[position].user_id.toString()] = "1"
                                followUser(videoList[position].user_id.toString())
                            } else {
                                val intent = Intent(
                                    this@NotificationDetailActivity,
                                    LoginActivity::class.java
                                )
                                intent.putExtra("screen_name", "NotificationDetail")
                                startActivityForResult(
                                    intent,
                                    Constants.LOGIN_ACTIVITY_REQUEST_CODE
                                )
                            }
                        }

                        tv_following_user.setOnClickListener {
                            isLogin =
                                SharedPreferenceUtils.getInstance(this@NotificationDetailActivity)
                                    .getBoolanValue(Constants.IS_LOGIN, false)
                            if (isLogin) {
                                tv_following_user.visibility = View.INVISIBLE
                                tv_follow_.visibility = View.VISIBLE
                                followerMap[videoList[position].user_id.toString()] = "0"
                                unFollowUser(videoList[position].user_id.toString())
                            } else {
                                val intent = Intent(
                                    this@NotificationDetailActivity,
                                    LoginActivity::class.java
                                )
                                intent.putExtra("screen_name", "NotificationDetail")
                                startActivityForResult(
                                    intent,
                                    Constants.LOGIN_ACTIVITY_REQUEST_CODE
                                )
                            }
                        }

                        if (videoList[position].post_view_count != null && videoList.toString() != "") {
                            Methods.showViewCounts(
                                videoList[position].post_view_count.toString(),
                                tvpostCount
                            )

                        }

                        if (videoList[position].has_liked.toString().contentEquals("1")) {
                            like_image.setImageResource(R.drawable.like_active)
                        }
                        if (videoList[position].has_liked.toString().contentEquals("0")) {
                            like_image.setImageResource(R.drawable.like_new)
                        }
                        if (videoList[position].post_like_count != null && !videoList[position].post_like_count!!.contentEquals(
                                ""
                            )
                        ) {
                            Methods.showLikeCounts(
                                videoList[position].post_like_count.toString(),
                                like_txt
                            )
                        }

                        like_image.setOnClickListener {
                            if (videoList[position].has_liked == 0) {
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
                                    Integer.parseInt(videoList[position].post_like_count!!) + 1

                                Methods.showLikeCounts(likeCounts.toString(), like_txt)
                                likeUnlikeVideo(videoList[position].post_id, likeCount)
                                videoList[position].post_like_count = "$likeCounts"
                                videoList[position].has_liked = 1
                            } else {
                                likeCount = 0
                                like_image.setImageResource(R.drawable.like_new)
                                if (!videoList[position].post_like_count.toString()
                                        .contentEquals("0")
                                ) {
                                    val likeCounts =
                                        Integer.parseInt(videoList[position].post_like_count!!) - 1
                                    Logger.print("unlike Counts!!!!!!!!!!!!!!!!!!!!!!!!!!$likeCounts")
                                    Methods.showLikeCounts(likeCounts.toString(), like_txt)

                                    likeUnlikeVideo(videoList[position].post_id, likeCount)
                                    videoList[position].post_like_count = "$likeCounts"
                                    videoList[position].has_liked = 0
                                }
                            }
                        }

                        if (videoList[position].post_comment_count != null && !videoList[position].post_comment_count!!.contentEquals(
                                ""
                            )
                        ) {
                            Methods.showCommentCounts(
                                videoList[position].post_comment_count.toString(),
                                comment_txt
                            )
                        }

                        if (videoList[position].post_comment_count != null && !videoList[position].post_comment_count!!.contentEquals(
                                ""
                            )
                        ) {
                            Methods.showCommentCountsDialog(
                                videoList[position].post_comment_count!!.toString(),
                                comments!!, this@NotificationDetailActivity
                            )
                        }
                    }
                }
            }

            /*override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                Logger.print("onPageScrollStateChanged $state")
                if(state == 1) {
                    isOnScrollCall = true
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                Logger.print("onPageScrolled Position $position PositionOffset $positionOffset PositionOffsetPixels $positionOffsetPixels")
                if(isOnScrollCall) {
                    isOnScrollCall = false
                    if(positionOffset < 0.2f) {
                        if(position < videoList.size - 1) {
                            Logger.print("Going to play video at ${position + 1}")
                            val lastFragment = adapter1?.fragments?.get(position + 1)
                            lastFragment?.playVideo(false)
                        }
                    } else {
                        if(position > 0) {
                            Logger.print("Going to play video at ${position - 1}")
                            val lastFragment = adapter1?.fragments?.get(position - 1)
                            lastFragment?.playVideo(false)
                        }
                    }
                }
            }*/
        })
        viewPagerPlayer.setCurrentItem(0, false)
        val lastFragment = adapter1?.fragments?.get(0)
        lastFragment?.playVideo(true)
    }

    private fun randomColor(): Int {
        return Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255))
    }

    private fun likeUnlikeVideo(post_id: Int?, like_unlike: Int) {
        val hashMap = HashMap<String, String>()
        hashMap["is_like"] = like_unlike.toString()
        hashMap["post_id"] = post_id.toString()
//        val apiName: String = APIConstants.LIKEUNLIKEVIDEO
        viewModel.likeUnlikeVideo(this, hashMap, false).observe(this, Observer {
            if (it != null) {
                Logger.print("LIKE VIDEO OR UNLIKE VIDEO!!!!!!!!!!!!!!!!!!!$it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 0) {
                    Utils.showAlert(this, "", jsonObject["message"].toString())
                }
            }
        }
        )
    }

    private fun createLinks(position: Int) {
        val iOSParams = DynamicLink.IosParameters.Builder("com.smylee.app")
        iOSParams.appStoreId = Constants.APP_STORE_ID

        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(createShareUri(videoList[position].post_id.toString()))
            .setDomainUriPrefix("https://link.smylee.app")
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder("smylee.app").build())
            .setIosParameters(iOSParams.build())
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters
                    .Builder()
                    .setTitle(videoList[position].post_title!!)
                    .setImageUrl(Uri.parse(videoList[position].post_video_thumbnail))
                    .build()
            )
            .buildShortDynamicLink()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    sharingLink = it.result!!.shortLink.toString()
                    Logger.print("Sharing Link $sharingLink")
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
                    Logger.print("Exception : " + it.exception)
                }
            }
    }

    private fun createShareUri(postId: String): Uri {
        val builder = Uri.Builder()
        builder.scheme("https")
            .authority("link.smylee.app")
            .appendPath("video")
            .appendQueryParameter("postId", postId)
        return builder.build()
    }

    /*fun clearReportSelection() {
        rgReport.clearCheck()
    }*/

    override fun onVideoStartPlaying(position: Int) {
        Log.i("ForYouFragment", "Position ${lastPosition + 1} Array size ${videoList.size}")
        /*if(previousPosition > -1) {
            val previousFragment = adapter1!!.fragments[previousPosition]
            previousFragment.stopPlayer()
        }*/
        /*if ((lastPosition + 1) < videoList.size) {
            downloadHandler.removeCallbacks(downloadRunnable)
            downloadHandler.postDelayed(downloadRunnable, 2000)
        }*/
    }

    /*val downloadHandler = Handler()
    val downloadRunnable = Runnable {
        preDownload(videoList[lastPosition + 1].post_video, null, lastPosition + 1, this)
    }*/

    override fun onVideoViewCountUpdate(position: Int, videoViewCount: String) {
        videoList[position].isViewed = true
    }

    override fun onInitNextCall(currentPos: Int) {
        Log.i("ForYouFragment", "InitNext Position ${currentPos + 1} Array size ${videoList.size}")
        /*if ((currentPos + 1) < videoList.size && (currentPos + 1) < (lastPosition + 6)) {
            preDownload(videoList[currentPos + 1].post_video, null, currentPos + 1, this)
        }*/
    }

    override fun onVideoReadyToShare(videoPath: String) {
        hideProgressDialog()
        Methods.deleteFileWithZeroKB(Constants.DOWNLOADED_PATH)

        Utils.showToastMessage(this, getString(R.string.video_downloaded))
        val gifFile = File(Constants.ROOT_PATH, "smylee_gif_new.gif")
        if (gifFile.exists()) {
            gifFile.delete()
        }
    }


    override fun onDoubleTAP(clickState: Int) {

        when (clickState) {
            1 -> {
                Logger.print("call single click fragment ********")
                if (spamBottomSheet != null && spamBottomSheet!!.isVisible) {
                    spamBottomSheet?.dismiss()
                } else if (commentBottomSheet != null && commentBottomSheet!!.isVisible) {
                    commentBottomSheet?.dismiss()
                }
            }
            2 -> {
                Logger.print("double tap for you fragment===============================")
                imgLikeDoubleTap.visibility = View.VISIBLE
                Methods.animateHeart(imgLikeDoubleTap)

                if (videoList[currentPosition].has_liked == 0) {
                    likeCount = 1
                    like_image.setImageResource(R.drawable.like_active)

                    val likeCounts =
                        Integer.parseInt(videoList[currentPosition].post_like_count!!) + 1
                    Methods.showLikeCounts(likeCounts.toString(), like_txt)

                    likeUnlikeVideo(videoList[currentPosition].post_id, likeCount)
                    videoList[currentPosition].post_like_count = "$likeCounts"
                    videoList[currentPosition].has_liked = 1
                }
            }
        }


    }

    private fun requestPermission(
        videoLink: String,
        videoWidth: Int,
        videoHeight: Int,
        isDownload: Boolean
    ) {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (isDownload) {
                    if (report.areAllPermissionsGranted()) {
                        showProgressDialog()
                        ShareVideoUtils.shareVideoFile(
                            this@NotificationDetailActivity,
                            videoLink,
                            videoWidth,
                            videoHeight,
                            this@NotificationDetailActivity
                        )
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog()
                    }
                } else {
                    if (report.areAllPermissionsGranted()) {
                        showProgressDialog()
                        val rootDirectory = File(Constants.EXTRACT_PATH)
                        if (rootDirectory.exists()) {
                            Methods.deleteFilesFromDirectory(Constants.EXTRACT_PATH)
                        }
                        ShareVideoUtils.extractVideoFile(
                            this@NotificationDetailActivity,
                            videoLink,
                            this@NotificationDetailActivity
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
            Toast.makeText(this, "Error occurred! ", Toast.LENGTH_SHORT).show()
        }.onSameThread().check()
    }


    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    private fun followUser(userId: String) {
        val isFollow = "1"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = userId

//        val apiName: String = APIConstants.FOLLOWUNFOLLOWUSER
        viewModel.FollowUnfollow(this, hashMap).observe(
            this, Observer {
                if (it != null) {
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                    Logger.print("FOLLOWUNFOLLOWUSER Response : $it")
                }
            }
        )
    }

    private fun unFollowUser(userID: String) {
        val isFollow = "0"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = userID
//        val apiName: String = APIConstants.FOLLOWUNFOLLOWUSER
        viewModel.FollowUnfollow(this, hashMap).observe(
            this, Observer {
                if (it != null) {
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                    Logger.print("Unfollowuser Whom im following Response : $it")
                }
            }
        )
    }


    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.message_need_permission))
        builder.setMessage(getString(R.string.message_permission))
        builder.setPositiveButton(getString(R.string.title_go_to_setting)) { dialog, _ ->
            dialog.cancel()
            openSettings()
        }
        builder.show()
    }

    override fun onExtractFinished() {
        hideProgressDialog()
        Methods.deleteFilesFromDirectory(Constants.EXTRACT_PATH)
        var nameFile = Methods.fileFromDir(Constants.Audio_PATH)
        if (nameFile!!.contains(".mp4")) {
            nameFile = nameFile.replace(".mp4", "")
        }

        val isLogin = SharedPreferenceUtils.getInstance(this)
            .getBoolanValue(Constants.IS_LOGIN, false)
        if (isLogin) {
            val intent = Intent(this, CameraVideoRecording::class.java)
            intent.putExtra("ISAUDIOMIX", true)
            intent.putExtra("extractAudio", true)
            intent.putExtra("audioFileNameFinal", nameFile)
            startActivity(intent)
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("screen_name", "startPost")
            startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
        }
    }

    override fun onCommentAdded(postId: Int, tvCommnetCountBottomSheet: AppCompatTextView) {
        val postIdCurrent = Integer.parseInt(postID)
        if (lastPosition != -1 && postId == postIdCurrent) {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "AddCommentForYou")
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, postID)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

            val commentsCount = Integer.parseInt(videoList[lastPosition].post_comment_count!!) + 1
            postCommentCount = "$commentsCount"
            videoList[lastPosition].post_comment_count = "$commentsCount"
            adapter1?.notifyItemChanged(lastPosition)
            if (commentsCount.toString() != "") {
                Methods.showCommentCounts(commentsCount.toString(), comment_txt)
                Methods.showCommentCountsDialog(
                    commentsCount.toString(),
                    tvCommnetCountBottomSheet,
                    this
                )

            }
        }
    }

    override fun onResume() {
        if (adapter1?.fragments != null) {
            adapter1?.fragments!![currentPosition].playVideo(true)
        }
        super.onResume()
    }

    override fun onPause() {
        if (adapter1?.fragments != null) {
            adapter1?.fragments!![currentPosition].stopPlayer()
        }
        if (spamBottomSheet != null && spamBottomSheet?.isVisible != null && spamBottomSheet?.isVisible!! && commentBottomSheet != null && commentBottomSheet?.isVisible != null && commentBottomSheet?.isVisible!!) {
            spamBottomSheet?.dismiss()
            commentBottomSheet?.dismiss()
            Utils.hideKeyboard(this)
        } else if (spamBottomSheet != null && spamBottomSheet?.isVisible != null && spamBottomSheet?.isVisible!!) {
            spamBottomSheet?.dismiss()
        } else if (commentBottomSheet != null && commentBottomSheet?.isVisible != null && commentBottomSheet?.isVisible!!) {
            commentBottomSheet?.dismiss()
        }
        super.onPause()
    }

    override fun onDialogDismiss() {
        Utils.hideKeyboard(this)
//        enableIcons()
        if (adapter1?.fragments != null) {
            adapter1?.fragments!![currentPosition].playVideo(true)
        }
    }

    override fun onSpamComment(commentId: String) {
        if (spamBottomSheet == null || !spamBottomSheet?.isVisible!!) {
            spamBottomSheet = SpamBottomSheet.newInstance(
                userId, commentId,
                Integer.parseInt(postID), this@NotificationDetailActivity
            )
            spamBottomSheet?.show(supportFragmentManager, "SpamBottomSheet")
        }
    }

    override fun onSelfProfileClick() {

    }

    override fun onSpamDialogDismiss() {
        Utils.hideKeyboard(this@NotificationDetailActivity)
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