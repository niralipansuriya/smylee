package smylee.app.playvideo

import android.Manifest
import android.app.Activity
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
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import kotlinx.android.synthetic.main.activity_video_play.*
import org.json.JSONObject
import smylee.app.CallBacks.DoubleTapCallBacks
import smylee.app.CallBacks.ExtractListner
import smylee.app.Profile.MyProfileFragment
import smylee.app.Profile.OtherUserProfileActivity
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.adapter.PlayerViewPagerAdapter
import smylee.app.dialog.CommonAlertDialog
import smylee.app.home.CommentBottomSheet
import smylee.app.home.HomeActivity
import smylee.app.home.HomeViewModel
import smylee.app.home.SpamBottomSheet
import smylee.app.listener.*
import smylee.app.login.LoginActivity
import smylee.app.model.ForYouResponse
import smylee.app.setting.SettingActivity
import smylee.app.ui.Activity.CameraVideoRecording
import smylee.app.ui.Activity.HashTagDetailOrTrendingDetail
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class VideoDetailActivity : BaseActivity(), OnVideoPlayingListener, InitNextVideoListener,
    OnShareVideoPrepareListener, DoubleTapCallBacks, ExtractListner, OnCommentAdded,
    OnSpamDialogListener {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var pageCount = 1
    lateinit var viewModel: HomeViewModel
    var videoList: ArrayList<ForYouResponse>? = null
    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var paginationCount: Int = 0
    private var adapter1: PlayerViewPagerAdapter? = null
    private var viewpagerposition: Int = 0
    private var currentPosition: Int = 0
    var otherUserId: String = ""
    private var isOnScrollCall = false
    var userId: String = ""
    private var likeCount: Int = 0
    var lastPosition = -1
    private var sharingLink: String = ""
    var previousPosition = -1
    var commentBottomSheet: CommentBottomSheet? = null
    private var spamBottomSheet: SpamBottomSheet? = null
    private var postId: Int? = null
    private var postCommentCount = "0"
    var isLogin: Boolean = false
    private var isShowDelete: Boolean = false
    private val mRandom = Random()
    var pos: Int = 0
    var screen: String = ""
    var hashtag: String = ""
    var searchTxt: String = ""
    var catId: String = ""
    var userIDApi: String = ""
    var followerMap: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        videoDetailActivity = this
        avatar.setOnClickListener {
            onBackPressed()
        }
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()

        if (intent != null) {
            pos = intent.getIntExtra("position", 0)
            catId = intent.getStringExtra("cat_ID")!!
            screen = intent.getStringExtra("screen")!!
            hashtag = intent.getStringExtra("hashtag")!!
            videoList = intent.getParcelableArrayListExtra("responseData")
            pageCount = intent.getIntExtra("pageNo", 0)
            searchTxt = intent.getStringExtra("searchTxt")!!
            userIDApi = intent.getStringExtra("userIDApi")!!
        }

        Methods.setImageRotation(imgExtractAudio)
        ivFlag.setOnClickListener {
            if (postId != null) {
                if (adapter1?.fragments != null) {
                    val lastFragment = adapter1?.fragments?.get(lastPosition)
                    lastFragment?.stopPlayer()
                }
                if (spamBottomSheet == null || !spamBottomSheet?.isVisible!!) {
                    spamBottomSheet = SpamBottomSheet.newInstance(userId, "", postId!!, this)
                    spamBottomSheet?.show(supportFragmentManager, "SpamBottomSheet")
                }
            }
        }
        if (screen.contentEquals("otherProfile") || screen.contentEquals("myProfile") || screen.contentEquals(
                "searchDetail"
            ) || screen.contentEquals("discoverDetail") || screen.contentEquals("hashtagDetail")
        ) {
            if (videoList!!.size > 0) {
                followerMap.clear()
                for (i in videoList!!.indices) {
                    followerMap[videoList!![i].user_id.toString()] = videoList!![i].is_following.toString()
                }
            }
            if (pos == 0) {
                if (videoList!![pos].post_comment_count != null && videoList!![pos].post_comment_count != "") {
                    Methods.showCommentCounts(videoList!![pos].post_comment_count.toString(), comment_txt)
                }
                if (videoList!![pos].post_view_count != null && videoList!![pos].post_view_count != "") {
                    Methods.showViewCounts(videoList!![pos].post_view_count.toString(), tvpostCount)
                }
                if (videoList!![pos].profile_name != null) {
                    tv_userName.text = videoList!![pos].profile_name.toString()
                }
                if (videoList!![pos].category_name != null) {
                    tv_cat_name.text = videoList!![pos].category_name
                }
                if (videoList!![pos].mark_as_verified_badge != null && videoList!![pos].mark_as_verified_badge!! == 1) {
                    ivVerified?.visibility = View.VISIBLE
                } else {
                    ivVerified?.visibility = View.GONE

                }
                if (videoList!![pos].post_title != null && videoList!![pos].post_title != "") {
                    if (videoList!![pos].post_title!!.toString().contains("#")) {
                        if (videoList!![pos].post_title!!.toString().trim().contains(" ")) {
                            val str = videoList!![pos].post_title!!.toString()
                            val splited = str.split(" ").toTypedArray()
                            val builder = SpannableStringBuilder()
                            for (word in splited) {
                                builder.append(word).append(" ").setSpan(object : ClickableSpan() {
                                    override fun onClick(@NonNull view: View) {
                                        if (word.startsWith("#")) {
                                            val intent = Intent(this@VideoDetailActivity,HashTagDetailOrTrendingDetail::class.java)
                                            intent.putExtra("hashTag", word)
                                            startActivity(intent)
                                        }
                                    }

                                    override fun updateDrawState(ds: TextPaint) {
                                        super.updateDrawState(ds)
                                        ds.color = ContextCompat.getColor(this@VideoDetailActivity, R.color.hash_tag)
                                        ds.isUnderlineText = false
                                    }
                                }, builder.length - word.length, builder.length, 0)
                            }
                            //  ss.setSpan(clickableSpan1,startindex,endIndex,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            tvPostTitle.setText(builder, TextView.BufferType.SPANNABLE)
                            tvPostTitle.movementMethod = LinkMovementMethod.getInstance()
                            tvPostTitle.setTextColor(ContextCompat.getColor(this, R.color.hash_tag))
                            tvPostTitle.setOnClickListener {}
                        } else {
                            Logger.print("not contain space only one hash tag @@@@@@@@@@@@@" + videoList!![pos].post_title!!.toString())
                            tvPostTitle.text = videoList!![pos].post_title
                            tvPostTitle.setTextColor(ContextCompat.getColor(this, R.color.hash_tag))
                            tvPostTitle.setOnClickListener {
                                val intent = Intent(this, HashTagDetailOrTrendingDetail::class.java)
                                intent.putExtra("hashTag", videoList!![pos].post_title.toString())
                                startActivity(intent)
                            }
                        }
                    } else {
                        tvPostTitle.text = videoList!![pos].post_title
                        tvPostTitle.setTextColor(ContextCompat.getColor(this, R.color.white))
                        tvPostTitle.setOnClickListener {}
                    }
                }

                if (videoList!![pos].no_of_followers != null && videoList!![pos].no_of_followers != "") {
                    llView.visibility = View.VISIBLE
                    tv_follower_counts.visibility = View.VISIBLE
                    Methods.showFollowerCounts(videoList!![pos].no_of_followers.toString(), tv_follower_counts,this)
                } else {
                    llView.visibility = View.GONE
                    tv_follower_counts.visibility = View.GONE
                }
                val currentUserId = SharedPreferenceUtils.getInstance(this)
                    .getStringValue(Constants.USER_ID_PREF, "")!!
                if (!screen.contentEquals("myProfile")) {
                    var isFollowingUser = ""
                    for (key in followerMap.keys) {
                        isFollowingUser = followerMap[videoList!![pos].user_id.toString()]!!
                    }
                    if (isFollowingUser.contentEquals("1")) {
                        tv_follow_.visibility = View.GONE
                        tv_following_user.visibility = View.VISIBLE
                    }
                    if (isFollowingUser.contentEquals("0")) {
                        tv_follow_.visibility = View.VISIBLE
                        tv_following_user.visibility = View.INVISIBLE
                    }

                    if (videoList!![pos].user_id.toString().contentEquals(currentUserId)) {
                        tv_follow_.visibility = View.GONE
                        tv_following_user.visibility = View.INVISIBLE
                    }
                }

                if (videoList!![pos].post_like_count != null && videoList!![pos].post_like_count != "") {
                    Methods.showLikeCounts(videoList!![pos].post_like_count.toString(), like_txt)
                }

                if (videoList!![pos].user_id.toString().contentEquals(currentUserId)) {
                    tv_follow_.visibility = View.GONE
                    tv_following_user.visibility = View.INVISIBLE
                }
            }
        }
        if (screen != "") {
            when (screen) {
                "discover" -> {
                    videoList!!.clear()
                    getDiscoverDetails(catId)
                }
                "trending" -> {
                    videoList!!.clear()
                    hashTagDetail(hashtag)
                }
                "otherProfile" -> {
                    if (videoList!!.size > 0) {
                        setViewPagerAdapter1()
                    }
                }
                "myProfile" -> {
                    if (videoList!!.size > 0) {
                        delele_post_ll?.visibility = View.VISIBLE
                        setViewPagerAdapter1()
                    }
                }
                "searchDetail" -> {
                    if (videoList!!.size > 0) {
                        setViewPagerAdapter1()
                    }
                }
                "discoverDetail" -> {
                    if (videoList!!.size > 0) {
                        setViewPagerAdapter1()
                    }
                }
                "hashtagDetail" -> {
                    if (videoList!!.size > 0) {
                        setViewPagerAdapter1()
                    }
                }
                "search" -> {
                    getViewMoreVideoListing(searchTxt)
                }
            }
        }
    }

    override fun onVideoStartPlaying(position: Int) {
    }

    override fun onVideoViewCountUpdate(position: Int, videoViewCount: String) {
        videoList!![position].isViewed = true
    }

    override fun onInitNextCall(currentPos: Int) {
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
                    if (videoList!![currentPosition].has_liked == 0) {
                        likeCount = 1
                        like_image.setImageResource(R.drawable.like_active)
                        val likeCounts = Integer.parseInt(videoList!![currentPosition].post_like_count!!) + 1
                        Methods.showLikeCounts(likeCounts.toString(), like_txt)
                        likeUnlikeVideo(videoList!![currentPosition].post_id, likeCount)
                        videoList!![currentPosition].post_like_count = "$likeCounts"
                        videoList!![currentPosition].has_liked = 1
                    }
                }
        }
    }

    override fun onExtractFinished() {
        hideProgressDialog()
        Logger.print("call video player onExtractFinished")

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
        val postIdCurrent = postId
        if (lastPosition != -1 && postId == postIdCurrent) {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "AddCommentForYou")
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, postId.toString())
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

            val commentsCount = Integer.parseInt(videoList!![lastPosition].post_comment_count!!) + 1
            postCommentCount = "$commentsCount"
            videoList!![lastPosition].post_comment_count = "$commentsCount"
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

    override fun onResume() {
        if (adapter1?.fragments != null) {
            adapter1?.fragments!![currentPosition].playVideo(true)
        }
        super.onResume()
    }

    override fun onDialogDismiss() {
        Utils.hideKeyboard(this)
        if (adapter1?.fragments != null) {
            adapter1?.fragments!![currentPosition].playVideo(true)
        }
    }

    override fun onSpamComment(commentId: String) {
        if (spamBottomSheet == null || !spamBottomSheet?.isVisible!!) {
            spamBottomSheet = SpamBottomSheet.newInstance(userId, commentId, postId!!, this)
            spamBottomSheet?.show(supportFragmentManager, "SpamBottomSheet")
        }
    }

    override fun onSelfProfileClick() {
        setResult(Activity.RESULT_OK)
        finish()
/*
        if (HomeActivity.homeActivity!=null) {
           // val homeActivity = activity as HomeActivity
            //homeActivity.hideTabBar()
            HomeActivity.homeActivity!!.setTabUI(false)
            HomeActivity.homeActivity!!.setSelected()
        }
        val fragment: Fragment
        fragment = MyProfileFragment()
        val ft =
            supportFragmentManager.beginTransaction()
        val backStateName: String = fragment.javaClass.name
        // ft.hide(this)
        //ft.show(fragment)
        ft.replace(R.id.container, MyProfileFragment(), "MyProfileFragment")
        ft.commit()
*/

    }


    override fun onSpamDialogDismiss() {
        Utils.hideKeyboard(this)

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

    override fun onStop() {
        if (adapter1 != null && lastPosition != -1) {
            if (lastPosition < adapter1?.fragments?.size!!) {
                val lastFragment = adapter1?.fragments?.get(lastPosition)
                lastFragment?.stopPlayer()
            } else {
                for (i in 0 until adapter1?.fragments?.size!!) {
                    val lastFragment = adapter1?.fragments?.get(i)
                    lastFragment?.stopPlayer()
                }
            }
        }
        super.onStop()
    }

    private fun setViewPagerAdapter1() {
        Log.d("call==========", "setViewPagerAdapter1")
        viewPagerPlayer.offscreenPageLimit = 1
        viewPagerPlayer.orientation = ViewPager2.ORIENTATION_VERTICAL

        if (adapter1 == null) {
            adapter1 = null
            adapter1 = PlayerViewPagerAdapter(this, this, this)
            adapter1?.addFragment1(videoList)
            viewPagerPlayer.adapter = adapter1
        } else {
            adapter1?.addFragment1(videoList)
            adapter1?.notifyDataSetChanged()
        }

        viewPagerPlayer.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d("call==========", "onPageSelected")
                if (position < videoList!!.size) {


                    viewpagerposition = position
                    currentPosition = position

                    if (userId.contentEquals(videoList!![position].user_id.toString())) {
                        flag_layout.visibility = View.GONE
                    } else {
                        flag_layout.visibility = View.VISIBLE
                    }

                    if (videoList!![position].no_of_followers != null && videoList!![position].no_of_followers != "") {
                        llView.visibility = View.VISIBLE
                        tv_follower_counts.visibility = View.VISIBLE
                        Methods.showFollowerCounts(
                            videoList!![position].no_of_followers.toString(),
                            tv_follower_counts, this@VideoDetailActivity
                        )
                    } else {
                        llView.visibility = View.GONE
                        tv_follower_counts.visibility = View.GONE
                    }
                    if (videoList!![position].has_liked.toString().contentEquals("1")) {
                        like_image.setImageResource(R.drawable.like_active)
                    }
                    if (videoList!![position].has_liked.toString().contentEquals("0")) {
                        like_image.setImageResource(R.drawable.like_new)
                    }
                    if (videoList!![position].post_like_count != null && videoList!![position].post_like_count != "") {
                        Methods.showLikeCounts(
                            videoList!![position].post_like_count.toString(),
                            like_txt
                        )

                    }

                    if (commentBottomSheet != null && commentBottomSheet?.isVisible!!) {
                        commentBottomSheet?.dismiss()
                    }
                    Log.d("call==========", "position<trendingList")
                    println("==onPageSelected Main $position")
                    if (lastPosition != -1) {
                        val lastFragment = adapter1?.fragments?.get(lastPosition)
                        lastFragment?.stopPlayer()
                    }

                    val currentFragment = adapter1?.fragments?.get(position)
                    println("lastPosition !!!!!!!!!!!!!!" + lastPosition + "position!!!!!!!!!!!!!!" + position)
                    currentFragment?.setVolumeOfVideo()
//                    currentFragment?.playVideo(true)
                    previousPosition = lastPosition
                    lastPosition = position
                    postId = videoList!![position].post_id
                    postCommentCount = videoList!![position].post_comment_count!!

                    otherUserId = videoList!![position].user_id.toString()
                    if (!otherUserId.contentEquals("")) {
                        iv_user_profile.setOnClickListener {
                            if (!userId.contentEquals(otherUserId)) {
                                adapter1?.fragments!![currentPosition].stopPlayer()
                                val intent = Intent(
                                    this@VideoDetailActivity,
                                    OtherUserProfileActivity::class.java
                                )
                                intent.putExtra("OTHER_USER_ID", otherUserId)
                                startActivity(intent)

                            } else {
                                adapter1?.fragments!![currentPosition].stopPlayer()
                                val fragment: Fragment
                                fragment = MyProfileFragment()
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.container, fragment).commit()
                            }
                        }
                    }

                    like_image.setOnClickListener {
                        if (videoList!![position].has_liked == 0) {
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
                                Integer.parseInt(videoList!![position].post_like_count!!) + 1

                            Methods.showViewCounts(likeCounts.toString(), like_txt)
                            likeUnlikeVideo(videoList!![position].post_id, likeCount)
                            videoList!![position].post_like_count = "$likeCounts"
                            videoList!![position].has_liked = 1

                        } else {
                            likeCount = 0
                            like_image.setImageResource(R.drawable.like_new)
                            if (!videoList!![position].post_like_count.toString().contentEquals("0")) {
                                val likeCounts = Integer.parseInt(videoList!![position].post_like_count!!) - 1
                                Methods.showViewCounts(likeCounts.toString(), like_txt)
                                likeUnlikeVideo(videoList!![position].post_id, likeCount)
                                videoList!![position].post_like_count = "$likeCounts"
                                videoList!![position].has_liked = 0
                            }
                        }
                    }
                    delele_post_ll.setOnClickListener {
                        val alertDialog = object : CommonAlertDialog(this@VideoDetailActivity,theme = android.R.style.Theme_Translucent_NoTitleBar) {
                            override fun okClicked() {
                                deletePost(videoList!![lastPosition].post_id.toString(), lastPosition)
                            }
                            override fun cancelClicked() {}
                        }
                        alertDialog.initDialog(resources.getString(R.string.delete), resources.getString(R.string.deletepost))
                        alertDialog.setCancelable(true)
                        alertDialog.show()
                        // showDelDialog(this, "${trendingList!![lastPosition].post_id}")
                    }
                    tv_userName.setOnClickListener {
                        if (otherUserId != "") {
                            if (!userId.contentEquals(otherUserId)) {
                                if (!otherUserId.contentEquals("")) {
                                    adapter1?.fragments!![currentPosition].stopPlayer()
                                    val intent = Intent(
                                        this@VideoDetailActivity,
                                        OtherUserProfileActivity::class.java
                                    )
                                    intent.putExtra("OTHER_USER_ID", otherUserId)
                                    startActivity(intent)
                                }
                            } else {
                                adapter1?.fragments!![currentPosition].stopPlayer()
                                val fragment: Fragment
                                fragment = MyProfileFragment()
                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.container, fragment).commit()
                            }
                        }
                    }
                    if (videoList!![position].post_comment_count != null && videoList!![position].post_comment_count != "") {
                        Methods.showCommentCounts(
                            videoList!![position].post_comment_count.toString(),
                            comment_txt
                        )
                    }
                    if (!screen.contentEquals("myProfile")) {
                        var isFollowingUser = ""
                        for (key in followerMap.keys) {
                            isFollowingUser =
                                followerMap[videoList!![position].user_id.toString()]!!
                        }

                        if (isFollowingUser.contentEquals("1")) {
                            tv_follow_.visibility = View.GONE
                            tv_following_user.visibility = View.VISIBLE
                        }
                        if (isFollowingUser.contentEquals("0")) {
                            tv_follow_.visibility = View.VISIBLE
                            tv_following_user.visibility = View.INVISIBLE
                        }

                        Logger.print("isFollowingUser !!!!!!!!!!!!!!!$isFollowingUser")
                        Logger.print("followerMap !!!!!!!!!!!!!!!${followerMap.size}")
                        val currentUserId =
                            SharedPreferenceUtils.getInstance(this@VideoDetailActivity)
                                .getStringValue(Constants.USER_ID_PREF, "")!!
                        if (videoList!![position].user_id.toString().contentEquals(currentUserId)) {
                            tv_follow_.visibility = View.GONE
                            tv_following_user.visibility = View.INVISIBLE
                        }

                        tv_follow_.setOnClickListener {
                            isLogin =
                                SharedPreferenceUtils.getInstance(context = this@VideoDetailActivity)
                                    .getBoolanValue(Constants.IS_LOGIN, false)
                            if (isLogin) {
                                tv_follow_.visibility = View.GONE
                                tv_following_user.visibility = View.VISIBLE
                                followerMap[videoList!![position].user_id.toString()] = "1"
                                followUser(videoList!![position].user_id.toString())
                            } else {
                                val intent =
                                    Intent(this@VideoDetailActivity, LoginActivity::class.java)
                                intent.putExtra("screen_name", "player")
                                startActivity(intent)
                                finish()
                            }
                        }

                        tv_following_user.setOnClickListener {
                            isLogin =
                                SharedPreferenceUtils.getInstance(context = this@VideoDetailActivity)
                                    .getBoolanValue(Constants.IS_LOGIN, false)
                            if (isLogin) {
                                tv_following_user.visibility = View.INVISIBLE
                                tv_follow_.visibility = View.VISIBLE
                                followerMap[videoList!![position].user_id.toString()] = "0"
                                unFollowUser(videoList!![position].user_id.toString())
                            } else {
                                val intent =
                                    Intent(this@VideoDetailActivity, LoginActivity::class.java)
                                intent.putExtra("screen_name", "player")
                                startActivity(intent)
                                finish()
                            }
                        }
                    }

                    if (videoList!![lastPosition].profile_pic != null && !videoList!![lastPosition].profile_pic!!.contentEquals(
                            ""
                        )
                    ) {
                        Glide.with(this@VideoDetailActivity)
                            .load(videoList!![lastPosition].profile_pic)
                            .error(R.drawable.profile_thumb)
                            .centerCrop()
                            .into(iv_user_profile)
                    } else {
                        iv_user_profile.scaleType = ImageView.ScaleType.FIT_CENTER
                        iv_user_profile.setImageResource(R.drawable.profile_thumb)
                    }
                    if (videoList!![position].mark_as_verified_badge != null && videoList!![position].mark_as_verified_badge!! == 1) {
                        ivVerified?.visibility = View.VISIBLE
                    } else {
                        ivVerified?.visibility = View.GONE

                    }


                    if (videoList!![position].post_comment_count != null && !videoList!![position].post_comment_count!!.contentEquals(
                            ""
                        )
                    ) {
                        Methods.showCommentCountsDialog(
                            videoList!![position].post_comment_count.toString(),
                            comments!!,
                            this@VideoDetailActivity
                        )
                    }
                    forward_layout.setOnClickListener {
                        createLinks(position)
                    }

                    imgDownload.setOnClickListener {
                        val videoWidth = videoList!![position].post_width
                        val videoHeight = videoList!![position].post_height
                        if (videoHeight != null && videoWidth != null) {
                            requestPermission(
                                videoList!![position].post_video!!,
                                videoWidth,
                                videoHeight,
                                true
                            )
                        } else {
                            requestPermission(videoList!![position].post_video!!, 0, 0, true)
                        }
                    }

                    imgExtractAudio.setOnClickListener {
                        isLogin = SharedPreferenceUtils.getInstance(this@VideoDetailActivity)
                            .getBoolanValue(Constants.IS_LOGIN, false)
                        if (isLogin) {
                            if (videoList!![position].post_video != null && videoList!![position].post_video != "") {
                                //  askPermission(trendingList!![position].post_video.toString())
                                requestPermission(
                                    videoList!![position].post_video!!,
                                    0,
                                    0,
                                    false
                                )
                            }
                        } else {
                            val intent = Intent(this@VideoDetailActivity, LoginActivity::class.java)
                            intent.putExtra("screen_name", "startPost")
                            startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
                        }
                    }
                    if (videoList!![position].profile_name != null && !videoList!![position].profile_name!!.contentEquals(
                            ""
                        )
                    ) {
                        tv_userName.text = videoList!![position].profile_name.toString().trim()
                    }
                    if (videoList!![position].category_name != null && !videoList!![position].category_name!!.contentEquals(
                            ""
                        )
                    ) {
                        tv_cat_name.text = videoList!![position].category_name
                    }

                    if (videoList!![position].post_title != null && videoList!![position].post_title != "") {
                        if (videoList!![position].post_title!!.toString().contains("#")) {
                            if (videoList!![position].post_title!!.toString().trim()
                                    .contains(" ")
                            ) {
                                Logger.print(" contain space only multiple hash tag hash tag @@@@@@@@@@@@@" + videoList!![position].post_title!!.toString())
                                val str = videoList!![position].post_title!!.toString()
                                val splited = str.split(" ").toTypedArray()

                                val builder = SpannableStringBuilder()
                                for (word in splited) {
                                    builder.append(word).append(" ")
                                        .setSpan(object : ClickableSpan() {
                                            override fun onClick(@NonNull view: View) {
                                                if (word.startsWith("#")) {
                                                    val intent = Intent(
                                                        this@VideoDetailActivity,
                                                        HashTagDetailOrTrendingDetail::class.java
                                                    )
                                                    intent.putExtra("hashTag", word)
                                                    startActivity(intent)
                                                }
                                            } // optional - for styling the specific text

                                            override fun updateDrawState(ds: TextPaint) {
                                                super.updateDrawState(ds)
                                                ds.color = ContextCompat.getColor(
                                                    this@VideoDetailActivity,
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
                                        this@VideoDetailActivity,
                                        R.color.hash_tag
                                    )
                                )
                                tvPostTitle.setOnClickListener {}
                            } else {
                                Logger.print("not contain space only one hash tag @@@@@@@@@@@@@" + videoList!![position].post_title!!.toString())
                                tvPostTitle.text = videoList!![position].post_title
                                tvPostTitle.setTextColor(
                                    ContextCompat.getColor(
                                        this@VideoDetailActivity,
                                        R.color.hash_tag
                                    )
                                )

                                tvPostTitle.setOnClickListener {
                                    val intent = Intent(
                                        this@VideoDetailActivity,
                                        HashTagDetailOrTrendingDetail::class.java
                                    )
                                    intent.putExtra(
                                        "hashTag",
                                        videoList!![position].post_title.toString()
                                    )
                                    startActivity(intent)
                                }
                            }
                        } else {
                            tvPostTitle.text = videoList!![position].post_title
                            tvPostTitle.setTextColor(
                                ContextCompat.getColor(
                                    this@VideoDetailActivity,
                                    R.color.white
                                )
                            )
                            tvPostTitle.setOnClickListener {}
                        }
                    }

                    if (videoList!![position].post_view_count != null && videoList!![position].post_view_count != "") {
                        Methods.showViewCounts(
                            videoList!![position].post_view_count.toString(),
                            tvpostCount
                        )
                    }

                    if (videoList!![position].post_comment_count != null && videoList!![position].post_comment_count.toString() != "") {
                        Methods.showCommentCounts(
                            videoList!![position].post_comment_count.toString(),
                            comment_txt
                        )
                    }
                    if (videoList!![position].post_comment_count != null && !videoList!![position].post_comment_count!!.contentEquals(
                            ""
                        )
                    ) {
                        Methods.showCommentCountsDialog(
                            videoList!![position].post_comment_count.toString(),
                            comments!!,
                            this@VideoDetailActivity
                        )
                    }


                    heart_layout.setOnClickListener {}

                    comment.setOnClickListener {
                        if (adapter1?.fragments != null) {
                            val lastFragment = adapter1?.fragments?.get(lastPosition)
                            lastFragment?.stopPlayer()
                        }
                        commentBottomSheet = CommentBottomSheet.newInstance(
                            userId,
                            postId!!, postCommentCount, this@VideoDetailActivity
                        )
                        commentBottomSheet?.show(supportFragmentManager, "Comment_Bottom")
                    }


                    if (!isDataFinished && position == videoList!!.size - 2) {
                        pageCount += 1
                        if (screen != "") {
                            when (screen) {
                                "discover" -> {
                                    getDiscoverDetails(catId)
                                }

                                "trending" -> {
                                    hashTagDetail(hashtag)
                                }

                                "otherProfile" -> {
                                    getUserProfile(false, userIDApi)
                                }
                                "myProfile" -> {
                                    getUserProfile(false, userIDApi)

                                }
                                "searchDetail" -> {

                                    getViewMoreVideoListing(searchTxt)
                                }
                                "discoverDetail" -> {
                                    getDiscoverDetails(catId)
                                }
                                "hashtagDetail" -> {
                                    hashTagDetail(hashtag)
                                }
                                "search" -> {
                                    getViewMoreVideoListing(searchTxt)

                                }
                            }
                        }
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                Logger.print("onPageScrollStateChanged $state")
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
                Logger.print("onPageScrolled Position $position PositionOffset $positionOffset PositionOffsetPixels $positionOffsetPixels")
                if (isOnScrollCall) {
                    isOnScrollCall = false
                    if (positionOffset < 0.2f) {
                        if (position < videoList?.size?.minus(1) ?: 0) {
                            Logger.print("Going to play video at ${position + 1}")
                            val lastFragment = adapter1?.fragments?.get(position + 1)
                            lastFragment?.playVideo(false)
                        }
                    } else {
                        if (position > 0) {
                            Logger.print("Going to play video at ${position - 1}")
                            val lastFragment = adapter1?.fragments?.get(position - 1)
                            lastFragment?.playVideo(false)
                        }
                    }
                }
            }
        })
        viewPagerPlayer.setCurrentItem(pos, false)
        Handler().postDelayed({
            val lastFragment = adapter1?.fragments?.get(pos)
            lastFragment?.playVideo(true)
        }, 250)
    }

    private fun hashTagDetail(hashTagName: String) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["hashtag"] = hashTagName.trim()

        val isProgressShow = pageCount == 1
        viewModel.observeHashTagDetail(this, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                Logger.print("getDiscoverDetails view all response : $it")
                //removeLoaderView()
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("pagination_count")) {
                        paginationCount = jsonObject.getInt("pagination_count")
                    }
                    if (jsonObject.has("data")) {
                        val jsonObj1 = jsonObject.getJSONObject("data")
                        val jsonArray = jsonObj1.getJSONArray("user_video_list")
                        val array = Gson().fromJson<ArrayList<ForYouResponse>>(jsonArray.toString(),
                            object : TypeToken<ArrayList<ForYouResponse>>() {}.type)

                        if (pageCount == 1) {
                            videoList!!.clear()
                            if (adapter1 != null) {
                                adapter1!!.removeAllFragment()
                            }
                            adapter1 = null
                            followerMap.clear()
                        }

                        if (array.isNotEmpty()) {
                            videoList!!.addAll(array)
                            for (i in videoList!!.indices) {
                                followerMap[videoList!![i].user_id.toString()] = videoList!![i].is_following.toString()
                            }
                            if (pageCount == 1) {
                                setViewPagerAdapter1()
                            } else {
                                val oldSize = adapter1?.addFragment1(array)
                                adapter1?.notifyItemRangeInserted(oldSize!!, array.size)
                            }
                        } else {
                            isDataFinished = true
                        }
                        if (videoList!!.size > 0) {
                            isLoading = false
                        }
                    } else {
                        isDataFinished = true
                    }
                } else {
                    if (pageCount == 1) {
                        Utils.showToastMessage(this, jsonObject["message"].toString())
                    }
                }
            }
        })
    }

    private fun getDiscoverDetails(cat_Id: String) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["category_id"] = cat_Id.trim()

        val isProgressShow = pageCount == 1
        viewModel.getDiscoverDetails(this, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                Logger.print("getDiscoverDetails view all response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("pagination_count")) {
                        paginationCount = jsonObject.getInt("pagination_count")
                    }
                    if (jsonObject.has("data")) {
                        val jsonObj1 = jsonObject.getJSONObject("data")
                        /*if (jsonObj1.has("min_public_id")) {
                            minPublicId = jsonObj1.getInt("min_public_id")
                        }*/

                            val jsonArray = jsonObj1.getJSONArray("user_video_list")
                            val array =
                                Gson().fromJson<ArrayList<ForYouResponse>>(
                                    jsonArray.toString(),
                                    object : TypeToken<ArrayList<ForYouResponse>>() {}.type
                                )

                            if (pageCount == 1) {

                                videoList!!.clear()
                                if (adapter1 != null) {
                                    adapter1!!.removeAllFragment()
                                }
                                adapter1 = null
                                followerMap.clear()
                                /* followerMap.clear()
                                 hasLikedMap.clear()
                                 likeVideoHash.clear()*/
                            }

                            if (array.isNotEmpty()) {
                                videoList!!.addAll(array)
                                for (i in videoList!!.indices) {
                                    followerMap[videoList!![i].user_id.toString()] =
                                        videoList!![i].is_following.toString()
                                }
                                if (pageCount == 1) {
                                    setViewPagerAdapter1()
                                } else {
                                    val oldSize = adapter1?.addFragment1(array)
                                    adapter1?.notifyItemRangeInserted(oldSize!!, array.size)
                                }
                            } else {
                                isDataFinished = true
                            }

                            if (videoList!!.size > 0) {
                                isLoading = false
                            }
                        } else {
                            isDataFinished = true
                        }
                    } else {
                        if (pageCount == 1) {
                            Utils.showToastMessage(this, jsonObject["message"].toString())
                        }
                    }
                }
            }
        )
    }

    private fun getUserProfile(ISProgress: Boolean, userID: String) {
        isLoading = true
        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["other_user_id"] = userID
        val isProgressShow: Boolean = if (ISProgress) {
            pageCount == 1
        } else {
            false
        }

        viewModel.observeGetProfile(this, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")

                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val jsonobj1 = jsonObject.getJSONObject("data")
                        val jsonArray = jsonobj1.getJSONArray("user_post")

                        if (pageCount == 1) {

                            if (adapter1 != null) {
                                adapter1!!.removeAllFragment()
                            }
                            adapter1 = null
                        }

                        val array = Gson().fromJson<ArrayList<ForYouResponse>>(
                            jsonArray.toString(),
                            object : TypeToken<ArrayList<ForYouResponse>>() {}.type
                        )
                        if (pageCount == 1) {
                            videoList!!.clear()
                            if (adapter1 != null) {
                                adapter1!!.removeAllFragment()
                            }
                            adapter1 = null
                            followerMap.clear()
                        }

                        if (array.isNotEmpty()) {
                            isDataFinished = false
                            videoList!!.addAll(array)
                            for (i in videoList!!.indices) {
                                followerMap[videoList!![i].user_id.toString()] =
                                    videoList!![i].is_following.toString()
                            }
                            if (pageCount == 1) {
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


                        isLoading = false
                    }
                }

            }
        })
    }

    private fun getViewMoreVideoListing(search: String) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["search_text"] = search
        val isProgressShow: Boolean = pageCount == 1
        viewModel.getVideoResult(this, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                Logger.print("My SEARCHVIEWMOREVIDEOS Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("pagination_count")) {
                        paginationCount = jsonObject.getInt("pagination_count")
                    }

                    if (jsonObject.has("data")) {
                        val jsonObj1 = jsonObject.getJSONObject("data")
                        val jsonArray = jsonObj1.getJSONArray("user_video_list")

                        val array = Gson().fromJson<ArrayList<ForYouResponse>>(
                            jsonArray.toString(),
                            object : TypeToken<ArrayList<ForYouResponse>>() {}.type
                        )

                        if (pageCount == 1) {
                            videoList!!.clear()
                            followerMap.clear()
//                            hasLikedMap.clear()
//                            likeVideoHash.clear()
                            if (adapter1 != null) {
                                adapter1!!.removeAllFragment()
                            }
                            adapter1 = null
                        }

                        if (array.isNotEmpty()) {
                            videoList!!.addAll(array)
                            for (i in videoList!!.indices) {
                                followerMap[videoList!![i].user_id.toString()] =
                                    videoList!![i].is_following.toString()
                            }
                            /* for (i in videosList.indices) {
                                 followerMap[videosList[i].user_id.toString()] =
                                     videosList[i].is_following.toString()
                                 likeVideoHash[videosList[i].post_id.toString()] =
                                     videosList[i].post_like_count.toString()
                                 hasLikedMap[videosList[i].post_id.toString()] =
                                     videosList[i].has_liked.toString()
                             }*/

                            if (pageCount == 1) {
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
                    }
                }

                if (code == 0) {
                    if (pageCount == 1) {
                        videoList!!.clear()

                    }
                }
            }
        })
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

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    private fun deletePost(POST_ID: String, position: Int) {
        val hashMap = HashMap<String, String>()
        hashMap["post_id"] = POST_ID
        showProgressDialog()
        viewModel.DeletePost(this, hashMap).observe(this, Observer {
            hideProgressDialog()
            if (it != null) {
                Logger.print("DELETE POST FROM PLAYER Response :::::::::::::::::::::::::: $it")
                val jsonObject = JSONObject(it.toString())
                if (jsonObject["code"] == 1) {
                    SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
//                    videoList!!.removeAt(position)
                    this.finish()
                }
            }
        })
    }

    private fun unFollowUser(userID: String) {
        val isFollow = "0"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = userID
        viewModel.FollowUnfollow(this, hashMap).observe(
            this, Observer {
                if (it != null) {
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_CHANGE_LIKE_OR_COMMENT_OR_FOLLOW, true)
                    Logger.print("Unfollowuser Whom im following Response : $it")
                }
            }
        )
    }

    private fun followUser(user_ID: String) {
        val isFollow = "1"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = user_ID

        viewModel.FollowUnfollow(this, hashMap).observe(
            this, Observer {
                if (it != null) {
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_CHANGE_LIKE_OR_COMMENT_OR_FOLLOW, true)
                    Logger.print("FOLLOWUNFOLLOWUSER Response : $it")
                }
            }
        )
    }

    private fun likeUnlikeVideo(post_id: Int?, like_unlike: Int) {
        val hashMap = HashMap<String, String>()
        hashMap["is_like"] = like_unlike.toString()
        hashMap["post_id"] = post_id.toString()
        viewModel.likeUnlikeVideo(this, hashMap, false).observe(this, Observer {
            if (it != null) {
                SharedPreferenceUtils.getInstance(this)
                    .setValue(Constants.IS_CHANGE_LIKE_OR_COMMENT_OR_FOLLOW, true)
                if (isShowDelete) {
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                }

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

    private fun createShareUri(postId: String): Uri {
        val builder = Uri.Builder()
        builder.scheme("https")
            .authority("link.smylee.app")
            .appendPath("video")
            .appendQueryParameter("postId", postId)

        return builder.build()
    }

    private fun createLinks(position: Int) {
        val iOSParams = DynamicLink.IosParameters.Builder("com.smylee.app")
        iOSParams.appStoreId = Constants.APP_STORE_ID

        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(createShareUri(videoList!![position].post_id.toString()))
            .setDomainUriPrefix("https://link.smylee.app")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("smylee.app").build()

            )
            .setIosParameters(iOSParams.build())

            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters
                    .Builder()
                    .setTitle(videoList!![position].post_title!!)
                    .setImageUrl(Uri.parse(videoList!![position].post_video_thumbnail))
                    .build()

            )
            .buildShortDynamicLink()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    sharingLink = it.result!!.shortLink.toString()
                    Logger.print("Sharing Link $sharingLink")
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    // sendIntent.putExtra(Intent.EXTRA_TEXT, "Hii Checkout $sharingLink")
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
                            this@VideoDetailActivity,
                            videoLink,
                            videoWidth,
                            videoHeight,
                            this@VideoDetailActivity
                        )
                    }
                    // check for permanent denial of any permission show alert dialog
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
                            this@VideoDetailActivity,
                            videoLink,
                            this@VideoDetailActivity
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

    private fun randomColor(): Int {
        return Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255))
    }

    companion object {
        var videoDetailActivity: VideoDetailActivity? = null

    }
}