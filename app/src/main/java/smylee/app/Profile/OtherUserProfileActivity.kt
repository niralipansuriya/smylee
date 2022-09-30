package smylee.app.Profile

import android.app.Activity
import android.content.Intent
import android.content.res.TypedArray
import android.os.Bundle
import android.os.Handler
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_other_user_profile.*
import org.json.JSONObject
import smylee.app.FollowUnfollowUser.FollowActivity
import smylee.app.R
import smylee.app.dialog.CommonAlertDialog
import smylee.app.engine.GridLayoutSpacing
import smylee.app.login.LoginActivity
import smylee.app.model.ForYouResponse
import smylee.app.playvideo.VideoDetailActivity
import smylee.app.ui.Activity.DisplayUrlActivity
import smylee.app.ui.Activity.FullScreenImage
import smylee.app.utils.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.math.abs


class OtherUserProfileActivity : AppCompatActivity() {

    private var otherUserId: String = ""
    private var pageCount = 1
    lateinit var viewModel: ProfileViewModel
    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var llm: LinearLayoutManager? = null
    private var currentVisibleCount: Int = 0
    var cmpagination: CustomPaginationDialog? = null

    //private var list = ArrayList<ProfileResponse>()
    private var list = ArrayList<ForYouResponse>()
    private var otherUserProfileAdapter: OtherUserProfileAdapter? = null

    private var totalItemCount: Int = 0

    private var lastVisibleItem: Int = 0

    private var visibleThreshold = 10
    private var appBarExpanded = true

    //    private var collapsedMenu: Menu? = null
    private var profilePic = ""
    private var FACEBOOK_URL = ""
    private var INSTAGRAM_URL = ""
    private var YOUTUBE_URL = ""
    private var fullImagePic = ""
    private var isSearch: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_user_profile)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        setSupportActionBar(toolbar)

        if (intent != null) {
            otherUserId = intent.getStringExtra("OTHER_USER_ID")!!
            isSearch = intent.getBooleanExtra("is_search", false)
            Logger.print("OTHER_USER_ID=======$otherUserId")
        }

        avatar.setOnClickListener {
            if (isSearch) {
                if (supportFragmentManager.backStackEntryCount > 0)
                    supportFragmentManager.popBackStack()
                else
                    onBackPressed()
            } else {
                onBackPressed()
            }
        }

        ivFacebook.setOnClickListener {
            val intent = Intent(this, DisplayUrlActivity::class.java)
            intent.putExtra("url", FACEBOOK_URL)
            startActivity(intent)
        }
        ivInsagram.setOnClickListener {
            val intent = Intent(this, DisplayUrlActivity::class.java)
            intent.putExtra("url", INSTAGRAM_URL)
            startActivity(intent)
        }
        ivYouTube.setOnClickListener {
            val intent = Intent(this, DisplayUrlActivity::class.java)
            intent.putExtra("url", YOUTUBE_URL)
            startActivity(intent)
        }
        ivChoose.setOnClickListener {
            blockUser()
        }

        val gridLayoutSpacing = GridLayoutSpacing()
        rv_post_profile.addItemDecoration(gridLayoutSpacing)

        ll_other_following.setOnClickListener {
            // val intent = Intent(this, UnFollowActivity::class.java)
            val intent = Intent(this, UnfollowActivityOtherProfile::class.java)
            intent.putExtra("OTHER_USER_ID", otherUserId)
            startActivity(intent)
        }

        ll_other_follower.setOnClickListener {
            val intent = Intent(this, FollowActivity::class.java)
            intent.putExtra("OTHER_USER_ID", otherUserId)
            intent.putExtra("isFromMyProfile", 0)
            startActivity(intent)
        }

        btn_follow.setOnClickListener {
            val isLogin = SharedPreferenceUtils.getInstance(this)
                .getBoolanValue(Constants.IS_LOGIN, false)
            if (isLogin) {
                btn_follow.visibility = View.GONE
                btn_unfollow.visibility = View.VISIBLE
                followUser()
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("screen_name", "otherprofile")
                startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
            }
        }

        btn_unfollow.setOnClickListener {
            val isLogin = SharedPreferenceUtils.getInstance(this)
                .getBoolanValue(Constants.IS_LOGIN, false)
            if (isLogin) {
                btn_follow.visibility = View.VISIBLE
                btn_unfollow.visibility = View.GONE
                unFollowUser()
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("screen_name", "otherprofile")
                startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
            }
        }

        htab_appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            //  Vertical offset == 0 indicates appBar is fully  expanded.
            if (abs(verticalOffset) > 200) {
                appBarExpanded = false
                invalidateOptionsMenu()
            } else {
                appBarExpanded = true
                invalidateOptionsMenu()
            }
        })

        iv_pic.setOnClickListener {
            if (!fullImagePic.contentEquals("") && !fullImagePic.contentEquals("null")) {
                val intent = Intent(this, FullScreenImage::class.java)
                intent.putExtra("imageUrl", fullImagePic)
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        if (isSearch) {
            if (supportFragmentManager.backStackEntryCount > 0)
                supportFragmentManager.popBackStack()
            else
                super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        pageCount = 1
        retrieveOtherUserProfile(true, true)
        applyPagination()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.LOGIN_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            retrieveOtherUserProfile(true, true)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = this.menuInflater
        inflater.inflate(R.menu.block_user, menu)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // status bar height
            var statusBarHeight = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                statusBarHeight = resources.getDimensionPixelSize(resourceId)
            }

            // action bar height
            val actionBarHeight: Int
            val styledAttributes: TypedArray = theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
            actionBarHeight = styledAttributes.getDimension(0, 0F).toInt()
            styledAttributes.recycle()
            val layoutParams = llOtherProfile.layoutParams as CollapsingToolbarLayout.LayoutParams
            layoutParams.topMargin = statusBarHeight + (actionBarHeight / 2)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.block_user) {
            return false
        }
        return true
    }

    private fun unFollowUser() {
        val isFollow = "0"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = otherUserId

        viewModel.FollowUnfollow(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("FOLLOWUNFOLLOWUSER Response : $it")
                SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                val jsonObject = JSONObject(it.toString())
                if (jsonObject["code"] == 1) {
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)

                    pageCount = 1
                    retrieveOtherUserProfile(false, false)
                }
            }
        })
    }

    private fun followUser() {
        val isFollow = "1"
        val hashMap = HashMap<String, String>()
        hashMap["is_follow"] = isFollow
        hashMap["other_user_id"] = otherUserId

        viewModel.FollowUnfollow(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("FOLLOWUNFOLLOWUSER Response : $it")
                SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                val jsonObject = JSONObject(it.toString())
                if (jsonObject["code"] == 1) {
                    pageCount = 1
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_FOR_You, true)
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)
                    retrieveOtherUserProfile(false, false)
                }
            }
        })
    }

    fun retrieveOtherUserProfile(isProgress: Boolean, IsShowButon: Boolean) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["other_user_id"] = otherUserId

        val isProgressShow: Boolean = if (isProgress) {
            pageCount == 1
        } else {
            false
        }

        viewModel.observeGetProfile(this, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                removeLoaderView()
                Logger.print("get other user profile Response ::::::::: $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")

                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val jsonObj1 = jsonObject.getJSONObject("data")
                        val jsonObj2 = jsonObj1.getJSONObject("user_data")
                        val jsonArray = jsonObj1.getJSONArray("user_post")

                        if (pageCount == 1) {
                            val userName = jsonObj2.getString("first_name")
                            if (jsonObj2.has("mark_as_verified_badge")) {
                                val mark_as_verified_badge =
                                    jsonObj2.getInt("mark_as_verified_badge")

                                if (mark_as_verified_badge != null && mark_as_verified_badge == 1) {
                                    ivVerified.visibility = View.VISIBLE
                                } else {
                                    ivVerified.visibility = View.GONE

                                }

                            }
                            if (jsonObj2.has("facebook_url")) {
                                FACEBOOK_URL = jsonObj2.getString("facebook_url")
                                if (FACEBOOK_URL != "" && !FACEBOOK_URL.contentEquals("null")) {
                                    ivFacebook.visibility = View.VISIBLE
                                }
                                Logger.print("FACEBOOK_URL ================$FACEBOOK_URL")
                            }

                            if (jsonObj2.has("instagram_url")) {
                                INSTAGRAM_URL = jsonObj2.getString("instagram_url")
                                if (!INSTAGRAM_URL.contentEquals("null") && INSTAGRAM_URL != "") {
                                    ivInsagram.visibility = View.VISIBLE
                                }
                                Logger.print("INSTAGRAM_URL================$INSTAGRAM_URL")
                            }

                            if (jsonObj2.has("youtube_url")) {
                                YOUTUBE_URL = jsonObj2.getString("youtube_url")
                                if (!YOUTUBE_URL.contentEquals("null") && YOUTUBE_URL != "") {
                                    ivYouTube.visibility = View.VISIBLE
                                }
                                Logger.print("YOUTUBE_URL================$YOUTUBE_URL")
                            }
                            profilePic = jsonObj2.getString("profile_pic")
                            fullImagePic = jsonObj2.getString("profile_pic_compres")
                            val following = jsonObj2.getString("no_of_followings")
                            val follower = jsonObj2.getString("no_of_followers")
                            val post = jsonObj2.getString("no_of_post")
                            val isFollowing = jsonObj2.getString("is_following")
                            Logger.print("post of other user================$post")

                            if (IsShowButon) {
                                if (!isFollowing.contentEquals("null") && isFollowing.contentEquals("0")) {
                                    btn_follow.visibility = View.VISIBLE
                                    btn_unfollow.visibility = View.GONE
                                } else if (!isFollowing.contentEquals("null") && isFollowing.contentEquals("1")) {
                                    btn_unfollow.visibility = View.VISIBLE
                                    btn_follow.visibility = View.GONE
                                } else {
                                    btn_unfollow.visibility = View.GONE
                                    btn_follow.visibility = View.GONE
                                }
                            }

                            if (!fullImagePic.contentEquals("null") && !fullImagePic.contentEquals("")) {
                                SharedPreferenceUtils.getInstance(this).setValue(fullImagePic, Constants.CROPED_PROFILE_PIC)
                            }
                            if (!profilePic.contentEquals("null")) {
                                Glide.with(this)
                                    .load(profilePic)
                                    .error(R.drawable.userpicfinal)
                                    .into(iv_pic)
                            }

                            if (!userName.contentEquals("null")) {
                                page_title.text = userName + ""
                            }
                            if (!post.contentEquals("null")) {
                                Methods.showViewCounts(post, tv_post_other_profile)
                            }
                            if (!userName.contentEquals("") && !userName.contentEquals("null")) {
                                tv_name.text = userName
                            }
                            if (!follower.contentEquals("") && !follower.contentEquals("null")) {
                                Methods.showViewCounts(follower, txt_follower_other_profile)
                            }
                            if (!following.contentEquals("") && !following.contentEquals("null")) {
                                Methods.showViewCounts(following, txt_following_other_profile)
                            }
                        }

                        val array = Gson().fromJson<ArrayList<ForYouResponse>>(jsonArray.toString(),
                            object : TypeToken<ArrayList<ForYouResponse>>() {}.type)

                        if (pageCount == 1) {
                            list.clear()

                        }

                        if (array.isNotEmpty()) {
                            list.addAll(array)

                        } else {
                            isDataFinished = true
                        }

                        if (list.size > 0) {
                            no_data_found.visibility = View.GONE
                            rv_post_profile.visibility = View.VISIBLE
                            isLoading = false
                            setAdapter()
                        } else {
                            rv_post_profile.visibility = View.GONE
                            no_data_found.visibility = View.VISIBLE
                        }
                        Logger.print("list profile post===============" + list.size)
                    }
                }
            }
        })
    }

    private fun blockUserAPICall() {
        val isBlock = "1"
        val hashMap = HashMap<String, String>()
        hashMap["is_block"] = isBlock
        hashMap["other_user_id"] = otherUserId
        viewModel.blockunblockuser(this, hashMap, true).observe(this, Observer {
            if (it != null) {
                Logger.print("Block Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")

                if (code == 1) {
                    Utils.showToastMessage(this, jsonObject.getString("message"))
                    SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                    SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_CHANGE_LIKE_OR_COMMENT_OR_FOLLOW, true)
                    SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_ANYTHING_CHANGE_DISCOVER, true)
                    SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_ANYTHING_CHANGE_TRENDING, true)
                    SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)
                    SharedPreferenceUtils.getInstance(this).setValue(Constants.IS_ANYTHING_CHANGE_FORYOU_FOR_BLOCK, true)
                    onBackPressed()
                    finish()
                } else if (jsonObject["code"] == 0) {
                    Utils.showToastMessage(this, jsonObject.getString("message"))
                }
            }
        })
    }

    private fun applyPagination() {
        rv_post_profile.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            addViewLoadingView()

                            if (dy > 0) {
                                addLoadingView()
                            }
                            Handler().postDelayed({retrieveOtherUserProfile(true, true)}, Constants.DELAY)
                        }
                    } else {
                        println("........finished data..........")
                    }
                }
            }
        })
    }

    private fun removeLoaderView() {
        if (cmpagination != null && cmpagination!!.isShowing) {
            cmpagination!!.hide()
        }
    }

    private fun addLoadingView() {
        if (cmpagination == null)
            cmpagination = CustomPaginationDialog(this)
        cmpagination!!.show(false)
    }

    private fun addViewLoadingView() {
        if (list.size > 0) {
            rv_post_profile.post {
                if (otherUserProfileAdapter != null) {
                    otherUserProfileAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun setAdapter() {
        if (otherUserProfileAdapter == null) {
            llm = GridLayoutManager(this, 3)
            rv_post_profile.layoutManager = llm
            otherUserProfileAdapter =
                OtherUserProfileAdapter(this, list, object : OtherUserProfileAdapter.ManageClick {
                    override fun managePlayVideoClick(position: Int?) {
                        val intent =
                            Intent(this@OtherUserProfileActivity, VideoDetailActivity::class.java)
                        intent.putExtra("position", position)
                        intent.putExtra("cat_ID", "")
                        intent.putExtra("screen", "otherProfile")
                        intent.putExtra("hashtag", "")
                        intent.putExtra("responseData", list)
                        intent.putExtra("pageNo", pageCount)
                        intent.putExtra("searchTxt", "")
                        intent.putExtra("userIDApi", otherUserId)

                        startActivity(intent)
                    }

                })
            rv_post_profile.adapter = otherUserProfileAdapter
        } else {
            otherUserProfileAdapter?.notifyDataSetChanged()
        }
    }

    private fun blockUser() {
        val isLogin = SharedPreferenceUtils.getInstance(this).getBoolanValue(Constants.IS_LOGIN, false)
        if (isLogin) {
            val alertDialog = object :
                CommonAlertDialog(this, theme = android.R.style.Theme_Translucent_NoTitleBar) {
                override fun okClicked() {
                    blockUserAPICall()
                }

                override fun cancelClicked() {}
            }
            alertDialog.initDialog(resources.getString(R.string.Block), resources.getString(R.string.block))
            alertDialog.setCancelable(true)
            alertDialog.show()
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("screen_name", "otherprofile")
            startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
        }
    }
}