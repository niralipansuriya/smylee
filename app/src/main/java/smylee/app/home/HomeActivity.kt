package smylee.app.home

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_home.*
import smylee.app.BuildConfig
import smylee.app.Intrest.ChooseYourLanguageIntrest
import smylee.app.Profile.MyProfileFragment
import smylee.app.R
import smylee.app.dialog.CommonAlertDialog
import smylee.app.exploreSearch.NewExploreFragment
import smylee.app.login.LoginActivity
import smylee.app.notification.NotificationFragement
import smylee.app.setting.SettingActivity
import smylee.app.ui.Activity.CameraVideoRecording
import smylee.app.ui.Activity.PreviewActivity
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.util.*
import kotlin.collections.HashMap


class HomeActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var viewModel: HomeViewModel
    private var fcmToken: String = ""
    private var deviceId: String = ""
    private var deviceManufacture: String = ""
    var isAttachFragmentAgain = false
    private var layoutParams: RelativeLayout.LayoutParams? = null
    private lateinit var handler: Handler
    private var height: Int = 0
    private var isLogin: Boolean = false
    private var isOnResumeFirstTime: Boolean = true
    private var screenName: String = ""
    private var notificationFragment: NotificationFragement? = null
    private var myProfileFragment: MyProfileFragment? = null
    private var homeFollowingForYou: HomeFollowingForYou? = null
    private var exploreFragment: NewExploreFragment? = null
    var currentTabIndex = 0
    private var postId = ""
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var isCallLogin = false
    private var mAppUpdateManager: AppUpdateManager? = null
    private val RC_APP_UPDATE = 11
    var shareUri: Uri? = null

    companion object {
        var activity: BaseActivity? = null
        var tabIndex = 0
        var homeActivity: HomeActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        activity = this
        homeActivity = this
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
//        Log.d("TopicHomeActivity", list.toString())
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        layoutParams = container.layoutParams as RelativeLayout.LayoutParams

        handler = Handler()
        if (intent != null) {
            if (intent.hasExtra("screenName")) {
                screenName = intent.getStringExtra("screenName")!!
            }
            if (intent.hasExtra("postId")) {
                postId = intent.getStringExtra("postId")!!
            }
            // Logger.print("postId!!!!!$postId")
            val bundle = intent.extras
            if (bundle != null && bundle.containsKey("isforPushNotification")) {
                Logger.print(
                    "post id from notification onCreate =========${bundle.getString("post_id")
                        .toString()}"
                )

                if (bundle.getString("post_id") != null) {
                    postId = bundle.getString("post_id").toString()

                }
            }


        }
        /* if (intent.extras != null) {
             Logger.print("home activity get extras========${intent.extras!!["post_id"].toString()}")
             for (key in intent.extras!!.keySet()) {
                 val value = intent.extras!![key]
                 Log.d("MainActivity: ", "Key: $key Value: $value")
             }
         }*/
        Logger.print("screenName home screen============================$screenName")

        when {
            intent?.action == Intent.ACTION_SEND -> {
                // Logger.print("intent.type =============== ${intent.type}")
                Logger.print("ACTION_SEND===============")
                // if ("video/*" == intent.type) {
                if (intent?.type!!.startsWith("video")) {
                    Logger.print("ACTION_SEND video===============")

                    (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                        SharedPreferenceUtils.getInstance(this)
                            .setValue(Constants.SHARE_URI, "")
                        shareUri = it
                        Logger.print("get data from other APPS =========${it.path}")
                        val path = FileUtils.getPathFromUri(this, it)
                        //fileName(path)

                        if (SharedPreferenceUtils.getInstance(this)
                                .getStringValue(Constants.LANGUAGE_PREF, "") == ""
                        ) {

                            val intent = Intent(this, ChooseYourLanguageIntrest::class.java)
                            intent.putExtra("is_show", false)
                            intent.putExtra("is_visible", "0")
                            intent.putExtra("screen_name", "preview")
                            intent.putExtra("shareUri", shareUri)
                            startActivity(intent)
                        } else if (SharedPreferenceUtils.getInstance(this)
                                .getBoolanValue(Constants.IS_LOGIN, false)
                        ) {
                            val intent = Intent(this, PreviewActivity::class.java)
                            intent.putExtra("shareUri", shareUri)
                            intent.putExtra("IS_AUDIO_CAMERA", false)
                            intent.putExtra("IS_MERGE", false)
                            intent.putExtra("IS_CAMERA_PREVIEW", false)
                            intent.putExtra("extractAudio", false)
                            intent.putExtra("videoRotation", false)
                            intent.putExtra("audioFile", "")
                            startActivityForResult(intent, 1010)
                        } else {
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.putExtra("screen_name", "preview")
                            intent.putExtra("shareUri", it)

                            startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
                        }
                    }
                }

            }

            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }

        val intentShareFormGallery = IntentFilter("shareFormGallery")
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(shareFromGallery, intentShareFormGallery)


        when {
            /*screenName.contentEquals("preview")->
            {
                //if (shareUri!=null && shareUri!!.path!="")
                if ( SharedPreferenceUtils.getInstance(this).setValue(Constants.SHARE_URI,"")!=null)
                {
                    val intent = Intent(this, PreviewActivity::class.java)
                    intent.putExtra("shareUri", shareUri)
                    intent.putExtra("IS_AUDIO_CAMERA", false)
                    intent.putExtra("IS_MERGE", false)
                    intent.putExtra("IS_CAMERA_PREVIEW", false)
                    intent.putExtra("extractAudio", false)
                    intent.putExtra("videoRotation", false)
                    intent.putExtra("audioFile", "")
                    startActivity(intent)
                }

            }*/
            screenName.contentEquals("notification") -> {
                currentTabIndex = 3
                if (notificationFragment == null) {
                    notificationFragment = NotificationFragement.newInstance()
                }
                loadFragment(notificationFragment)
                bottomNavigation.menu.getItem(3).isChecked = true
                bottomNavigation.menu.getItem(3).setIcon(R.drawable.notification_selector)
            }
            screenName.contentEquals("profile") -> {
                currentTabIndex = 4
                if (myProfileFragment == null) {
                    myProfileFragment = MyProfileFragment.newInstance()
                }
                loadFragment(myProfileFragment)
                bottomNavigation.menu.getItem(4).isChecked = true
                bottomNavigation.menu.getItem(4).setIcon(R.drawable.profile_selector)
            }
            screenName.contentEquals("profile+settings") -> {
                currentTabIndex = 4
                if (myProfileFragment == null) {
                    myProfileFragment = MyProfileFragment.newInstance()
                }
                loadFragment(myProfileFragment)
                bottomNavigation.menu.getItem(4).isChecked = true
                bottomNavigation.menu.getItem(4).setIcon(R.drawable.profile_selector)
                setTabUI(false)
                showTabBar()
                val refresh = Intent(this, SettingActivity::class.java)
                startActivity(refresh)
            }
            screenName.contentEquals("startPost") -> {
                currentTabIndex = 2
                isLogin = SharedPreferenceUtils.getInstance(this)
                    .getBoolanValue(Constants.IS_LOGIN, false)
                if (isLogin) {
                    val intent = Intent(this@HomeActivity, CameraVideoRecording::class.java)
                    intent.putExtra("ISAUDIOMIX", false)
                    intent.putExtra("extractAudio", false)
                    intent.putExtra("audioFileNameFinal", "")
                    startActivityForResult(intent, 1010)
                } else {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("screen_name", "startPost")
                    startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
                }
            }
            screenName.contentEquals("explore") -> {
                currentTabIndex = 1
                if (exploreFragment == null) {
                    // exploreFragment = ExploreFragment()
                    exploreFragment = NewExploreFragment()
                }
                loadFragment(exploreFragment)
                bottomNavigation.menu.getItem(1).isChecked = true
                bottomNavigation.menu.getItem(1).setIcon(R.drawable.explore_selector)
            }
            else -> {
                currentTabIndex = 0
                if (homeFollowingForYou == null) {
                    homeFollowingForYou = HomeFollowingForYou.newInstance()
                }
                homeFollowingForYou?.setPostIdLink(postId)
                homeFollowingForYou?.setTabHeight(height)
                loadFragment(homeFollowingForYou)
                setTabUI(true)
            }
        }

        deviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        if (!deviceId.contentEquals("")) {
            SharedPreferenceUtils.getInstance(this).setValue(Constants.DEVICE_ID, deviceId)
        }
        Logger.print("DEVICE_ID=============", deviceId)
        bottomNavigation.setOnNavigationItemSelectedListener(this)
        bottomNavigation.itemIconTintList = null
        deviceManufacture = Build.MANUFACTURER

        bottomNavigation.post {
            height = bottomNavigation.measuredHeight
            if (homeFollowingForYou != null) {
                homeFollowingForYou?.setTabHeight(height)
            }
            Log.i("HomeActivity", "Bottom Navigation bar height $height")
        }

        SharedPreferenceUtils.getInstance(this).setValue(Constants.MANUFACTURER, deviceManufacture)
        Logger.print("DEVICE_MANUFACTURER=================", deviceManufacture)
        Log.i("HomeActivity", "onCreate Home")
        isLogin = SharedPreferenceUtils.getInstance(this).getBoolanValue(Constants.IS_LOGIN, false)

        imgPost.setOnClickListener {
            isLogin =
                SharedPreferenceUtils.getInstance(this).getBoolanValue(Constants.IS_LOGIN, false)
                val intent = Intent(this@HomeActivity, CameraVideoRecording::class.java)
                intent.putExtra("ISAUDIOMIX", false)
                intent.putExtra("extractAudio", false)
                intent.putExtra("audioFileNameFinal", "")
                startActivityForResult(intent, 1010)

            /*if (isLogin) {
                val intent = Intent(this@HomeActivity, CameraVideoRecording::class.java)
                intent.putExtra("ISAUDIOMIX", false)
                intent.putExtra("extractAudio", false)
                intent.putExtra("audioFileNameFinal", "")
                startActivityForResult(intent, 1010)
            } else {
                if (currentTabIndex == 0) {
                    homeFollowingForYou?.onStopAllVideo()
                }
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("screen_name", "startPost")
                startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
            }*/
        }

        fcmToken =
            SharedPreferenceUtils.getInstance(this).getStringValue(Constants.FIREBASE_TOKEN, "")!!
        val isTokenChanged = SharedPreferenceUtils.getInstance(this)
            .getBoolanValue(Constants.FIREBASE_TOKEN_CHANGED, true)

        //if (!fcmToken.contentEquals("") ) {
        if (!fcmToken.contentEquals("") && isTokenChanged) {
            SharedPreferenceUtils.getInstance(this)
                .setValue(Constants.FIREBASE_TOKEN_CHANGED, false)
            sendFcmToken(fcmToken)
        }

        //in app review rating
        if (!SharedPreferenceUtils.getInstance(this)
                .getBoolanValue(Constants.FIRST_INSTALL, false)
        ) {
            SharedPreferenceUtils.getInstance(this).setValue(Constants.FIRST_INSTALL, true)
            SharedPreferenceUtils.getInstance(this)
                .setValue(Constants.CURRENT_DATE, System.currentTimeMillis())
        }
        val sharedDate =
            SharedPreferenceUtils.getInstance(this).getLongValue(Constants.CURRENT_DATE, 0L)
        val cal = Calendar.getInstance()
        cal.timeInMillis = sharedDate
        val date = incrementDateByTwoDays(cal.time)
        Logger.print("date !!!!!!!!!!!!!!!!!${date}")

        if (Calendar.getInstance().time >= date) {
            Logger.print("Calendar.getInstance().time!!!!!!!!")
            showRatingReviewDialog()
        }
        val intentFilter = IntentFilter("uploadingMessage")
        val intentFilterForErrorMessage = IntentFilter("errorMessage")

        LocalBroadcastManager.getInstance(this).registerReceiver(uploadingMessage, intentFilter)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(errorMessage, intentFilterForErrorMessage)

        updateApp()
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Logger.print("onNewIntent")
        val bundle = intent?.extras
        Logger.print("onNewIntent post id ==========${bundle?.getString("post_id")}")

    }

    fun updateAppDialog() {

        val alertDialog =
            object : CommonAlertDialog(this, theme = android.R.style.Theme_Translucent_NoTitleBar) {
                override fun okClicked() {
                    try {
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
                                )
                            )
                            finish()
                        } catch (anfe: ActivityNotFoundException) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                                )
                            )
                            finish()
                        }
                    } catch (e: java.lang.Exception) {
                        //	System.out.println("");
                    }
                }

                override fun cancelClicked() {
                }
            }

        alertDialog.initDialog(
            resources.getString(R.string.update),
            resources.getString(R.string.update_app)
        )

        alertDialog.setCancelable(true)
        alertDialog.show()


    }

    private fun incrementDateByTwoDays(date: Date?): Date? {
        val c: Calendar = Calendar.getInstance()
        c.time = date!!
        c.add(Calendar.DATE, 2)
        return c.time
    }

    var installStateUpdatedListener: InstallStateUpdatedListener =
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(state: InstallState) {
                if (state.installStatus() === InstallStatus.DOWNLOADED) {
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                } else if (state.installStatus() === InstallStatus.INSTALLED) {
                    if (mAppUpdateManager != null) {
                        mAppUpdateManager!!.unregisterListener(this)
                    }
                } else {
                    Logger.print("state================")

                }
            }
        }

    fun updateApp() {
        mAppUpdateManager = AppUpdateManagerFactory.create(this)

        mAppUpdateManager!!.registerListener(installStateUpdatedListener)

        mAppUpdateManager!!.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                        AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/
                    )
                ) {
                    try {
                        mAppUpdateManager!!.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/,
                            this,
                            RC_APP_UPDATE
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                    }
                } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    Logger.print("DOWNLOADED ==================${appUpdateInfo.installStatus()}")
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                } else {

                    Logger.print("App update status=============${appUpdateInfo.installStatus()}")
                }
            }
        mAppUpdateManager!!.appUpdateInfo.addOnFailureListener {
            it.printStackTrace()

        }

    }

    fun hideTabBar() {
        coordinatorLayout.visibility = View.GONE
        imgPost.visibility = View.GONE
        Logger.print("hideTabBar======================")
    }

    fun showTabBar() {
        coordinatorLayout.visibility = View.VISIBLE
        imgPost.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(uploadingMessage)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(errorMessage)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(shareFromGallery)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        Logger.print("HomeActivity onResume !!!!!!!!!!!!!!!!!!")
        Log.i("HomeActivity", "onResume $isAttachFragmentAgain")
        if (isOnResumeFirstTime) {
            isOnResumeFirstTime = false
        } else {
            if (currentTabIndex == 0) {
                homeFollowingForYou?.resumeVideoPlay(isCallLogin)
                isCallLogin = false
            }
        }
    }


    override fun onBackPressed() {
        Logger.print("call on back pressed *************************" + bottomNavigation.selectedItemId)
        Logger.print(" NewExploreFragment.isSearchAvailable *************************" + NewExploreFragment.isSearchAvailable)
        if (bottomNavigation.menu.getItem(0).isChecked) {
            if (ForYouFragmentHome.spamBottomSheet != null && ForYouFragmentHome.spamBottomSheet!!.isVisible) {
                Utils.hideKeyboard(this@HomeActivity)
                ForYouFragmentHome.spamBottomSheet!!.dismiss()
            } else if (FollowingFragment.spamBottomSheet != null && FollowingFragment.spamBottomSheet!!.isVisible) {
                Utils.hideKeyboard(this@HomeActivity)
                FollowingFragment.spamBottomSheet!!.dismiss()
            } else if (ForYouFragmentHome.commentBottomSheet != null && ForYouFragmentHome.commentBottomSheet?.isVisible!!) {
                Utils.hideKeyboard(this@HomeActivity)
                ForYouFragmentHome.commentBottomSheet?.dismiss()
            } else if (FollowingFragment.commentBottomSheet != null && FollowingFragment.commentBottomSheet?.isVisible!!) {
                Utils.hideKeyboard(this@HomeActivity)
                FollowingFragment.commentBottomSheet?.dismiss()
            } else {
                this@HomeActivity.finish()
            }
            Logger.print("call for home Fragment *************************")
        } else if (bottomNavigation.menu.getItem(1).isChecked && NewExploreFragment.isSearchAvailable) {
            if (exploreFragment != null) {
                Logger.print("exploreFragment not null==================")
                exploreFragment!!.clearSearch()
            }

        } else {
            this@HomeActivity.finish()
        }
    }

    fun setTabUI(isForHome: Boolean) {
        if (isForHome) {
            //  bottomNavigation.menu.getItem(0).setIcon(R.drawable.home_selected_transparent)
            bottomNavigation.menu.getItem(0).setIcon(R.drawable.home_transperent)
            bottomNavigation.menu.getItem(1).setIcon(R.drawable.search_transparent)
            imgPost.setImageResource(R.drawable.create_post_transparent)
            bottomNavigation.menu.getItem(3).setIcon(R.drawable.notification_transparent)
            bottomNavigation.menu.getItem(4).setIcon(R.drawable.profile_transparent)
            bottomNavigation.setBackgroundColor(Color.TRANSPARENT)
            handler.postDelayed({
                layoutParams?.removeRule(RelativeLayout.ABOVE)
                container.layoutParams = layoutParams
            }, 50)
        } else {
            layoutParams?.addRule(RelativeLayout.ABOVE, R.id.coordinatorLayout)
            container.layoutParams = layoutParams
            bottomNavigation.menu.getItem(0).setIcon(R.drawable.nav_selector)
            bottomNavigation.menu.getItem(1).setIcon(R.drawable.explore_selector)
            imgPost.setImageResource(R.drawable.create_post_new)
            bottomNavigation.menu.getItem(3).setIcon(R.drawable.notification_selector)
            bottomNavigation.menu.getItem(4).setIcon(R.drawable.profile_selector)
            bottomNavigation.setBackgroundColor(Color.WHITE)
        }
        Logger.print("setTabUI==========================================")
    }

    fun setSelected() {
        bottomNavigation.menu.getItem(4).isChecked = true
        bottomNavigation.menu.getItem(4).setIcon(R.drawable.profile_selector)
        Logger.print("setSelected===================================")
    }

    private fun sendFcmToken(FCM_TOKEN: String) {
        val hashMap = HashMap<String, String>()
        hashMap["device_token"] = FCM_TOKEN
        viewModel.SendFcmToken(this, hashMap, false).observe(this, Observer {
            if (it != null) {
                Logger.print("SETUSERDEVICERELATION Response : $it")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_APP_UPDATE) {
            if (resultCode == RESULT_OK) {
                Logger.print("onActivityResult: app download RESULT_OK");
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Logger.print("onActivityResult: app download RESULT_CANCELED");

            } else if (resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
                Logger.print("onActivityResult: app download RESULT_IN_APP_UPDATE_FAILED");

            }
        }
        if (requestCode == 1010) {
            Log.i("HomeActivity", "onActivityResult Home")
            /*if (currentTabIndex == 4) {
                myProfileFragment?.onResume()
            }*/
        }
    }


    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.navigation_home -> {
                tabIndex = 0
                currentTabIndex = 0
                Log.i("HomeActivity", "onNavigationItemSelected Home")
                if (homeFollowingForYou == null) {
                    homeFollowingForYou = HomeFollowingForYou.newInstance()
                }
                homeFollowingForYou?.setPostIdLink(postId)
                homeFollowingForYou?.setTabHeight(height)
                setTabUI(true)
                val isLoad = loadFragment(homeFollowingForYou)
                Log.i("HomeActivity", "isCallLogin $isCallLogin")

                if (isCallLogin) {
                    homeFollowingForYou?.resumeVideoPlay(true)
                    isCallLogin = false
                } else {
                    homeFollowingForYou?.resumeVideoPlay(false)
                }
                homeFollowingForYou?.blockResume()
                if (notificationFragment != null) {
                    notificationFragment?.removeData()
                }
                if (myProfileFragment != null) {
                    myProfileFragment?.clearData()
                }
                return isLoad
            }
            R.id.navigation_explore -> {
                tabIndex = 1
                if (currentTabIndex == 0) {
                    homeFollowingForYou?.onStopAllVideo()
                }
                currentTabIndex = 1
                var callLoadData = true
                if (exploreFragment == null) {
                    callLoadData = false
                    exploreFragment = NewExploreFragment()
                }
                setTabUI(false)
                val isLoad = loadFragment(exploreFragment)
                if (callLoadData) {
                    exploreFragment?.loadData()
                }
                if (notificationFragment != null) {
                    notificationFragment?.removeData()
                }
                if (myProfileFragment != null) {
                    myProfileFragment?.clearData()
                }
                return isLoad
            }
            R.id.navigation_create_post -> {
                if (currentTabIndex == 0) {
                    homeFollowingForYou?.onStopAllVideo()
                }
                isLogin = SharedPreferenceUtils.getInstance(this)
                    .getBoolanValue(Constants.IS_LOGIN, false)
                if (isLogin) {
                    val intent = Intent(this@HomeActivity, CameraVideoRecording::class.java)
                    intent.putExtra("ISAUDIOMIX", false)
                    intent.putExtra("extractAudio", false)
                    intent.putExtra("audioFileNameFinal", "")
                    startActivityForResult(intent, 1010)
                } else {
                    isCallLogin = true
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("screen_name", "startPost")
                    startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
                }
                return false
            }
            R.id.navigation_notifications -> {
                tabIndex = 3
                if (currentTabIndex == 0) {
                    homeFollowingForYou?.onStopAllVideo()
                }
                currentTabIndex = 3
                var callLoadData = true
                if (notificationFragment == null) {
                    callLoadData = false
                    notificationFragment = NotificationFragement.newInstance()
                }
                setTabUI(false)
                val isLoad = loadFragment(notificationFragment)
                if (callLoadData) {
                    notificationFragment?.pageCount = 1
                    notificationFragment?.loadData()
                }
                if (myProfileFragment != null) {
                    myProfileFragment?.clearData()
                }
                return isLoad
            }
            R.id.navigation_profile -> {
                tabIndex = 4
                if (currentTabIndex == 0) {
                    homeFollowingForYou?.onStopAllVideo()
                }
                currentTabIndex = 4
                var callLoadData = true
                if (myProfileFragment == null) {
                    callLoadData = false
                    myProfileFragment = MyProfileFragment.newInstance()
                }
                setTabUI(false)
                val isLoad = loadFragment(myProfileFragment)
                if (callLoadData) {
                    myProfileFragment?.loadData()
                }
                if (notificationFragment != null) {
                    notificationFragment?.removeData()
                }
                return isLoad
            }
        }
        return false
    }

    public fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            val manager: FragmentManager = supportFragmentManager
            val backStateName: String = fragment.javaClass.name
            if (fragment.isAdded) {
                setHomeTabFragments(manager)
                val ft = manager.beginTransaction()
                ft.show(fragment).commit()
            } else {
                val ft: FragmentTransaction = manager.beginTransaction()
                ft.add(R.id.container, fragment)
                ft.addToBackStack(backStateName)
                // ft.commit()

                try {
                    ft.commitAllowingStateLoss()
                    // ft.commit()

                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
                return true
            }
            return true
        }
        return false
    }

    private fun showRatingReviewDialog() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            try {
                if (request.isSuccessful) {
                    Logger.print("request.isSuccessful !!!!!!!!!!!!!!!!${request.isSuccessful}")
                    // We got the ReviewInfo object
                    val reviewInfo = request.result
                    val flow = manager.launchReviewFlow(this, reviewInfo)

                    flow.addOnFailureListener {
                        it.printStackTrace()
                    }
                    flow.addOnCompleteListener {
                        Logger.print("addOnCompleteListener !!!!!!!!!!!!!!!!")
                    }
                }
            } catch (e: Exception) {
                // Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()

                e.printStackTrace()
            }
        }
        request.addOnFailureListener {
            it.printStackTrace()
            //   Toast.makeText(this,it.message,Toast.LENGTH_LONG).show()

        }
    }

    private fun setHomeTabFragments(manager: FragmentManager) {
        if (homeFollowingForYou != null && currentTabIndex != 0) {
            manager.beginTransaction().hide(homeFollowingForYou!!).commit()
        }
        if (exploreFragment != null && currentTabIndex != 1) {
            manager.beginTransaction().hide(exploreFragment!!).commit()
        }
        if (notificationFragment != null && currentTabIndex != 3) {
            manager.beginTransaction().hide(notificationFragment!!).commit()
        }
        if (myProfileFragment != null && currentTabIndex != 4) {
            manager.beginTransaction().hide(myProfileFragment!!).commit()
        }
    }

    private val uploadingMessage = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra("videoUploadMessage")!!
            showDialogForPostVideo(message)
        }
    }
    private val shareFromGallery = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var shareUri: Uri? = null
            shareUri = intent?.getParcelableExtra("shareUri")
            Logger.print("shareUri shareFromGallery===============$shareUri")


            if (shareUri != null && shareUri.path != "") {

                val intentShare = Intent(this@HomeActivity, PreviewActivity::class.java)
                intentShare.putExtra("shareUri", shareUri)
                intentShare.putExtra("IS_AUDIO_CAMERA", false)
                intentShare.putExtra("IS_MERGE", false)
                intentShare.putExtra("IS_CAMERA_PREVIEW", false)
                intentShare.putExtra("extractAudio", false)
                intentShare.putExtra("videoRotation", false)
                intentShare.putExtra("audioFile", "")
                startActivity(intentShare)
            }
        }
    }
    private val errorMessage = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra("apiErrorMessage")!!
            showDialogForPostVideo(message)
        }
    }

    private fun showDialogForPostVideo(message: String) {
        val inflater = LayoutInflater.from(this)

        val dialogView: View = inflater.inflate(R.layout.post_video_layout, null)
        val builder = AlertDialog.Builder(this)

        val b = builder.create()
        builder.setCancelable(true)
        builder.setView(dialogView)
        b.setCanceledOnTouchOutside(true)

        val show = builder.show()
        show.setCancelable(true)
        show.setCanceledOnTouchOutside(true)

        val tvYes = dialogView.findViewById<View>(R.id.tv_yes) as TextView
        val tvNo = dialogView.findViewById<View>(R.id.tv_no) as TextView
        val tvEditProfile = dialogView.findViewById<View>(R.id.tv_edit_profile) as TextView
        tvEditProfile.text = message
        show.setOnDismissListener {
            setResult(Activity.RESULT_OK)
            Log.i("CameraVideoRecording", "onDismiss Popup")
        }

        tvNo.setOnClickListener {
            show.dismiss()
        }

        tvYes.setOnClickListener {
            show.dismiss()
        }
    }

}