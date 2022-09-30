package smylee.app.home

import android.Manifest
import android.app.AlertDialog
import android.content.Context
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
import kotlinx.android.synthetic.main.fragment_following.*
import kotlinx.android.synthetic.main.fragment_following.btnSignUp
import kotlinx.android.synthetic.main.fragment_following.comment
import kotlinx.android.synthetic.main.fragment_following.comment_txt
import kotlinx.android.synthetic.main.fragment_following.flag_layout
import kotlinx.android.synthetic.main.fragment_following.forward_layout
import kotlinx.android.synthetic.main.fragment_following.heart_layout
import kotlinx.android.synthetic.main.fragment_following.imgDownload
import kotlinx.android.synthetic.main.fragment_following.imgExtractAudio
import kotlinx.android.synthetic.main.fragment_following.imgLikeDoubleTap
import kotlinx.android.synthetic.main.fragment_following.ivFlag
import kotlinx.android.synthetic.main.fragment_following.ivVerified
import kotlinx.android.synthetic.main.fragment_following.iv_user_profile
import kotlinx.android.synthetic.main.fragment_following.like_image
import kotlinx.android.synthetic.main.fragment_following.like_txt
import kotlinx.android.synthetic.main.fragment_following.llUserDetails
import kotlinx.android.synthetic.main.fragment_following.llView
import kotlinx.android.synthetic.main.fragment_following.side_menu
import kotlinx.android.synthetic.main.fragment_following.swipeRefresh
import kotlinx.android.synthetic.main.fragment_following.tvPostTitle
import kotlinx.android.synthetic.main.fragment_following.tv_cat_name
import kotlinx.android.synthetic.main.fragment_following.tv_follower_counts
import kotlinx.android.synthetic.main.fragment_following.tv_userName
import kotlinx.android.synthetic.main.fragment_following.viewOverLayTab
import kotlinx.android.synthetic.main.fragment_notification_fragement.*
import org.json.JSONObject
import smylee.app.CallBacks.DoubleTapCallBacks
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
import smylee.app.ui.base.BaseFragment_x
import smylee.app.utils.*
import uz.jamshid.library.progress_bar.CircleProgressBar
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class FollowingFragment : BaseFragment_x(), OnVideoPlayingListener, InitNextVideoListener,
    OnShareVideoPrepareListener,
    DoubleTapCallBacks, ExtractListner, OnCommentAdded, OnSpamDialogListener {
    private var topic: Topic? = null
    var position: Int? = 0
    var otherUserId: String = ""
    private var likeCount: Int = 0
    private val mRandom = Random()
    private var currentPosition: Int = 0
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var isLogin: Boolean = false
    private var viewpagerposition: Int = 0
    /*var commentBottomSheet: CommentBottomSheet? = null
    var spamBottomSheet: SpamBottomSheet? = null*/
    var userId: String = ""
    private var tvNoData: TextView? = null
    private var isFirstTimePlay = true

    private var reaction: Reaction? = null
    private var adapter1: PlayerViewPagerAdapter? = null
    var lastPosition = -1
    var previousPosition = -1
    lateinit var viewModel: HomeViewModel
    private var pageCountFollowing = 1
    private var isDataFinished: Boolean = false
    private var categoryvideo = ArrayList<ForYouResponse>()

    private var llMainComment: LinearLayout? = null
    private var rvComments: RecyclerView? = null
    private var ivClose: ImageView? = null
    private var tvLike: AppCompatTextView? = null
    var comments: AppCompatTextView? = null
    private var ivSend: AppCompatImageView? = null
    private var edtComment: AppCompatEditText? = null
    private var postId: Int? = null
    private var postCommentCount: String? = null
    private var ignoreFirstTime = true

    lateinit var activity: HomeActivity
    var array: ArrayList<Topic>? = null
    private var likeVideoHashFollowing: HashMap<String, String> = HashMap()
    private var hasLikedMapFollowing: HashMap<String, String> = HashMap()
    private var commentCountMapFollowing: HashMap<String, String> = HashMap()
    private var bottomTabHeight: Int = 0

    private var lastAPICallTime: Long = 0
    private var sharingLink: String = ""
    private var callOnResume: Boolean = false
    private var isOnScrollCall = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topic = arguments?.getParcelable("topic")
        position = arguments?.getInt("position", 0)
        reaction = arguments?.getParcelable("reaction")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as HomeActivity
    }

    override fun onPause() {
        Log.i("FollowingFragment", "onPause")
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
                    val lastFragment = adapter1?.fragments?.get(i)
                    lastFragment?.stopPlayer()
                }
            }
        }
        super.onStop()
    }

    override fun onDetach() {
        adapter1 = null
        super.onDetach()
    }

    private fun createLinks(position: Int) {
        val iOSParams = DynamicLink.IosParameters.Builder("com.smylee.app")
        iOSParams.appStoreId = Constants.APP_STORE_ID
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(createShareUri(categoryvideo[position].post_id.toString()))
            .setDomainUriPrefix("https://link.smylee.app")
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder("smylee.app").build())
            .setIosParameters(iOSParams.build())
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters
                    .Builder()
                    .setTitle(categoryvideo[position].post_title!!)
                    .setImageUrl(Uri.parse(categoryvideo[position].post_video_thumbnail))
                    .build()
            )
            .buildShortDynamicLink()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    sharingLink = it.result!!.shortLink.toString()
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(Intent.EXTRA_TEXT,"Checkout this interesting video from Smylee App : $sharingLink")
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

    fun setTabHeight(bottomTabHeight: Int) {
        this.bottomTabHeight = bottomTabHeight
        if (llUserDetails != null) {
            val layoutParams = llUserDetails.layoutParams as RelativeLayout.LayoutParams
            layoutParams.bottomMargin = bottomTabHeight
            val layoutParamsSideMenu = side_menu.layoutParams as RelativeLayout.LayoutParams
            layoutParamsSideMenu.bottomMargin = bottomTabHeight
            val layoutParamOverLayTab = viewOverLayTab.layoutParams as RelativeLayout.LayoutParams
            layoutParamOverLayTab.height = bottomTabHeight
        }
    }

    private fun setViewPagerAdapter1() {
        viewPager_following?.offscreenPageLimit = 2
        viewPager_following?.orientation = ViewPager2.ORIENTATION_VERTICAL
        if (adapter1 == null) {
            adapter1 = null
            adapter1 = PlayerViewPagerAdapter(this, this, this)
            adapter1?.addFragment1(categoryvideo)
            viewPager_following?.adapter = adapter1
        } else {
            adapter1?.addFragment1(categoryvideo)
            adapter1?.notifyDataSetChanged()
        }

        viewPager_following?.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position < categoryvideo.size) {
                    currentPosition = position
                    if (commentBottomSheet != null && commentBottomSheet?.isVisible!!) {
                        commentBottomSheet?.dismiss()
                    }

                    if (categoryvideo.size > 0) {
                        viewpagerposition = position
                        iv_user_profile.setImageBitmap(null)
                        iv_user_profile.setBackgroundResource(R.drawable.profile_thumb)

                        if (categoryvideo[position].no_of_followers != null && categoryvideo[position].no_of_followers != "") {
                            llView.visibility = View.VISIBLE
                            tv_follower_counts.visibility = View.VISIBLE
                            Methods.showFollowerCounts(categoryvideo[position].no_of_followers.toString(),tv_follower_counts, context)
                        } else {
                            llView.visibility = View.GONE
                            tv_follower_counts.visibility = View.GONE
                        }
                        if (userId.contentEquals(categoryvideo[position].user_id.toString())) {
                            flag_layout.visibility = View.GONE
                        } else {
                            flag_layout.visibility = View.VISIBLE
                        }

                        otherUserId = categoryvideo[position].user_id.toString()
                        if (!otherUserId.contentEquals("")) {
                            iv_user_profile.setOnClickListener {
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
                                    fragmentManager!!.beginTransaction().replace(R.id.container, fragment).commit()
//                                    childFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
                                }
                            }
                        }
                        tv_userName.setOnClickListener {
                            if (otherUserId != "") {
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
                                    fragmentManager!!.beginTransaction().replace(R.id.container, fragment).commit()
//                                    childFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
                                }
                            }
                        }
                        iv_user_profile.visibility = View.VISIBLE
                        side_menu.visibility = View.VISIBLE
                        if (categoryvideo[position].profile_pic != null && categoryvideo[position].profile_pic!! != "") {
                            iv_user_profile.scaleType = ImageView.ScaleType.CENTER_CROP
                            Glide.with(context!!)
                                .load(categoryvideo[position].profile_pic)
                                .error(R.drawable.profile_thumb)
                                .centerCrop()
                                .into(iv_user_profile)
                        } else {
                            iv_user_profile.scaleType = ImageView.ScaleType.FIT_CENTER
                            iv_user_profile.setImageResource(R.drawable.profile_thumb)
                        }

                        postId = categoryvideo[position].post_id
                        postCommentCount = categoryvideo[position].post_comment_count

                        forward_layout.setOnClickListener {
                            createLinks(position)
                        }

                        imgDownload.setOnClickListener {
                            val videoWidth = categoryvideo[position].post_width
                            val videoHeight = categoryvideo[position].post_height
                            if (videoHeight != null && videoWidth != null) {
                                requestPermission(categoryvideo[position].post_video!!, videoWidth, videoHeight,true)
                            } else {
                                requestPermission(categoryvideo[position].post_video!!, 0, 0, true)
                            }
                        }
                        imgExtractAudio.setOnClickListener {
                            isLogin = SharedPreferenceUtils.getInstance(activity)
                                .getBoolanValue(Constants.IS_LOGIN, false)
                            if (isLogin) {
                                if (categoryvideo[position].post_video != null && categoryvideo[position].post_video != "") {
                                    requestPermission(categoryvideo[position].post_video!!,0,0,false)
                                }
                            } else {
                                val intent = Intent(getActivity(), LoginActivity::class.java)
                                intent.putExtra("screen_name", "startPost")
                                startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
                            }
                        }
                        if (categoryvideo[position].profile_name != null && !categoryvideo[position].profile_name!!.contentEquals("")) {
                            tv_userName.text = categoryvideo[position].profile_name.toString().trim()
                        }
                        if (categoryvideo[position].category_name != null && !categoryvideo[position].category_name!!.contentEquals(
                                "")) {
                            tv_cat_name.text = categoryvideo[position].category_name
                        }

                        if (categoryvideo[position].post_title != null && categoryvideo[position].post_title != "") {
                            if (categoryvideo[position].post_title!!.toString().contains("#")) {
                                if (categoryvideo[position].post_title!!.toString().trim().contains(" ")) {
                                    val str = categoryvideo[position].post_title!!.toString()
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
                                    Logger.print("not contain space only one hash tag @@@@@@@@@@@@@" + categoryvideo[position].post_title!!.toString())
                                    tvPostTitle.text = categoryvideo[position].post_title
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
                                            categoryvideo[position].post_title.toString()
                                        )
                                        startActivity(intent)
                                    }
                                }
                            } else {
                                tvPostTitle.text = categoryvideo[position].post_title
                                tvPostTitle.setTextColor(
                                    ContextCompat.getColor(
                                        activity,
                                        R.color.white
                                    )
                                )
                                tvPostTitle.setOnClickListener {}
                            }
                        }

                        if (categoryvideo[position].post_comment_count != null && !categoryvideo[position].post_comment_count!!.contentEquals(
                                ""
                            )
                        ) {
                            Methods.showCommentCounts(
                                categoryvideo[position].post_comment_count.toString(),
                                comment_txt
                            )
                        }
                        if (categoryvideo[position].post_comment_count != null && !categoryvideo[position].post_comment_count!!.contentEquals(
                                ""
                            )
                        ) {
                            Methods.showCommentCountsDialog(
                                categoryvideo[position].post_comment_count.toString(),
                                comments,
                                context
                            )
                        }
                        if (categoryvideo[position].mark_as_verified_badge != null && categoryvideo[position].mark_as_verified_badge!! == 1) {
                            ivVerified?.visibility = View.VISIBLE
                        } else {
                            ivVerified?.visibility = View.GONE

                        }



                        if (categoryvideo[position].post_view_count != null && categoryvideo[position].post_view_count != "") {
                            Methods.showViewCounts(
                                categoryvideo[position].post_view_count.toString(),
                                tvpostCountFollowing
                            )
                        }
                        if (categoryvideo[position].has_liked.toString().contentEquals("1")) {
                            like_image.setImageResource(R.drawable.like_active)
                        }
                        if (categoryvideo[position].has_liked.toString().contentEquals("0")) {
                            like_image.setImageResource(R.drawable.like_new)
                        }
                        if (categoryvideo[position].post_like_count != null && !categoryvideo[position].post_like_count!!.contentEquals(
                                ""
                            )
                        ) {
                            Methods.showLikeCounts(
                                categoryvideo[position].post_like_count.toString(),
                                like_txt
                            )
                        }

                        like_image.setOnClickListener {
                            if (categoryvideo[position].has_liked == 0) {
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
                                    Integer.parseInt(categoryvideo[position].post_like_count!!) + 1
                                Logger.print("like Counts!!!!!!!!!!!!!!!!!!!!!!!!!!$likeCounts")

                                Methods.showLikeCounts(likeCounts.toString(), like_txt)
                                likeUnlikeVideo(categoryvideo[position].post_id, likeCount)
                                categoryvideo[position].post_like_count = "$likeCounts"
                                categoryvideo[position].has_liked = 1
                            } else {
                                likeCount = 0
                                like_image.setImageResource(R.drawable.like_new)
                                if (!categoryvideo[position].post_like_count.toString()
                                        .contentEquals("0")
                                ) {
                                    val likeCounts =
                                        Integer.parseInt(categoryvideo[position].post_like_count!!) - 1
                                    Logger.print("unlike Counts!!!!!!!!!!!!!!!!!!!!!!!!!!$likeCounts")
                                    Methods.showLikeCounts(likeCounts.toString(), like_txt)
                                    likeUnlikeVideo(categoryvideo[position].post_id, likeCount)
                                    categoryvideo[position].post_like_count = "$likeCounts"
                                    categoryvideo[position].has_liked = 0
                                }
                            }
                        }
                    }

                    if (lastPosition != -1) {
//                        downloadHandler.removeCallbacks(downloadRunnable)
                        val lastFragment = adapter1?.fragments?.get(lastPosition)
                        lastFragment?.stopPlayer()
                    }
                    if (!isFirstTimePlay) {
                        val lastFragment = adapter1?.fragments?.get(position)
                        lastFragment?.setVolumeOfVideo()
                    } else {
                        isFirstTimePlay = false
                    }
                    previousPosition = lastPosition
                    lastPosition = position
                    if (!isDataFinished && position == categoryvideo.size - 2) {
                        pageCountFollowing += 1
                        //commented for streaming
                        Logger.print("pageCountFollowing!!!!!!!$pageCountFollowing")
                        getFollowingVideoList(true)
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
                        if (position < categoryvideo.size - 1) {
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
        Handler().postDelayed({
            viewPager_following?.setCurrentItem(0, false)
            val lastFragment = adapter1?.fragments?.get(0)
            lastFragment?.playVideo(true)
        }, 1000)
    }

    private fun randomColor(): Int {
        return Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255))
    }

    fun playVideo() {
        Log.i("FollowingFragment", "playVideo $ignoreFirstTime")
        if (categoryvideo.size > 0) {
            if (lastPosition != viewPager_following?.currentItem) {
                viewPager_following?.setCurrentItem(lastPosition, false)
            } else {

                val lastFragment = adapter1?.fragments?.get(lastPosition)
                lastFragment?.playVideo(true)


            }
        } else {
            getFollowingVideoList(true)
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

    private fun likeUnlikeVideo(post_id: Int?, like_unlike: Int) {
        val hashMap = HashMap<String, String>()
        hashMap["is_like"] = like_unlike.toString()
        hashMap["post_id"] = post_id.toString()
        viewModel.likeUnlikeVideo(activity, hashMap, false).observe(this, Observer {
            if (it != null) {
                SharedPreferenceUtils.getInstance(activity)
                    .setValue(Constants.IS_CHANGE_LIKE_OR_COMMENT_OR_FOLLOW, true)
                Logger.print("LIKE VIDEO OR UNLIKE VIDEO!!!!!!!!!!!!!!!!!!!$it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 0) {
                    Utils.showAlert(activity, "", jsonObject["message"].toString())
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val isAnythingChangeFollowingForBlock = SharedPreferenceUtils.getInstance(activity)
            .getBoolanValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, false)

        Logger.print("onResume FollowingFragment !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        Log.i("FollowingFragment", "onResume ${HomePagerAdapter.isCallOnResume}")
        if (!callOnResume) {
            callOnResume = true
        } else if (SharedPreferenceUtils.getInstance(activity).getBoolanValue(
                Constants.IS_ANYTHING_CHANGE_FOLLOWING,
                false
            ) && HomePagerAdapter.currentTab == 1
        ) {
            SharedPreferenceUtils.getInstance(activity)
                .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING, false)
            lastAPICallTime = System.currentTimeMillis()
            adapter1 = null
            pageCountFollowing = 1
            //commented for streaming
            getFollowingVideoList(true)
        } else if (isAnythingChangeFollowingForBlock && HomePagerAdapter.currentTab == 1 && HomeActivity.tabIndex == 0 || adapter1 == null) {
            lastAPICallTime = System.currentTimeMillis()
            pageCountFollowing = 1
            getFollowingVideoList(true)
            SharedPreferenceUtils.getInstance(activity)
                .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, false)
        }
    }

    fun callForBlockFollowing() {
        if (SharedPreferenceUtils.getInstance(activity)
                .getBoolanValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, false) || adapter1 == null
        ) {
            lastAPICallTime = System.currentTimeMillis()
            pageCountFollowing = 1
            getFollowingVideoList(true)
            SharedPreferenceUtils.getInstance(activity)
                .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, false)
        }
    }

    private fun requestPermission(
        videoLink: String,
        videoWidth: Int,
        videoHeight: Int,
        isDownload: Boolean
    ) {
        HomePagerAdapter.isCallOnResume = 2
        activity.isAttachFragmentAgain = false
        Dexter.withActivity(activity).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (isDownload) {
                    // check if all permissions are granted or not
                    if (report.areAllPermissionsGranted()) {
                        activity.showProgressDialog()
                        ShareVideoUtils.shareVideoFile(
                            context!!,
                            videoLink,
                            videoWidth,
                            videoHeight,
                            this@FollowingFragment
                        )
                    }
                    // check for permanent denial of any permission show alert dialog
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
                            this@FollowingFragment
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
        tvLike = view.findViewById(R.id.like_txt)
        firebaseAnalytics = FirebaseAnalytics.getInstance(activity)

        isLogin =
            SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_LOGIN, false)
        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()

        if (isLogin) {
            tv_nodata_following?.visibility = View.GONE
            btnSignUp?.visibility = View.GONE
            Logger.print("isLogin!!!!!!!!!!!!!!!!!!")
        } else {
            tv_nodata_following?.visibility = View.VISIBLE
            btnSignUp?.visibility = View.VISIBLE
            Logger.print("not isLogin!!!!!!!!!!!!!!!!!!")

        }
        firebaseAnalytics.setUserId(userId)

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, userId)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

        Logger.print("USER_ID============$userId")
        if (bottomTabHeight > 0) {
            val layoutParams = llUserDetails.layoutParams as RelativeLayout.LayoutParams
            layoutParams.bottomMargin = bottomTabHeight
            val layoutParamsSideMenu = side_menu.layoutParams as RelativeLayout.LayoutParams
            layoutParamsSideMenu.bottomMargin = bottomTabHeight
            val layoutParamOverLayTab = viewOverLayTab.layoutParams as RelativeLayout.LayoutParams
            layoutParamOverLayTab.height = bottomTabHeight
            viewOverLayTab.layoutParams = layoutParamOverLayTab
        }

        llMainComment = view.findViewById(R.id.llMainComment) as LinearLayout
        rvComments = view.findViewById<View>(R.id.rv_comments) as RecyclerView
        comments = view.findViewById<View>(R.id.comments) as AppCompatTextView
        tvNoData = view.findViewById<View>(R.id.tv_nodata) as TextView

        ivClose = view.findViewById<View>(R.id.iv_close) as ImageView
        ivSend = view.findViewById<View>(R.id.iv_send) as AppCompatImageView
        edtComment = view.findViewById<View>(R.id.edt_comment) as AppCompatEditText
        rvComments!!.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        btnSignUp?.setOnClickListener {
            val intent = Intent(getActivity(), LoginActivity::class.java)
            intent.putExtra("screen_name", "following")
            activity.startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
        }
        Methods.setImageRotation(imgExtractAudio)
        ivFlag.setOnClickListener {
            if (postId != null) {
                if (adapter1?.fragments != null) {
                    val lastFragment = adapter1?.fragments?.get(lastPosition)
                    lastFragment?.stopPlayer()
                }
                if (spamBottomSheet == null || !spamBottomSheet?.isVisible!!) {
                    spamBottomSheet =
                        SpamBottomSheet.newInstance(userId, "", postId!!, this@FollowingFragment)
                    spamBottomSheet?.show(fragmentManager!!, "SpamBottomSheet")
//                    spamBottomSheet?.show(childFragmentManager, "SpamBottomSheet")
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
                        this@FollowingFragment
                    )
                }
                commentBottomSheet?.show(fragmentManager!!, "Comment_Bottom")
//                commentBottomSheet?.show(childFragmentManager, "Comment_Bottom")
            }
        }
        //   swipeRefresh?.setProgressViewOffset(false, 0, 130)
//        val cc = CircleProgressBar(activity)
//        cc.setSize(50)
        swipeRefresh?.setCustomBar(Circle(activity))

        swipeRefresh?.setRefreshListener {
            lastAPICallTime = System.currentTimeMillis()
            pageCountFollowing = 1
            getFollowingVideoList(false)
        }

        like_image.setOnClickListener {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_following, null, false)
    }

    private fun getFollowingVideoList(isProgress: Boolean) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCountFollowing.toString()

        var isProgressShow = false
        if (isProgress) {
            isProgressShow = pageCountFollowing == 1
        }
        viewModel.getFollowingAPI(activity, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                //  swipeRefresh?.isRefreshing = false
                swipeRefresh?.setRefreshing(false)
                Logger.print("FOLLOWING  Home response ::::::::: $it")
                if (pageCountFollowing == 1) {
                    categoryvideo.clear()
                }

                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    frame_following.visibility = View.VISIBLE
                    tv_nodata_following.visibility = View.GONE
                    btnSignUp.visibility = View.GONE

                    if (jsonObject.has("data")) {
                        val jsonobj1 = jsonObject.getJSONObject("data")
                        val jsonArray = jsonobj1.getJSONArray("userFollowerWiseList")
                        val array = Gson().fromJson<ArrayList<ForYouResponse>>(
                            jsonArray.toString(),
                            object : TypeToken<ArrayList<ForYouResponse>>() {}.type
                        )

                        if (pageCountFollowing == 1) {
                            categoryvideo.clear()
                            likeVideoHashFollowing.clear()
                            hasLikedMapFollowing.clear()
                            commentCountMapFollowing.clear()

                            if (adapter1 != null) {
                                adapter1!!.removeAllFragment()
                            }
                            adapter1 = null
                        }

                        if (array.isNotEmpty()) {
                            if (array.size < jsonObject.getInt("pagination_count")) {
                                isDataFinished = true
                            }
                            categoryvideo.addAll(array)
                            for (i in categoryvideo.indices) {
                                likeVideoHashFollowing[categoryvideo[i].post_id.toString()] =
                                    categoryvideo[i].post_like_count.toString()
                                hasLikedMapFollowing[categoryvideo[i].post_id.toString()] =
                                    categoryvideo[i].has_liked.toString()
                                commentCountMapFollowing[categoryvideo[i].post_id.toString()] =
                                    categoryvideo[i].post_comment_count.toString()
                            }

                            if (pageCountFollowing == 1) {
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
                    Logger.print("Following category video=====================" + categoryvideo.size)
                } else {
                    isDataFinished = true
                    if (code == 0) {
                        if (pageCountFollowing == 1) {
                            frame_following.visibility = View.GONE
                            Logger.print("Following category video code 0=====================" + categoryvideo.size)

                            if (SharedPreferenceUtils.getInstance(activity)
                                    .getBoolanValue(Constants.IS_LOGIN, false)
                            ) {
                                tv_nodata_following.visibility = View.VISIBLE
                                btnSignUp.visibility = View.GONE
                                tv_nodata_following.text =
                                    resources.getString(R.string.no_data_found)
                                Logger.print("Following category video code isLogin=====================" + categoryvideo.size)

                            } else {
                                tv_nodata_following.visibility = View.VISIBLE
                                btnSignUp.visibility = View.VISIBLE
                                Logger.print("Following category video code else isLogin=====================" + categoryvideo.size)

                            }
                        }
                    }
                }
            }
        })
    }

    override fun onVideoStartPlaying(position: Int) {
//        Log.i("FollowingFragment", "Position ${lastPosition + 1} Array size ${categoryvideo.size}")
        /*if(previousPosition > -1) {
            val previousFragment = adapter1!!.fragments[previousPosition]
            previousFragment.stopPlayer()
        }*/
    }

    override fun onVideoViewCountUpdate(position: Int, videoViewCount: String) {
        categoryvideo[position].isViewed = true
    }

    override fun onInitNextCall(currentPos: Int) {
        /*if ((currentPos + 1) < categoryvideo.size && (currentPos + 1) < (lastPosition + 6)) {
            activity.preDownload(categoryvideo[currentPos + 1].post_video,
                null, currentPos + 1, this@FollowingFragment)
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
                if ((commentBottomSheet == null || !commentBottomSheet?.isVisible!!) && (spamBottomSheet == null || !spamBottomSheet?.isVisible!!)) {
                    Logger.print("double tap for you fragment===============================")
                    imgLikeDoubleTap.visibility = View.VISIBLE
                    Methods.animateHeart(imgLikeDoubleTap)
                    if (categoryvideo[currentPosition].has_liked == 0) {
                        likeCount = 1
                        like_image.setImageResource(R.drawable.like_active)
                        val likeCounts =
                            Integer.parseInt(categoryvideo[currentPosition].post_like_count!!) + 1
                        Logger.print("like Counts!!!!!!!!!!!!!!!!!!!!!!!!!!$likeCounts")
                        Methods.showLikeCounts(likeCounts.toString(), like_txt)
                        likeUnlikeVideo(categoryvideo[currentPosition].post_id, likeCount)
                        categoryvideo[currentPosition].post_like_count = "$likeCounts"
                        categoryvideo[currentPosition].has_liked = 1
                    }
                }
            }
        }
    }

    companion object {
        var commentBottomSheet: CommentBottomSheet? = null
        var spamBottomSheet: SpamBottomSheet? = null
    }

    override fun onExtractFinished() {
        activity.hideProgressDialog()
        Methods.deleteFilesFromDirectory(Constants.EXTRACT_PATH)
        var nameFile = Methods.fileFromDir(Constants.Audio_PATH)
        Logger.print("call following")
        if (nameFile!!.contains(".mp4")) {
            nameFile = nameFile.replace(".mp4", "")
        }

        val isLogin =
            SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_LOGIN, false)
        if (isLogin) {
            val intent = Intent(activity, CameraVideoRecording::class.java)
            intent.putExtra("ISAUDIOMIX", true)
            intent.putExtra("audioFileNameFinal", nameFile)
            intent.putExtra("extractAudio", true)
            activity.startActivity(intent)
        } else {
            val intent = Intent(getActivity(), LoginActivity::class.java)
            intent.putExtra("screen_name", "startPost")
            startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
        }
    }

    override fun onCommentAdded(postId: Int, tvCommnetCountBottomSheet: AppCompatTextView) {
        if (lastPosition != -1 && postId == categoryvideo[lastPosition].post_id) {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "AddCommentForYou")
            bundle.putString(
                FirebaseAnalytics.Param.ITEM_ID,
                categoryvideo[lastPosition].post_id.toString()
            )
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

            val commentsCount =
                Integer.parseInt(categoryvideo[lastPosition].post_comment_count!!) + 1
            postCommentCount = "$commentsCount"
            categoryvideo[lastPosition].post_comment_count = "$commentsCount"
            adapter1?.notifyItemChanged(lastPosition)
            if (commentsCount.toString() != "") {
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
                SpamBottomSheet.newInstance(userId, commentId, postId!!, this@FollowingFragment)
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