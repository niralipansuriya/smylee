//package smylee.app.playvideo
//
//import android.Manifest
//import android.app.AlertDialog
//import android.content.Intent
//import android.graphics.Color
//import android.net.Uri
//import android.os.Bundle
//import android.os.Handler
//import android.provider.Settings
//import android.text.SpannableStringBuilder
//import android.text.TextPaint
//import android.text.method.LinkMovementMethod
//import android.text.style.ClickableSpan
//import android.util.Log
//import android.view.View
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.annotation.NonNull
//import androidx.appcompat.widget.AppCompatEditText
//import androidx.appcompat.widget.AppCompatImageView
//import androidx.appcompat.widget.AppCompatTextView
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProviders
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import androidx.viewpager2.widget.ViewPager2
//import com.bumptech.glide.Glide
//import com.google.firebase.analytics.FirebaseAnalytics
//import com.google.firebase.dynamiclinks.DynamicLink
//import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
//import com.karumi.dexter.Dexter
//import com.karumi.dexter.MultiplePermissionsReport
//import com.karumi.dexter.PermissionToken
//import com.karumi.dexter.listener.PermissionRequest
//import com.karumi.dexter.listener.multi.MultiplePermissionsListener
//import kotlinx.android.synthetic.main.activity_video_play.*
//import org.json.JSONObject
//import smylee.app.CallBacks.DoubleTapCallBacks
//import smylee.app.CallBacks.ExtractListner
//import smylee.app.Profile.MyProfileFragment
//import smylee.app.Profile.OtherUserProfileActivity
//import smylee.app.R
//import smylee.app.URFeedApplication
//import smylee.app.adapter.PlayerViewPagerAdapter
//import smylee.app.dialog.CommonAlertDialog
//import smylee.app.home.CommentBottomSheet
//import smylee.app.home.HomeViewModel
//import smylee.app.home.SpamBottomSheet
//import smylee.app.listener.*
//import smylee.app.login.LoginActivity
//import smylee.app.model.ForYouResponse
//import smylee.app.ui.Activity.CameraVideoRecording
//import smylee.app.ui.Activity.HashTagDetailOrTrendingDetail
//import smylee.app.ui.base.BaseActivity
//import smylee.app.utils.*
//import java.io.File
//import java.util.*
//import kotlin.collections.HashMap
//
////class VideoPlayActivity : smylee.app.ui.base.BaseActivity(), OnVideoPlayingListener, InitNextVideoListener,
//class VideoPlayActivity : BaseActivity(), OnVideoPlayingListener, InitNextVideoListener,
//    OnShareVideoPrepareListener, DoubleTapCallBacks, ExtractListner, OnCommentAdded,
//    OnSpamDialogListener {
//
//    private var adapter1: PlayerViewPagerAdapter? = null
//    private var ivClose: ImageView? = null
//    var hasLikedMap: HashMap<String, String> = HashMap()
//    private val mRandom = Random()
//    var likeVideoHash: HashMap<String, String> = HashMap()
//    private var likeCount: Int = 0
//    var otherUserId: String = ""
//    var userId: String = ""
//    private var catID: String = ""
//    private var currentPosition: Int = 0
//    var isLogin: Boolean = false
//    private var viewpagerposition: Int = 0
//    private lateinit var firebaseAnalytics: FirebaseAnalytics
//
//    var followerMap: HashMap<String, String> = HashMap()
//    var comments: AppCompatTextView? = null
//    private var ivSend: AppCompatImageView? = null
//    private var edtComment: AppCompatEditText? = null
//    private var rvComments: RecyclerView? = null
//
//    var trendingList: ArrayList<ForYouResponse>? = null
//    var lastPosition = -1
//    var previousPosition = -1
//    var pos: Int = 0
//    lateinit var viewModel: HomeViewModel
//    private var isDiscover: Boolean = false
//    private var isProfile: Boolean = false
//    private var isShowDelete: Boolean = false
//    var commentCountMap: HashMap<String, String> = HashMap()
//    private var postId: Int? = null
//    private var postCommentCount = "0"
//    var commentBottomSheet: CommentBottomSheet? = null
//    private var spamBottomSheet: SpamBottomSheet? = null
//
//    private var sharingLink: String = ""
//    private var isOnScrollCall = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_video_play)
//        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
//
//        avatar.setOnClickListener {
//            onBackPressed()
//        }
//        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
//
//        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
//            .getStringValue(Constants.USER_ID_PREF, "").toString()
//
//        if (intent != null) {
//            trendingList = intent.getParcelableArrayListExtra("response")
//            pos = intent.getIntExtra("position", 0)
//            isDiscover = intent.getBooleanExtra("is_discover", false)
//            isProfile = intent.getBooleanExtra("isProfile", false)
//            catID = intent.getStringExtra("cat_ID")!!
//            isShowDelete = intent.getBooleanExtra("isShowDelete", false)
//
//            likeVideoHash = intent.getSerializableExtra("likeCountMap") as HashMap<String, String>
//            hasLikedMap = intent.getSerializableExtra("hasLikedMap") as HashMap<String, String>
//            if (!isProfile) {
//                followerMap = intent.getSerializableExtra("followerMap") as HashMap<String, String>
//            }
//        }
//
//        Methods.setImageRotation(imgExtractAudio)
//        rvComments = findViewById<View>(R.id.rv_comments) as RecyclerView
//        comments = findViewById<View>(R.id.comments) as AppCompatTextView
//        ivClose = findViewById<View>(R.id.iv_close) as ImageView
//        ivSend = findViewById<View>(R.id.iv_send) as AppCompatImageView
//        edtComment = findViewById<View>(R.id.edt_comment) as AppCompatEditText
//        rvComments!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//
//        if (isShowDelete) {
//            delele_post_ll.visibility = View.VISIBLE
//        } else {
//            delele_post_ll.visibility = View.GONE
//        }
//
//        ivFlag.setOnClickListener {
//            if (postId != null) {
//                if (adapter1?.fragments != null) {
//                    val lastFragment = adapter1?.fragments?.get(lastPosition)
//                    lastFragment?.stopPlayer()
//                }
//                if (spamBottomSheet == null || !spamBottomSheet?.isVisible!!) {
//                    spamBottomSheet = SpamBottomSheet.newInstance(userId, "", postId!!, this@VideoPlayActivity)
//                    spamBottomSheet?.show(supportFragmentManager, "SpamBottomSheet")
//                }
//            }
//        }
//
//        if (trendingList!!.size > 0) {
//            setViewPagerAdapter1()
//
//        }
//        if (pos == 0) {
//            if (trendingList!![pos].post_comment_count != null && trendingList!![pos].post_comment_count != "") {
//                Methods.showCommentCounts(
//                    trendingList!![pos].post_comment_count.toString(),
//                    comment_txt
//                )
//            }
//            if (trendingList!![pos].post_view_count != null && trendingList!![pos].post_view_count != "") {
//                Methods.showViewCounts(trendingList!![pos].post_view_count.toString(), tvpostCount)
//            }
//            if (trendingList!![pos].profile_name != null) {
//                tv_userName.text = trendingList!![pos].profile_name.toString()
//            }
//            if (trendingList!![pos].category_name != null) {
//                tv_cat_name.text = trendingList!![pos].category_name
//            }
//
//            if (trendingList!![pos].post_title != null && trendingList!![pos].post_title != "") {
//                if (trendingList!![pos].post_title!!.toString().contains("#")) {
//                    if (trendingList!![pos].post_title!!.toString().trim().contains(" ")) {
//                        val str = trendingList!![pos].post_title!!.toString()
//                        val splited = str.split(" ").toTypedArray()
//
//                        val builder = SpannableStringBuilder()
//                        for (word in splited) {
//                            builder.append(word).append(" ").setSpan(object : ClickableSpan() {
//                                override fun onClick(@NonNull view: View) {
//                                    if (word.startsWith("#")) {
//                                        val intent = Intent(
//                                            this@VideoPlayActivity,
//                                            HashTagDetailOrTrendingDetail::class.java
//                                        )
//                                        intent.putExtra("hashTag", word)
//                                        startActivity(intent)
//                                    }
//                                }
//
//                                override fun updateDrawState(ds: TextPaint) {
//                                    super.updateDrawState(ds)
//                                    ds.color = ContextCompat.getColor(
//                                        this@VideoPlayActivity,
//                                        R.color.hash_tag
//                                    )
//                                    ds.isUnderlineText = false
//                                }
//                            }, builder.length - word.length, builder.length, 0)
//                        }
//                        //  ss.setSpan(clickableSpan1,startindex,endIndex,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                        tvPostTitle.setText(builder, TextView.BufferType.SPANNABLE)
//                        tvPostTitle.movementMethod = LinkMovementMethod.getInstance()
//                        tvPostTitle.setTextColor(
//                            ContextCompat.getColor(
//                                this@VideoPlayActivity,
//                                R.color.hash_tag
//                            )
//                        )
//                        tvPostTitle.setOnClickListener {}
//                    } else {
//                        Logger.print("not contain space only one hash tag @@@@@@@@@@@@@" + trendingList!![pos].post_title!!.toString())
//                        tvPostTitle.text = trendingList!![pos].post_title
//                        tvPostTitle.setTextColor(
//                            ContextCompat.getColor(
//                                this@VideoPlayActivity,
//                                R.color.hash_tag
//                            )
//                        )
//
//                        tvPostTitle.setOnClickListener {
//                            val intent = Intent(
//                                this@VideoPlayActivity,
//                                HashTagDetailOrTrendingDetail::class.java
//                            )
//                            intent.putExtra("hashTag", trendingList!![pos].post_title.toString())
//                            startActivity(intent)
//                        }
//                    }
//                } else {
//                    tvPostTitle.text = trendingList!![pos].post_title
//                    tvPostTitle.setTextColor(
//                        ContextCompat.getColor(
//                            this@VideoPlayActivity,
//                            R.color.white
//                        )
//                    )
//                    tvPostTitle.setOnClickListener {}
//                }
//            }
//
//            if (trendingList!![pos].no_of_followers != null && trendingList!![pos].no_of_followers != "") {
//                llView.visibility = View.VISIBLE
//                tv_follower_counts.visibility = View.VISIBLE
//                Methods.showFollowerCounts(
//                    trendingList!![pos].no_of_followers.toString(),
//                    tv_follower_counts,
//                    this
//                )
//            } else {
//                llView.visibility = View.GONE
//                tv_follower_counts.visibility = View.GONE
//            }
//
//            var likeCountInitial = ""
//            for (key in likeVideoHash.keys) {
//                likeCountInitial = likeVideoHash[trendingList!![pos].post_id.toString()]!!
//            }
//            if (likeCountInitial != "") {
//                Methods.showLikeCounts(likeCountInitial, like_txt)
//            }
//
//            Logger.print("postcounts in oncreate*******************************" + trendingList!![pos].post_comment_count)
//            var isFollowingUser = ""
//            for (key in followerMap.keys) {
//                isFollowingUser = followerMap[trendingList!![pos].user_id.toString()]!!
//            }
//            if (isFollowingUser.contentEquals("1")) {
//                tv_follow_.visibility = View.GONE
//                tv_following_user.visibility = View.VISIBLE
//            }
//            if (isFollowingUser.contentEquals("0")) {
//                tv_follow_.visibility = View.VISIBLE
//                tv_following_user.visibility = View.INVISIBLE
//            }
//
//            val currentUserId = SharedPreferenceUtils.getInstance(this@VideoPlayActivity)
//                .getStringValue(Constants.USER_ID_PREF, "")!!
//            if (trendingList!![pos].user_id.toString().contentEquals(currentUserId)) {
//                tv_follow_.visibility = View.GONE
//                tv_following_user.visibility = View.INVISIBLE
//            }
//        }
//
//        like_image.setOnClickListener {}
//        delele_post_ll.setOnClickListener {
//            val alertDialog = object :
//                CommonAlertDialog(this, theme = android.R.style.Theme_Translucent_NoTitleBar) {
//                override fun okClicked() {
//                    deletePost(trendingList!![lastPosition].post_id.toString(), lastPosition)
//                }
//
//                override fun cancelClicked() {
//                }
//            }
//            alertDialog.initDialog(
//                resources.getString(R.string.delete),
//                resources.getString(R.string.deletepost)
//            )
//            alertDialog.setCancelable(true)
//            alertDialog.show()
//            // showDelDialog(this, "${trendingList!![lastPosition].post_id}")
//        }
//
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (adapter1 != null && lastPosition != -1) {
//            if (lastPosition < adapter1?.fragments?.size!!) {
//                val lastFragment = adapter1?.fragments?.get(lastPosition)
//                lastFragment?.stopPlayer()
//            } else {
//                for (i in 0 until adapter1?.fragments?.size!!) {
//                    val lastFragment = adapter1?.fragments?.get(i)
//                    lastFragment?.stopPlayer()
//                }
//            }
//        }
//        super.onStop()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//    }
//
//    private fun setViewPagerAdapter1() {
//        Log.d("call==========", "setViewPagerAdapter1")
//        viewPagerPlayer.offscreenPageLimit = 1
//        viewPagerPlayer.orientation = ViewPager2.ORIENTATION_VERTICAL
//
//        if (adapter1 == null) {
//            adapter1 = null
//            adapter1 = PlayerViewPagerAdapter(this, this, this)
//            adapter1?.addFragment1(trendingList)
//            viewPagerPlayer.adapter = adapter1
//        } else {
//            adapter1?.addFragment1(trendingList)
//            adapter1?.notifyDataSetChanged()
//        }
//
//        viewPagerPlayer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                Log.d("call==========", "onPageSelected")
//                if (position < trendingList!!.size) {
//
//
//                    viewpagerposition = position
//                    currentPosition = position
//
//                    if (userId.contentEquals(trendingList!![position].user_id.toString())) {
//                        flag_layout.visibility = View.GONE
//                    } else {
//                        flag_layout.visibility = View.VISIBLE
//                    }
//
//                    if (trendingList!![position].no_of_followers != null && trendingList!![position].no_of_followers != "") {
//                        llView.visibility = View.VISIBLE
//                        tv_follower_counts.visibility = View.VISIBLE
//                        Methods.showFollowerCounts(
//                            trendingList!![position].no_of_followers.toString(),
//                            tv_follower_counts, this@VideoPlayActivity
//                        )
//                    } else {
//                        llView.visibility = View.GONE
//                        tv_follower_counts.visibility = View.GONE
//                    }
//
//                    if (commentBottomSheet != null && commentBottomSheet?.isVisible!!) {
//                        commentBottomSheet?.dismiss()
//                    }
//                    Log.d("call==========", "position<trendingList")
//                    println("==onPageSelected Main $position")
//                    if (lastPosition != -1) {
//                        val lastFragment = adapter1?.fragments?.get(lastPosition)
//                        lastFragment?.stopPlayer()
//                    }
//
//                    val currentFragment = adapter1?.fragments?.get(position)
//                    println("lastPosition !!!!!!!!!!!!!!" + lastPosition + "position!!!!!!!!!!!!!!" + position)
//                    currentFragment?.setVolumeOfVideo()
////                    currentFragment?.playVideo(true)
//                    previousPosition = lastPosition
//                    lastPosition = position
//                    postId = trendingList!![position].post_id
//                    postCommentCount = trendingList!![position].post_comment_count!!
//
//                    otherUserId = trendingList!![position].user_id.toString()
//                    if (!otherUserId.contentEquals("")) {
//                        iv_user_profile.setOnClickListener {
//                            if (!userId.contentEquals(otherUserId)) {
//                                adapter1?.fragments!![currentPosition].stopPlayer()
//                                val intent = Intent(
//                                    this@VideoPlayActivity,
//                                    OtherUserProfileActivity::class.java
//                                )
//                                intent.putExtra("OTHER_USER_ID", otherUserId)
//                                startActivity(intent)
//
//                            } else {
//                                adapter1?.fragments!![currentPosition].stopPlayer()
//                                val fragment: Fragment
//                                fragment = MyProfileFragment()
//                                supportFragmentManager.beginTransaction()
//                                    .replace(R.id.container, fragment).commit()
//                            }
//                        }
//                    }
//                    tv_userName.setOnClickListener {
//                        if (otherUserId != "") {
//                            if (!userId.contentEquals(otherUserId)) {
//                                if (!otherUserId.contentEquals("")) {
//                                    adapter1?.fragments!![currentPosition].stopPlayer()
//                                    val intent = Intent(
//                                        this@VideoPlayActivity,
//                                        OtherUserProfileActivity::class.java
//                                    )
//                                    intent.putExtra("OTHER_USER_ID", otherUserId)
//                                    startActivity(intent)
//                                }
//                            } else {
//                                adapter1?.fragments!![currentPosition].stopPlayer()
//                                val fragment: Fragment
//                                fragment = MyProfileFragment()
//                                supportFragmentManager.beginTransaction()
//                                    .replace(R.id.container, fragment).commit()
//                            }
//                        }
//                    }
//                    if (trendingList!![position].post_comment_count != null && trendingList!![position].post_comment_count != "") {
//                        Methods.showCommentCounts(
//                            trendingList!![position].post_comment_count.toString(),
//                            comment_txt
//                        )
//                    }
//
//                    if (trendingList!![lastPosition].profile_pic != null && !trendingList!![lastPosition].profile_pic!!.contentEquals(
//                            ""
//                        )
//                    ) {
//                        Glide.with(this@VideoPlayActivity)
//                            .load(trendingList!![lastPosition].profile_pic)
//                            .error(R.drawable.profile_thumb)
//                            .centerCrop()
//                            .into(iv_user_profile)
//                    } else {
//                        iv_user_profile.scaleType = ImageView.ScaleType.FIT_CENTER
//                        iv_user_profile.setImageResource(R.drawable.profile_thumb)
//                    }
//
//                    commentCountMap[trendingList!![position].post_id.toString()] =
//                        trendingList!![position].post_comment_count.toString()
//                    if (trendingList!![position].post_comment_count != null && !trendingList!![position].post_comment_count!!.contentEquals(
//                            ""
//                        )
//                    ) {
//                        Methods.showCommentCountsDialog(
//                            trendingList!![position].post_comment_count.toString(),
//                            comments!!,
//                            this@VideoPlayActivity
//                        )
//                    }
//                    forward_layout.setOnClickListener {
//                        createLinks(position)
//                    }
//
//                    imgDownload.setOnClickListener {
//                        val videoWidth = trendingList!![position].post_width
//                        val videoHeight = trendingList!![position].post_height
//                        if (videoHeight != null && videoWidth != null) {
//                            requestPermission(
//                                trendingList!![position].post_video!!,
//                                videoWidth,
//                                videoHeight,
//                                true
//                            )
//                        } else {
//                            requestPermission(trendingList!![position].post_video!!, 0, 0, true)
//                        }
//                    }
//
//                    imgExtractAudio.setOnClickListener {
//                        isLogin = SharedPreferenceUtils.getInstance(this@VideoPlayActivity)
//                            .getBoolanValue(Constants.IS_LOGIN, false)
//                        if (isLogin) {
//                            if (trendingList!![position].post_video != null && trendingList!![position].post_video != "") {
//                                //  askPermission(trendingList!![position].post_video.toString())
//                                requestPermission(
//                                    trendingList!![position].post_video!!,
//                                    0,
//                                    0,
//                                    false
//                                )
//                            }
//                        } else {
//                            val intent = Intent(this@VideoPlayActivity, LoginActivity::class.java)
//                            intent.putExtra("screen_name", "startPost")
//                            startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
//                        }
//                    }
//                    if (trendingList!![position].profile_name != null && !trendingList!![position].profile_name!!.contentEquals(
//                            ""
//                        )
//                    ) {
//                        tv_userName.text = trendingList!![position].profile_name.toString().trim()
//                    }
//                    if (trendingList!![position].category_name != null && !trendingList!![position].category_name!!.contentEquals(
//                            ""
//                        )
//                    ) {
//                        tv_cat_name.text = trendingList!![position].category_name
//                    }
//
//                    if (trendingList!![position].post_title != null && trendingList!![position].post_title != "") {
//                        if (trendingList!![position].post_title!!.toString().contains("#")) {
//                            if (trendingList!![position].post_title!!.toString().trim()
//                                    .contains(" ")
//                            ) {
//                                Logger.print(" contain space only multiple hash tag hash tag @@@@@@@@@@@@@" + trendingList!![position].post_title!!.toString())
//                                val str = trendingList!![position].post_title!!.toString()
//                                val splited = str.split(" ").toTypedArray()
//
//                                val builder = SpannableStringBuilder()
//                                for (word in splited) {
//                                    builder.append(word).append(" ")
//                                        .setSpan(object : ClickableSpan() {
//                                            override fun onClick(@NonNull view: View) {
//                                                if (word.startsWith("#")) {
//                                                    val intent = Intent(
//                                                        this@VideoPlayActivity,
//                                                        HashTagDetailOrTrendingDetail::class.java
//                                                    )
//                                                    intent.putExtra("hashTag", word)
//                                                    startActivity(intent)
//                                                }
//                                            } // optional - for styling the specific text
//
//                                            override fun updateDrawState(ds: TextPaint) {
//                                                super.updateDrawState(ds)
//                                                ds.color = ContextCompat.getColor(
//                                                    this@VideoPlayActivity,
//                                                    R.color.hash_tag
//                                                )
//                                                ds.isUnderlineText = false
//                                            }
//                                        }, builder.length - word.length, builder.length, 0)
//                                }
//
//                                tvPostTitle.setText(builder, TextView.BufferType.SPANNABLE)
//                                tvPostTitle.movementMethod = LinkMovementMethod.getInstance()
//                                tvPostTitle.setTextColor(
//                                    ContextCompat.getColor(
//                                        this@VideoPlayActivity,
//                                        R.color.hash_tag
//                                    )
//                                )
//                                tvPostTitle.setOnClickListener {}
//                            } else {
//                                Logger.print("not contain space only one hash tag @@@@@@@@@@@@@" + trendingList!![position].post_title!!.toString())
//                                tvPostTitle.text = trendingList!![position].post_title
//                                tvPostTitle.setTextColor(
//                                    ContextCompat.getColor(
//                                        this@VideoPlayActivity,
//                                        R.color.hash_tag
//                                    )
//                                )
//
//                                tvPostTitle.setOnClickListener {
//                                    val intent = Intent(
//                                        this@VideoPlayActivity,
//                                        HashTagDetailOrTrendingDetail::class.java
//                                    )
//                                    intent.putExtra(
//                                        "hashTag",
//                                        trendingList!![position].post_title.toString()
//                                    )
//                                    startActivity(intent)
//                                }
//                            }
//                        } else {
//                            tvPostTitle.text = trendingList!![position].post_title
//                            tvPostTitle.setTextColor(
//                                ContextCompat.getColor(
//                                    this@VideoPlayActivity,
//                                    R.color.white
//                                )
//                            )
//                            tvPostTitle.setOnClickListener {}
//                        }
//                    }
//
//                    var likeCountInitial = ""
//                    for (key in likeVideoHash.keys) {
//                        likeCountInitial =
//                            likeVideoHash[trendingList!![position].post_id.toString()]!!
//                    }
//
//                    if (!likeCountInitial.contentEquals("")) {
//                        Methods.showLikeCounts(likeCountInitial, like_txt)
//                    }
//                    var isFollowingUser = ""
//                    for (key in followerMap.keys) {
//                        isFollowingUser = followerMap[trendingList!![position].user_id.toString()]!!
//                    }
//
//                    if (isFollowingUser.contentEquals("1")) {
//                        tv_follow_.visibility = View.GONE
//                        tv_following_user.visibility = View.VISIBLE
//                    }
//                    if (isFollowingUser.contentEquals("0")) {
//                        tv_follow_.visibility = View.VISIBLE
//                        tv_following_user.visibility = View.INVISIBLE
//                    }
//
//                    val currentUserId = SharedPreferenceUtils.getInstance(this@VideoPlayActivity)
//                        .getStringValue(Constants.USER_ID_PREF, "")!!
//                    if (trendingList!![position].user_id.toString().contentEquals(currentUserId)) {
//                        tv_follow_.visibility = View.GONE
//                        tv_following_user.visibility = View.INVISIBLE
//                    }
//
//                    tv_follow_.setOnClickListener {
//                        isLogin =
//                            SharedPreferenceUtils.getInstance(context = this@VideoPlayActivity)
//                                .getBoolanValue(Constants.IS_LOGIN, false)
//                        if (isLogin) {
//                            tv_follow_.visibility = View.GONE
//                            tv_following_user.visibility = View.VISIBLE
//                            followerMap[trendingList!![position].user_id.toString()] = "1"
//                            followUser(trendingList!![position].user_id.toString())
//                        } else {
//                            val intent = Intent(this@VideoPlayActivity, LoginActivity::class.java)
//                            intent.putExtra("screen_name", "player")
//                            startActivity(intent)
//                            finish()
//                        }
//                    }
//
//                    tv_following_user.setOnClickListener {
//                        isLogin =
//                            SharedPreferenceUtils.getInstance(context = this@VideoPlayActivity)
//                                .getBoolanValue(Constants.IS_LOGIN, false)
//                        if (isLogin) {
//                            tv_following_user.visibility = View.INVISIBLE
//                            tv_follow_.visibility = View.VISIBLE
//                            followerMap[trendingList!![position].user_id.toString()] = "0"
//                            unFollowUser(trendingList!![position].user_id.toString())
//                        } else {
//                            val intent = Intent(this@VideoPlayActivity, LoginActivity::class.java)
//                            intent.putExtra("screen_name", "player")
//                            startActivity(intent)
//                            finish()
//                        }
//                    }
//
//                    if (trendingList!![position].post_view_count != null && trendingList!![position].post_view_count != "") {
//                        Methods.showViewCounts(
//                            trendingList!![position].post_view_count.toString(),
//                            tvpostCount
//                        )
//                    }
//                    if (!likeCountInitial.contentEquals("")) {
//                        Methods.showLikeCounts(likeCountInitial, like_txt)
//                    }
//                    if (trendingList!![position].post_comment_count != null && trendingList!![position].post_comment_count.toString() != "") {
//                        Methods.showCommentCounts(
//                            trendingList!![position].post_comment_count.toString(),
//                            comment_txt
//                        )
//                    }
//                    if (trendingList!![position].post_comment_count != null && !trendingList!![position].post_comment_count!!.contentEquals(
//                            ""
//                        )
//                    ) {
//                        Methods.showCommentCountsDialog(
//                            trendingList!![position].post_comment_count.toString(),
//                            comments!!,
//                            this@VideoPlayActivity
//                        )
//                    }
//
//                    var hasLikeOrNotInitial = ""
//                    for (key in hasLikedMap.keys) {
//                        hasLikeOrNotInitial =
//                            hasLikedMap[trendingList!![position].post_id.toString()]!!
//                    }
//
//                    if (!hasLikeOrNotInitial.contentEquals("")) {
//                        if (hasLikeOrNotInitial.contentEquals("1")) {
//                            like_image.setImageDrawable(
//                                resources.getDrawable(
//                                    R.drawable.like_active,
//                                    this@VideoPlayActivity.theme
//                                )
//                            )
//                        }
//
//                        if (hasLikeOrNotInitial.contentEquals("0")) {
//                            like_image.setImageDrawable(
//                                resources.getDrawable(
//                                    R.drawable.like_new,
//                                    this@VideoPlayActivity.theme
//                                )
//                            )
//                        }
//                    }
//
//                    if (!likeCountInitial.contentEquals("")) {
//                        Methods.showLikeCounts(likeCountInitial, like_txt)
//                    }
//
//                    heart_layout.setOnClickListener {}
//
//                    comment.setOnClickListener {
//                        if (adapter1?.fragments != null) {
//                            val lastFragment = adapter1?.fragments?.get(lastPosition)
//                            lastFragment?.stopPlayer()
//                        }
//                        commentBottomSheet = CommentBottomSheet.newInstance(
//                            userId,
//                            postId!!, postCommentCount, this@VideoPlayActivity
//                        )
//                        commentBottomSheet?.show(supportFragmentManager, "Comment_Bottom")
//                    }
//
//                    like_image.setOnClickListener {
//                        var hasLikeOrNot = ""
//                        for (key in hasLikedMap.keys) {
//                            hasLikeOrNot =
//                                hasLikedMap[trendingList!![position].post_id.toString()]!!
//                        }
//
//                        if (hasLikeOrNot.contentEquals("0")) {
//                            likeCount = 1
//                            heart_layout.addHeart(randomColor())
//                            heart_layout.addHeart(
//                                randomColor(),
//                                R.drawable.like_active,
//                                R.drawable.like_new
//                            )
//                            heart_layout.addHeart(randomColor())
//                            heart_layout.addHeart(randomColor())
//                            heart_layout.addHeart(randomColor())
//                            heart_layout.addHeart(randomColor())
//                            like_image.setImageResource(R.drawable.like_active)
//
//                            var likeCounts = ""
//                            for (key in likeVideoHash.keys) {
//                                likeCounts =
//                                    likeVideoHash[trendingList!![position].post_id.toString()]!!
//                            }
//
//                            var totalLikeCounts: Int = likeCounts.toInt()
//                            totalLikeCounts += 1
//                            if (totalLikeCounts.toString() != "") {
//                                Methods.showLikeCounts(totalLikeCounts.toString(), like_txt)
//                            }
//
//                            likeVideoHash[trendingList!![position].post_id.toString()] =
//                                totalLikeCounts.toString()
//                            hasLikedMap[trendingList!![position].post_id.toString()] = "1"
//                        }
//
//                        if (hasLikeOrNot.contentEquals("1")) {
//                            var unLikeCounts = ""
//                            likeCount = 0
//                            like_image.setImageResource(R.drawable.like_new)
//
//                            for (key in likeVideoHash.keys) {
//                                /*println("Element at key $key : ${likeVideoHash[key]}")
//                                Log.d("likeunlike key===", likeVideoHash[trendingList!![position].post_id.toString()]!!)*/
//                                unLikeCounts =
//                                    likeVideoHash[trendingList!![position].post_id.toString()]!!
//                            }
//                            if (!unLikeCounts.contentEquals("0")) {
//                                var totalUnlikeCounts: Int = unLikeCounts.toInt()
//                                totalUnlikeCounts -= 1
//
//                                if (totalUnlikeCounts.toString() != "") {
//                                    Methods.showLikeCounts(totalUnlikeCounts.toString(), like_txt)
//                                }
//                                //  like_txt.text = totalUnlikeCounts.toString()
//                                likeVideoHash[trendingList!![position].post_id.toString()] =
//                                    totalUnlikeCounts.toString()
//                            }
//                            hasLikedMap[trendingList!![position].post_id.toString()] = "0"
//                        }
//                        likeUnlikeVideo(trendingList!![position].post_id, likeCount)
//                    }
//                }
//            }
//
//            override fun onPageScrollStateChanged(state: Int) {
//                super.onPageScrollStateChanged(state)
//                Logger.print("onPageScrollStateChanged $state")
//                if (state == 1) {
//                    isOnScrollCall = true
//                }
//            }
//
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {
//                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
//                Logger.print("onPageScrolled Position $position PositionOffset $positionOffset PositionOffsetPixels $positionOffsetPixels")
//                if (isOnScrollCall) {
//                    isOnScrollCall = false
//                    if (positionOffset < 0.2f) {
//                        if (position < trendingList?.size?.minus(1) ?: 0) {
//                            Logger.print("Going to play video at ${position + 1}")
//                            val lastFragment = adapter1?.fragments?.get(position + 1)
//                            lastFragment?.playVideo(false)
//                        }
//                    } else {
//                        if (position > 0) {
//                            Logger.print("Going to play video at ${position - 1}")
//                            val lastFragment = adapter1?.fragments?.get(position - 1)
//                            lastFragment?.playVideo(false)
//                        }
//                    }
//                }
//            }
//        })
//        viewPagerPlayer.setCurrentItem(pos, false)
//        Handler().postDelayed({
//            val lastFragment = adapter1?.fragments?.get(pos)
//            lastFragment?.playVideo(true)
//        }, 250)
//    }
//
//    override fun onPause() {
//        if (adapter1?.fragments != null) {
//            adapter1?.fragments!![currentPosition].stopPlayer()
//        }
//
//        if (spamBottomSheet != null && spamBottomSheet?.isVisible != null && spamBottomSheet?.isVisible!! && commentBottomSheet != null && commentBottomSheet?.isVisible != null && commentBottomSheet?.isVisible!!) {
//            spamBottomSheet?.dismiss()
//            commentBottomSheet?.dismiss()
//            Utils.hideKeyboard(this)
//        } else if (spamBottomSheet != null && spamBottomSheet?.isVisible != null && spamBottomSheet?.isVisible!!) {
//            spamBottomSheet?.dismiss()
//        } else if (commentBottomSheet != null && commentBottomSheet?.isVisible != null && commentBottomSheet?.isVisible!!) {
//            commentBottomSheet?.dismiss()
//        }
//
//
//        super.onPause()
//    }
//
//    override fun onResume() {
//        if (adapter1?.fragments != null) {
//            adapter1?.fragments!![currentPosition].playVideo(true)
//        }
//        super.onResume()
//    }
//
//    private fun createLinks(position: Int) {
//        FirebaseDynamicLinks.getInstance().createDynamicLink()
//            .setLink(createShareUri(trendingList!![position].post_id.toString()))
//            .setDomainUriPrefix("https://link.smylee.app")
//            .setAndroidParameters(
//                DynamicLink.AndroidParameters.Builder("smylee.app").build()
//            )
//
//            .setSocialMetaTagParameters(
//                DynamicLink.SocialMetaTagParameters
//                    .Builder()
//                    .setTitle(trendingList!![position].post_title!!)
//                    .setImageUrl(Uri.parse(trendingList!![position].post_video_thumbnail))
//                    .build()
//
//            )
//            .buildShortDynamicLink()
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    sharingLink = it.result!!.shortLink.toString()
//                    Logger.print("Sharing Link $sharingLink")
//                    val sendIntent = Intent()
//                    sendIntent.action = Intent.ACTION_SEND
//                    // sendIntent.putExtra(Intent.EXTRA_TEXT, "Hii Checkout $sharingLink")
//                    sendIntent.putExtra(
//                        Intent.EXTRA_TEXT,
//                        "Checkout this interesting video from Smylee App : $sharingLink"
//                    )
//                    sendIntent.type = "text/plain"
//                    startActivity(sendIntent)
//                } else {
//                    it.exception?.printStackTrace()
//                    Logger.print("Exception : " + it.exception)
//                }
//            }
//    }
//
//    private fun createShareUri(postId: String): Uri {
//        val builder = Uri.Builder()
//        builder.scheme("https")
//            .authority("link.smylee.app")
//            .appendPath("video")
//            .appendQueryParameter("postId", postId)
//
//        return builder.build()
//    }
//
//    private fun requestPermission(
//        videoLink: String,
//        videoWidth: Int,
//        videoHeight: Int,
//        isDownload: Boolean
//    ) {
//        Dexter.withActivity(this).withPermissions(
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        ).withListener(object : MultiplePermissionsListener {
//            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
//                if (isDownload) {
//                    if (report.areAllPermissionsGranted()) {
//                        showProgressDialog()
//                        ShareVideoUtils.shareVideoFile(
//                            this@VideoPlayActivity,
//                            videoLink,
//                            videoWidth,
//                            videoHeight,
//                            this@VideoPlayActivity
//                        )
//                    }
//                    // check for permanent denial of any permission show alert dialog
//                    if (report.isAnyPermissionPermanentlyDenied) {
//                        showSettingsDialog()
//                    }
//                } else {
//                    if (report.areAllPermissionsGranted()) {
//                        showProgressDialog()
//                        val rootDirectory = File(Constants.EXTRACT_PATH)
//                        if (rootDirectory.exists()) {
//                            Methods.deleteFilesFromDirectory(Constants.EXTRACT_PATH)
//                        }
//                        ShareVideoUtils.extractVideoFile(
//                            this@VideoPlayActivity,
//                            videoLink,
//                            this@VideoPlayActivity
//                        )
//                    }
//                    if (report.isAnyPermissionPermanentlyDenied) {
//                        showSettingsDialog()
//                    }
//                }
//            }
//
//            override fun onPermissionRationaleShouldBeShown(
//                permissions: List<PermissionRequest>,
//                token: PermissionToken
//            ) {
//                token.continuePermissionRequest()
//            }
//        }).withErrorListener {
//            Toast.makeText(this, "Error occurred! ", Toast.LENGTH_SHORT).show()
//        }.onSameThread().check()
//    }
//
//    private fun showSettingsDialog() {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle(getString(R.string.message_need_permission))
//        builder.setMessage(getString(R.string.message_permission))
//        builder.setPositiveButton(getString(R.string.title_go_to_setting)) { dialog, _ ->
//            dialog.cancel()
//            openSettings()
//        }
//        builder.show()
//    }
//
//    private fun openSettings() {
//        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//        val uri = Uri.fromParts("package", packageName, null)
//        intent.data = uri
//        startActivityForResult(intent, 101)
//    }
//
//    private fun deletePost(POST_ID: String, position: Int) {
//        val hashMap = HashMap<String, String>()
//        hashMap["post_id"] = POST_ID
//        showProgressDialog()
//        viewModel.DeletePost(this, hashMap).observe(this, Observer {
//            hideProgressDialog()
//            if (it != null) {
//                Logger.print("DELETE POST FROM PLAYER Response :::::::::::::::::::::::::: $it")
//                val jsonObject = JSONObject(it.toString())
//                if (jsonObject["code"] == 1) {
//                    SharedPreferenceUtils.getInstance(this)
//                        .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
//                    trendingList!!.removeAt(position)
//                    this@VideoPlayActivity.finish()
//                }
//            }
//        })
//    }
//
//    private fun unFollowUser(userID: String) {
//        val isFollow = "0"
//        val hashMap = HashMap<String, String>()
//        hashMap["is_follow"] = isFollow
//        hashMap["other_user_id"] = userID
//        viewModel.FollowUnfollow(this, hashMap).observe(
//            this, Observer {
//                if (it != null) {
//                    SharedPreferenceUtils.getInstance(this@VideoPlayActivity)
//                        .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
//                    SharedPreferenceUtils.getInstance(this)
//                        .setValue(Constants.IS_CHANGE_LIKE_OR_COMMENT_OR_FOLLOW, true)
//                    Logger.print("Unfollowuser Whom im following Response : $it")
//                }
//            }
//        )
//    }
//
//    private fun followUser(user_ID: String) {
//        val isFollow = "1"
//        val hashMap = HashMap<String, String>()
//        hashMap["is_follow"] = isFollow
//        hashMap["other_user_id"] = user_ID
//
//        viewModel.FollowUnfollow(this, hashMap).observe(
//            this, Observer {
//                if (it != null) {
//                    SharedPreferenceUtils.getInstance(this)
//                        .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
//                    SharedPreferenceUtils.getInstance(this)
//                        .setValue(Constants.IS_CHANGE_LIKE_OR_COMMENT_OR_FOLLOW, true)
//                    Logger.print("FOLLOWUNFOLLOWUSER Response : $it")
//                }
//            }
//        )
//    }
//
//    private fun likeUnlikeVideo(post_id: Int?, like_unlike: Int) {
//        val hashMap = HashMap<String, String>()
//        hashMap["is_like"] = like_unlike.toString()
//        hashMap["post_id"] = post_id.toString()
//        viewModel.likeUnlikeVideo(this, hashMap, false).observe(this, Observer {
//            if (it != null) {
//                SharedPreferenceUtils.getInstance(this)
//                    .setValue(Constants.IS_CHANGE_LIKE_OR_COMMENT_OR_FOLLOW, true)
//                if (isShowDelete) {
//                    SharedPreferenceUtils.getInstance(this)
//                        .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
//                }
//
//                Logger.print("LIKE VIDEO OR UNLIKE VIDEO!!!!!!!!!!!!!!!!!!!$it")
//                val jsonObject = JSONObject(it.toString())
//                val code = jsonObject.getInt("code")
//                if (code == 0) {
//                    Utils.showAlert(this, "", jsonObject["message"].toString())
//                }
//            }
//        }
//        )
//    }
//
//    private fun randomColor(): Int {
//        return Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255))
//    }
//
//    override fun onVideoStartPlaying(position: Int) {
//        /*if(previousPosition > -1) {
//            val previousFragment = adapter1!!.fragments[previousPosition]
//            previousFragment.stopPlayer()
//        }*/
//    }
//
//    override fun onVideoViewCountUpdate(position: Int, videoViewCount: String) {
//        trendingList!![position].isViewed = true
//    }
//
//    override fun onInitNextCall(currentPos: Int) {
//        /*if ((currentPos + 1) < trendingList!!.size && (currentPos + 1) < (lastPosition + 6)) {
//            preDownload(trendingList!![currentPos + 1].post_video,null,
//                currentPos + 1,this@VideoPlayActivity)
//        }*/
//    }
//
//    override fun onVideoReadyToShare(videoPath: String) {
//        hideProgressDialog()
//        Methods.deleteFileWithZeroKB(Constants.DOWNLOADED_PATH)
//        Utils.showToastMessage(this, getString(R.string.video_downloaded))
//        val gifFile = File(Constants.ROOT_PATH, "smylee_gif_new.gif")
//        if (gifFile.exists()) {
//            gifFile.delete()
//        }
//    }
//
//    override fun onDoubleTAP(clickState: Int) {
//
//        when (clickState) {
//            1 -> {
//                if (commentBottomSheet != null && commentBottomSheet?.isVisible!!) {
//                    commentBottomSheet?.dismiss()
//                } else if (spamBottomSheet != null && spamBottomSheet?.isVisible!!) {
//                    spamBottomSheet?.dismiss()
//                }
//            }
//            2 -> {
//                if ((commentBottomSheet == null || !commentBottomSheet?.isVisible!!) && (spamBottomSheet == null || !spamBottomSheet?.isVisible!!)) {
//                    imgLikeDoubleTap.visibility = View.VISIBLE
//                    Methods.animateHeart(imgLikeDoubleTap)
//                    var hasLikeOrNot = ""
//                    for (key in hasLikedMap.keys) {
//                        hasLikeOrNot =
//                            hasLikedMap[trendingList!![viewpagerposition].post_id.toString()]!!
//                    }
//
//                    if (hasLikeOrNot.contentEquals("0")) {
//                        likeCount = 1
//                        like_image.setImageResource(R.drawable.like_active)
//                        var likeCounts = ""
//                        for (key in likeVideoHash.keys) {
//                            likeCounts =
//                                likeVideoHash[trendingList!![viewpagerposition].post_id.toString()]!!
//                        }
//
//                        var totalLikeCounts: Int = likeCounts.toInt()
//                        totalLikeCounts += 1
//                        if (totalLikeCounts.toString() != "") {
//                            Methods.showLikeCounts(totalLikeCounts.toString(), like_txt)
//                        }
//
//                        likeVideoHash[trendingList!![viewpagerposition].post_id.toString()] =
//                            totalLikeCounts.toString()
//                        hasLikedMap[trendingList!![viewpagerposition].post_id.toString()] = "1"
//                    }
//                    likeUnlikeVideo(trendingList!![viewpagerposition].post_id, 1)
//                }
//            }
//        }
//
//
//    }
//
//    override fun onBackPressed() {
//        when {
//            (spamBottomSheet != null && spamBottomSheet?.isVisible!!) -> {
//                spamBottomSheet?.dismiss()
//            }
//            (commentBottomSheet != null && commentBottomSheet?.isVisible!!) -> {
//                commentBottomSheet?.dismiss()
//            }
//            else -> {
//                super.onBackPressed()
//            }
//        }
//    }
//
//    override fun onExtractFinished() {
//        hideProgressDialog()
//        Logger.print("call video player onExtractFinished")
//
//        Methods.deleteFilesFromDirectory(Constants.EXTRACT_PATH)
//        var nameFile = Methods.fileFromDir(Constants.Audio_PATH)
//        if (nameFile!!.contains(".mp4")) {
//            nameFile = nameFile.replace(".mp4", "")
//        }
//
//        val isLogin = SharedPreferenceUtils.getInstance(this)
//            .getBoolanValue(Constants.IS_LOGIN, false)
//        if (isLogin) {
//            val intent = Intent(this, CameraVideoRecording::class.java)
//            intent.putExtra("ISAUDIOMIX", true)
//            intent.putExtra("extractAudio", true)
//            intent.putExtra("audioFileNameFinal", nameFile)
//            startActivity(intent)
//        } else {
//            val intent = Intent(this, LoginActivity::class.java)
//            intent.putExtra("screen_name", "startPost")
//            startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
//        }
//    }
//
//    override fun onCommentAdded(postId: Int, tvCommnetCountBottomSheet: AppCompatTextView) {
//        val postIdCurrent = postId
//        if (lastPosition != -1 && postId == postIdCurrent) {
//            val bundle = Bundle()
//            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "AddCommentForYou")
//            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, postId.toString())
//            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
//
//            val commentsCount =
//                Integer.parseInt(trendingList!![lastPosition].post_comment_count!!) + 1
//            postCommentCount = "$commentsCount"
//            trendingList!![lastPosition].post_comment_count = "$commentsCount"
//            adapter1?.notifyItemChanged(lastPosition)
//            if (commentsCount.toString() != "") {
//                Methods.showCommentCounts(commentsCount.toString(), comment_txt)
//                Methods.showCommentCountsDialog(
//                    commentsCount.toString(),
//                    tvCommnetCountBottomSheet,
//                    this
//                )
//            }
//        }
//    }
//
//    override fun onDialogDismiss() {
//        Utils.hideKeyboard(this)
//        if (adapter1?.fragments != null) {
//            adapter1?.fragments!![currentPosition].playVideo(true)
//        }
//    }
//
//    override fun onSpamComment(commentId: String) {
//        if (spamBottomSheet == null || !spamBottomSheet?.isVisible!!) {
//            spamBottomSheet =
//                SpamBottomSheet.newInstance(userId, commentId, postId!!, this@VideoPlayActivity)
//            spamBottomSheet?.show(supportFragmentManager, "SpamBottomSheet")
//        }
//    }
//
//    override fun onSpamDialogDismiss() {
//        Utils.hideKeyboard(this@VideoPlayActivity)
//
//        if (commentBottomSheet != null && commentBottomSheet?.isVisible!!) {
//            if (adapter1?.fragments != null) {
//                adapter1?.fragments!![currentPosition].stopPlayer()
//            }
//        } else {
//            if (adapter1?.fragments != null) {
//                adapter1?.fragments!![currentPosition].playVideo(true)
//            }
//        }
//    }
//}