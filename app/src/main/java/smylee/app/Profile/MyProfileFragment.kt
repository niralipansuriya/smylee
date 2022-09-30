package smylee.app.Profile

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.fragment_my_profile.*
import org.json.JSONObject
import smylee.app.FollowUnfollowUser.FollowActivity
import smylee.app.FollowUnfollowUser.UnFollowActivity
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.engine.GridLayoutSpacing
import smylee.app.login.LoginActivity
import smylee.app.model.ForYouResponse
import smylee.app.playvideo.VideoDetailActivity
import smylee.app.setting.SettingActivity
import smylee.app.ui.Activity.FullScreenImage
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.io.*
import java.net.URL
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * A simple [Fragment] subclass.
 * Use the [MyProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyProfileFragment : Fragment() {
    var customLoaderDialog: CustomLoaderDialog? = null

    lateinit var viewModel: ProfileViewModel

    private var list = ArrayList<ForYouResponse>()
    private var pageCount = 1
    private var cmpagination: CustomPaginationIndicator? = null

    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var llm: LinearLayoutManager? = null
    private var currentVisibleCount: Int = 0

    private var totalItemCount: Int = 0
    private var lastVisibleItem: Int = 0
//    private var visibleThreshold = 10
    lateinit var activity: BaseActivity

    private var otherProfileAdapter: OtherProfileAdapter? = null
    private var userId: String = ""

    private var isLogin: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        customLoaderDialog = CustomLoaderDialog(activity)

        isLogin = SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_LOGIN, false)
        iv_choose.setOnClickListener {
            val intent = Intent(getActivity(), SettingActivity::class.java)
            startActivity(intent)
        }
        llEdtProfile.setOnClickListener {
            val intent = Intent(getActivity(), ProfileActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setNestedScrollingEnabled(rv_post, false)
        rv_post.isNestedScrollingEnabled = false
        ll_rv.isNestedScrollingEnabled = false

        val gridLayoutSpacing = GridLayoutSpacing()
        rv_post.addItemDecoration(gridLayoutSpacing)
        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()

        ll_follower.setOnClickListener {
            val intent = Intent(activity, FollowActivity::class.java)
            intent.putExtra("OTHER_USER_ID", "")
            intent.putExtra("isFromMyProfile", 1)
            startActivity(intent)
        }
        ll_following.setOnClickListener {
            val intent = Intent(activity, UnFollowActivity::class.java)
            intent.putExtra("OTHER_USER_ID", "")
            startActivity(intent)
        }
        iv_pic.setOnClickListener {
            //val profilePhoto: String = SharedPreferenceUtils.getInstance(activity).getStringValue(Constants.PROFILE_PIC_PREF, "")!!
            val profilePhoto: String = SharedPreferenceUtils.getInstance(activity)
                .getStringValue(Constants.CROPED_PROFILE_PIC, "")!!
            if (!profilePhoto.contentEquals("") && !profilePhoto.contentEquals("null")) {
                val intent = Intent(activity, FullScreenImage::class.java)
                intent.putExtra("imageUrl", profilePhoto)
                startActivity(intent)
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

    private fun addLoadingView() {
        try {
            if (cmpagination == null)
                cmpagination = CustomPaginationIndicator(getActivity())
            cmpagination!!.show(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearData() {
        /*list.clear()
        otherProfileAdapter?.notifyDataSetChanged()
        otherProfileAdapter = null*/
    }

    fun loadData() {
        if (::activity.isInitialized) {
            if (nestedScrollView != null) {
                loadFragmentData()
            } else {
                Handler().postDelayed({
                    loadFragmentData()
                }, 200)
            }
        } else {
            Log.i("MyProfileFragment", "activity not initialised")
        }
    }

    private fun loadFragmentData() {
        Log.i("MyProfileFragment", "loadFragmentData")
        isLogin = SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_LOGIN, false)
        if (isLogin) {
            Logger.print("isLogin loadFragmentData********************************************")
            nestedScrollView.visibility = View.VISIBLE
            htab_appbar.visibility = View.VISIBLE
            ll_without_login_profile.visibility = View.GONE
            pageCount = 1
            val apiCallTime = SharedPreferenceUtils.getInstance(activity).getLongValue(Constants.PROFILE_DATA_API_TIME, 0)
            val isAnythingChanged = SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, false)
            val currentTime = System.currentTimeMillis()
            if ((currentTime - apiCallTime) > 1500000 || isAnythingChanged) {
                Logger.print("isAnythingChanged loadFragmentData ******************************")
                if (!isLoading) {
                    Logger.print(" isLoading loadFragmentData***********************************")
                    getUserProfile(true)
                }
                SharedPreferenceUtils.getInstance(activity).setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, false)
            } else {
                Logger.print("else  loadFragmentData*****************************************")
                ProfileReadAsync(this).execute()
                // applyPagination()
            }
            val profilePhoto: String = SharedPreferenceUtils.getInstance(activity).getStringValue(Constants.PROFILE_PIC_PREF, "")!!
            if (!profilePhoto.contentEquals("") && !profilePhoto.contentEquals("null")) {
                Logger.print("isAnythingChanged ProfileReadAsync loadFragmentData********************************************")
                Glide.with(this)
                    .load(profilePhoto)
                    .error(R.drawable.profile_thumb)
                    .into(iv_pic)
                ivBlurPic.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this)
                    .load(profilePhoto)
                    .into(ivBlurPic)
            }
        } else {
            Logger.print("not login loadFragmentData********************************************")
            nestedScrollView.visibility = View.GONE
            htab_appbar.visibility = View.GONE
            ll_without_login_profile.visibility = View.VISIBLE
        }
        btnSignUp.setOnClickListener {
            val intent = Intent(getActivity(), LoginActivity::class.java)
            intent.putExtra("screen_name", "profile")
            activity.startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i("MyProfileFragment", "onResume")
        isLogin = SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_LOGIN, false)
        if (isLogin) {
            nestedScrollView.visibility = View.VISIBLE
            htab_appbar.visibility = View.VISIBLE
            ll_without_login_profile.visibility = View.GONE
            pageCount = 1
            val apiCallTime = SharedPreferenceUtils.getInstance(activity).getLongValue(Constants.PROFILE_DATA_API_TIME, 0)
            val isAnythingChanged = SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, false)
            val currentTime = System.currentTimeMillis()
            if ((currentTime - apiCallTime) > 1500000 || isAnythingChanged) {
                Logger.print("isAnythingChanged================")
                if (!isLoading) {
                    getUserProfile(true)
                    Logger.print("isLoading================")
                }
                SharedPreferenceUtils.getInstance(activity).setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, false)
            } else {
                Logger.print("ProfileReadAsync================")
                ProfileReadAsync(this).execute()
                // applyPagination()
            }
            val profilePhoto: String = SharedPreferenceUtils.getInstance(activity)
                .getStringValue(Constants.PROFILE_PIC_PREF, "")!!
            if (!profilePhoto.contentEquals("") && !profilePhoto.contentEquals("null")) {
                Glide.with(this)
                    .load(profilePhoto)
                    .error(R.drawable.profile_thumb)
                    .into(iv_pic)
                Glide.with(this)
                    .load(profilePhoto)
                    .into(ivBlurPic)
            }
        } else {
            nestedScrollView.visibility = View.GONE
            htab_appbar.visibility = View.GONE
            ll_without_login_profile.visibility = View.VISIBLE
        }
        btnSignUp.setOnClickListener {
            // activity.isCallLogin = true
            val intent = Intent(getActivity(), LoginActivity::class.java)
            intent.putExtra("screen_name", "profile")
            activity.startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        //activity = context as HomeActivity
        activity = context as BaseActivity
    }

    private fun applyPagination() {
        rv_post.addOnScrollListener(onScrollListener)
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            currentVisibleCount = llm?.childCount!!
            totalItemCount = llm?.itemCount!!
            lastVisibleItem = llm?.findFirstVisibleItemPosition()!!
            if (!isLoading && list.size > 0) {
                if (!isDataFinished) {
                    //  if (totalItemCount - currentVisibleCount <= lastVisibleItem + visibleThreshold) {
                    if (currentVisibleCount + lastVisibleItem >= totalItemCount && lastVisibleItem >= 0) {
                        isLoading = true
                        pageCount++
                        if (dy > 0) {
                            addLoadingView()
                        }
                        addViewLoadingView()
                        Logger.print("applyPagination !!!!!!!!!!!!!!!!!!!!!!!!!!!!$pageCount")
                        Handler().postDelayed({ getUserProfile(true) }, Constants.DELAY)
                    }
                } else {
                    println("........finished data..........")
                }
            }
        }
    }

    private fun addViewLoadingView() {
        if (list.size > 0) {
            rv_post.post {
                if (otherProfileAdapter != null) {
                    otherProfileAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun getUserProfile(ISProgress: Boolean) {
        Logger.print("pageCount getUserProfile ======================$pageCount")
        isLoading = true
        userId = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.USER_ID_PREF, "").toString()
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["other_user_id"] = userId
        val isProgressShow: Boolean = if (ISProgress) {
            pageCount == 1
        } else {
            false
        }

        viewModel.observeGetProfile(activity, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                removeLoaderView()
                Logger.print("page no !!!!!!!!!!!!!!!!!!!!!!!$pageCount")
                SharedPreferenceUtils.getInstance(activity).setValue(Constants.PROFILE_DATA_API_TIME, System.currentTimeMillis())
                setProfileResponseData(it, false)
            }
        })
    }

    private fun setProfileResponseData(response: String, isFromReadFile: Boolean) {
        val jsonObject = JSONObject(response)
        val code = jsonObject.getInt("code")
        if (code == 1) {
            if (jsonObject.has("data")) {
                val jsonobj1 = jsonObject.getJSONObject("data")
                val jsonobj2 = jsonobj1.getJSONObject("user_data")
                val jsonArray = jsonobj1.getJSONArray("user_post")

                if (pageCount == 1) {
                    if (!isFromReadFile) {
                        Logger.print("isFromReadFile========================$isFromReadFile")
                        ProfileWriterAsync(activity).execute(response.trim())
                    }
                    val userName = jsonobj2.getString("first_name")
                    val profilePic = jsonobj2.getString("profile_pic")

                    if (jsonobj2.has("facebook_url")) {
                        val facebookUrl = jsonobj2.getString("facebook_url")
                        SharedPreferenceUtils.getInstance(activity)
                            .setValue(Constants.FACEBOOK_URL, facebookUrl)
                    }
                    if (jsonobj2.has("mark_as_verified_badge")) {
                        val mark_as_verified_badge = jsonobj2.getInt("mark_as_verified_badge")
                        if (mark_as_verified_badge != null && mark_as_verified_badge == 1) {
                            ivVerified?.visibility = View.VISIBLE
                        } else {
                            ivVerified?.visibility = View.GONE

                        }
                        SharedPreferenceUtils.getInstance(activity)
                            .setValue(Constants.MARK_AS_VERIFIED_BADGE, mark_as_verified_badge)
                    }
                    if (jsonobj2.has("instagram_url")) {
                        val instagramUrl = jsonobj2.getString("instagram_url")
                        SharedPreferenceUtils.getInstance(activity)
                            .setValue(Constants.INSTAGRAM_URL, instagramUrl)
                    }
                    if (jsonobj2.has("youtube_url")) {
                        val youtubeUrl = jsonobj2.getString("youtube_url")
                        SharedPreferenceUtils.getInstance(activity)
                            .setValue(Constants.YOUTUBE_URL, youtubeUrl)
                    }
                    val profilePicCompress = jsonobj2.getString("profile_pic_compres")
                    val following = jsonobj2.getString("no_of_followings")
                    val follower = jsonobj2.getString("no_of_followers")
                    val post = jsonobj2.getString("no_of_post")

                    if (!post.isNullOrEmpty() && !post.contentEquals("null") && tv_post != null) {
                        Methods.showViewCounts(post, tv_post)
                    }
                    if (!userName.isNullOrEmpty() && !userName.contentEquals("") && !userName.contentEquals("null") && tv_name != null) {
                        tv_name.text = userName
                    }
                    if (!follower.isNullOrEmpty() && !follower.contentEquals("") && !follower.contentEquals("null") && txt_follower != null) {
                        Methods.showViewCounts(follower, txt_follower)
                    }
                    if (!following.isNullOrEmpty() && !following.contentEquals("") && !following.contentEquals("null") && txt_following != null) {
                        Methods.showViewCounts(following, txt_following)
                    }
                    if (!profilePicCompress.isNullOrEmpty() && !profilePicCompress.contentEquals("") && !profilePicCompress.contentEquals("null")) {
                        SharedPreferenceUtils.getInstance(activity).setValue(Constants.CROPED_PROFILE_PIC, profilePicCompress)
                    }
                    if (!profilePic.isNullOrEmpty() && !profilePic.contentEquals("") && iv_pic != null) {
                        SharedPreferenceUtils.getInstance(activity).setValue(Constants.PROFILE_PIC_PREF, profilePic)
                        iv_pic.setImageResource(R.drawable.userprofilenotification)
                        Glide.with(context!!)
                            .asBitmap()
                            .load(profilePic)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    if (iv_pic != null) {
                                        iv_pic.setImageBitmap(resource)
                                    }
                                    try {
                                        ivBlurPic.scaleType = ImageView.ScaleType.CENTER_CROP
                                        Blurry.with(context).from(resource).into(ivBlurPic)
                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            })
                    } else if (ivBlurPic != null) {
                        ivBlurPic!!.setBackgroundColor(ContextCompat.getColor(activity, R.color.purple))
                    }
                }

                val array = Gson().fromJson<ArrayList<ForYouResponse>>(jsonArray.toString(), object : TypeToken<ArrayList<ForYouResponse>>() {}.type)
                if (pageCount == 1) {
                    list.clear()
                }
                if (array.isNotEmpty()) {
                    isDataFinished = false
                    list.addAll(array)
                } else {
                    isDataFinished = true
                }

                if(no_data_found != null && rv_post != null) {
                    if (list.size > 0) {
                        no_data_found.visibility = View.GONE
                        rv_post.visibility = View.VISIBLE
                        setAdapter((isFromReadFile || pageCount == 1))
                    } else {
                        rv_post.visibility = View.GONE
                        no_data_found.visibility = View.VISIBLE
                    }
                }
                isLoading = false
            }
        }
    }

    private fun setAdapter(isSetAdapter: Boolean) {
        if (otherProfileAdapter == null || isSetAdapter) {
            llm = GridLayoutManager(context, 3)
            rv_post.layoutManager = llm
            rv_post.setHasFixedSize(true)
            otherProfileAdapter = OtherProfileAdapter(activity, list, object : OtherProfileAdapter.ManageClick {
                override fun manageDeletePost(IDp: String?) {
                    if (IDp != null && !IDp.contentEquals("")) {
                        deletePost(IDp)
                    }
                }

                override fun managePlayVideoClick(position: Int) {
                    val intent = Intent(context, VideoDetailActivity::class.java)
                    intent.putExtra("position", position)
                    intent.putExtra("cat_ID", "")
                    intent.putExtra("screen", "myProfile")
                    intent.putExtra("hashtag", "")
                    intent.putExtra("responseData", list)
                    intent.putExtra("pageNo", pageCount)
                    intent.putExtra("searchTxt", "")
                    intent.putExtra("userIDApi", userId)
                    startActivity(intent)
                }

                override fun shareVideo(
                    position: Int,
                    url: String,
                    postHeight: Int,
                    postWidth: Int
                ) {
                    requestPermission(url, postHeight, postWidth)
                    Logger.print("url============$url")
                }


            })
            rv_post.adapter = otherProfileAdapter
            applyPagination()
        } else {
            otherProfileAdapter?.notifyDataSetChanged()
            //  applyPagination()
        }
    }

    private class DownloadFileFromURL internal constructor(
        var activity: BaseActivity,
        var postHeight: Int,
        var postWidth: Int,
        var customLoaderDialog: CustomLoaderDialog

    ) : AsyncTask<String?, String?, String?>() {
        var nameoffile: String? = null
        override fun onPreExecute() {
            super.onPreExecute()
            if (customLoaderDialog != null)
                customLoaderDialog!!.show(false)
            // DialogUtils.showProgressDialog(activity, "")
        }


        override fun onProgressUpdate(vararg values: String?) {
        }

        override fun onPostExecute(file_url: String?) {

            Logger.print("share video path ==========${Methods.filePathFromDir(Constants.Share_Path)}")

            val rootDirectory = File(Constants.ROOT_PATH)

            val gifFile = File(Constants.ROOT_PATH, "smylee_gif_new.gif")
            if (!rootDirectory.exists()) {
                rootDirectory.mkdirs()
                gifFile.createNewFile()
                installBinaryFromRaw(activity, R.raw.smylee_gif_new, gifFile)
            } else if (!gifFile.exists()) {
                gifFile.createNewFile()
                installBinaryFromRaw(activity, R.raw.smylee_gif_new, gifFile)
            }
            //  val fileDirectory = File(Constants.DOWNLOADED_PATH)
            val fileDirectory = File(Constants.Share_Path)
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs()
            }

            val tmpFilePath =
                Constants.Share_Path + "Smylee_" + System.currentTimeMillis() + "1.mp4"

            AddGIF(
                Methods.filePathFromDir(Constants.Share_Path)!!,
                postHeight,
                postWidth,
                tmpFilePath,
                activity,
                customLoaderDialog
            ).execute()

            Logger.print("onPostExecute =============$file_url")
            /*  try {
                  if (cm != null && cm!!.isShowing) {
                      cm!!.hide()
                  }
              } catch (e: IllegalArgumentException) {
                  e.printStackTrace()
              } catch (e: Exception) {
                  e.printStackTrace()
              } finally {
                  cm = null
              }
              if (file_url == "Success") {

              } else {
                  Utils.showToastMessage(
                      context = activity,
                      message = activity.resources.getString(R.string.error_downloading_audio)
                  )
              }*/

        }

        private fun getFileOutputStream(file: File?): OutputStream? {
            try {
                return FileOutputStream(file)
            } catch (e: FileNotFoundException) {
                Log.e("MainActivity", "File not found attempting to stream file.", e)
            }
            return null
        }

        private fun pipeStreams(`is`: InputStream, os: OutputStream) {
            val buffer = ByteArray(1024)
            var count: Int
            try {
                while (`is`.read(buffer).also { count = it } > 0) {
                    os.write(buffer, 0, count)
                }
            } catch (e: IOException) {
                Log.e("MainActivity", "Error writing stream.", e)
            }
        }

        private fun installBinaryFromRaw(context: Context, resId: Int, file: File?) {
            val rawStream: InputStream = context.resources.openRawResource(resId)
            val binStream: OutputStream = getFileOutputStream(file)!!
            pipeStreams(rawStream, binStream)
            try {
                rawStream.close()
                binStream.close()
            } catch (e: IOException) {
                Log.e("MainActivity", "Failed to close streams!", e)
            }
        }

        init {
        }

        override fun doInBackground(vararg p0: String?): String? {
            val fileDirectory = File(Constants.Share_Path)
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs()
            }
            var count: Int
            var input: InputStream? = null
            var output: OutputStream? = null
            try {
                val url = URL(p0[0])
                val urofysllabusl: String = p0[0]!!
                println("urofysllabusl::::::$urofysllabusl")
                val parts = urofysllabusl.split("/".toRegex()).toTypedArray()
                println("parts::::::::::::::$parts")
                var result = parts[parts.size - 1]
                result = result.replace("+", "", true)
                result = result.replace("(", "", true)
                result = result.replace(")", "", true)
                result = result.replace("_", "", true)
                println("result:::::::::::$result")
                nameoffile = result

                println("result::::::doInback::::$result")
                println("name in  doInBackground>>>>$nameoffile")
                val conection = url.openConnection()
                conection.connect()
                val lenghtOfFile = conection.contentLength

                input = BufferedInputStream(url.openStream())
                output = FileOutputStream(Constants.Share_Path + "" + nameoffile)

                println("output:::::::::$output")
                val data = ByteArray(1024)
                var total: Long = 0
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (total * 100 / lenghtOfFile).toInt())

                    // writing data to file
                    output.write(data, 0, count)
                }
                output.flush()
                output.close()
                input.close()


            } catch (e: Exception) {
                e.printStackTrace()
                if (output != null) {
                    output.flush()
                    output.close()
                }
                input?.close()
                return "error"
            }
            return "Success"
        }
    }


    class AddGIF(
        var filePath: String,
        var videoHeight: Int,
        var videoWidth: Int,
        var tmpFilePath: String,
        var activity: BaseActivity,
        var customLoaderDialog: CustomLoaderDialog
    ) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            val gifFile = File(Constants.ROOT_PATH, "smylee_gif_new.gif")

            val command: String
            val standardWidth = 360.0f
            val standardHeight = 640.0f
            if (videoHeight > 100 && videoWidth > 100) {
                val scaleWidth = (videoWidth / standardWidth) * 100
                val scaleHeight = (videoHeight / standardHeight) * 100
                Logger.print("ShareVideoUtils > Scale Width $scaleWidth")
                Logger.print("ShareVideoUtils > Scale Height $scaleHeight")
                var scale: Int
                if (videoWidth > videoHeight) {
                    if (scaleHeight > scaleWidth) {
                        scale = scaleWidth.toInt()
                    } else {
                        scale = scaleHeight.toInt()
                    }
                    if (scale < 75) {
                        scale = 75
                    }
                } else {
                    if (scaleHeight > scaleWidth) {
                        scale = scaleHeight.toInt()
                    } else {
                        scale = scaleWidth.toInt()
                    }
                }
                Logger.print("ShareVideoUtils > Scale $scale")
                command =
                    "-i $filePath -ignore_loop 0 -i ${gifFile.absolutePath} -ignore_loop 0 -i ${gifFile.absolutePath} -filter_complex \"[1:v]scale=$scale:$scale[b];[0:v][b] overlay=10:10:shortest=1:enable='between(t,0,7)' [tmp]; [2:v]scale=$scale:$scale[c];[tmp][c] overlay=${videoWidth - (100 + (scale - 100))}:${videoHeight - (100 + (scale - 100))}:shortest=1:enable='between(t,7,30)'\" -pix_fmt yuv420p -map 0:a? -qscale:v 3 -c:a copy $tmpFilePath -y"
            } else if (videoHeight > 100) {
                val scaleHeight = (videoHeight / standardHeight) * 100
                Logger.print("ShareVideoUtils > Scale Height $scaleHeight")
                command =
                    "-i $filePath -ignore_loop 0 -i ${gifFile.absolutePath} -ignore_loop 0 -i ${gifFile.absolutePath} -filter_complex \"[1:v]scale=$scaleHeight:$scaleHeight[b];[0:v][b] overlay=10:10:shortest=1:enable='between(t,0,7)' [tmp]; [2:v]scale=$scaleHeight:$scaleHeight[c];[tmp][c] overlay=10:${videoHeight - (100 + (scaleHeight - 100))}:shortest=1:enable='between(t,7,30)'\" -pix_fmt yuv420p -map 0:a? -qscale:v 3 -c:a copy $tmpFilePath -y"
            } else if (videoWidth > 100) {
                val scaleWidth = (videoWidth / standardWidth) * 100
                Logger.print("ShareVideoUtils > Scale Width $scaleWidth")
                command =
                    "-i $filePath -ignore_loop 0 -i ${gifFile.absolutePath} -ignore_loop 0 -i ${gifFile.absolutePath} -filter_complex \"[1:v]scale=$scaleWidth:$scaleWidth[b];[0:v][b] overlay=10:10:shortest=1:enable='between(t,0,7)' [tmp]; [2:v]scale=$scaleWidth:$scaleWidth[c];[tmp][c] overlay=${videoWidth - (100 + (scaleWidth - 100))}:10:shortest=1:enable='between(t,7,30)'\" -pix_fmt yuv420p -map 0:a? -qscale:v 3 -c:a copy $tmpFilePath -y"
            } else {
                command =
                    "-i $filePath -ignore_loop 0 -i ${gifFile.absolutePath} -ignore_loop 0 -i ${gifFile.absolutePath} -filter_complex \"[0:v][1:v] overlay=10:10:shortest=1:enable='between(t,0,7)' [tmp]; [tmp][2:v] overlay=10:10:shortest=1:enable='between(t,7,30)'\" -pix_fmt yuv420p -map 0:a? -qscale:v 3 -c:a copy $tmpFilePath -y"
            }
            FFmpeg.execute(command)

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            File(filePath).delete()
            // scanDownloadedFile(context)
            Logger.print("FFmpeg > onSuccess onPostExecute")

            Logger.print("path of post video ==============${Methods.filePathFromDir(Constants.Share_Path)}")

            try {
                if (customLoaderDialog != null && customLoaderDialog.isShowing) {
                    Logger.print("isShowing ==============")


                    customLoaderDialog.hide()
                }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // customLoaderDialog = null
            }
            shareVideo(
                "Share this video",
                Methods.filePathFromDir(Constants.Share_Path)!!,
                activity
            )


        }

        fun shareVideo(title: String?, path: String, activity: BaseActivity) {
            MediaScannerConnection.scanFile(activity, arrayOf(path),
                null, object : MediaScannerConnection.OnScanCompletedListener {
                    override fun onScanCompleted(path: String?, uri: Uri?) {
                        val shareIntent = Intent(
                            Intent.ACTION_SEND
                        )
                        shareIntent.type = "video/*"
                        shareIntent.putExtra(
                            Intent.EXTRA_SUBJECT, title
                        )
                        shareIntent.putExtra(
                            Intent.EXTRA_TITLE, title
                        )
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                        shareIntent
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                        activity.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Share Video"
                            )
                        )
                    }
                })
        }

    }

    private fun requestPermission(
        url: String, postHeight: Int, postWidth: Int

    ) {
        Dexter.withActivity(activity).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                if (report.areAllPermissionsGranted()) {
                    Methods.deleteFilesFromDirectory(Constants.Share_Path)
                    DownloadFileFromURL(
                        activity,
                        postHeight,
                        postWidth,
                        customLoaderDialog!!
                    ).execute(url)

                }
                if (report.isAnyPermissionPermanentlyDenied) {
                    showSettingsDialog()
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

    private fun deletePost(POST_ID: String) {
        val hashMap = HashMap<String, String>()
        hashMap["post_id"] = POST_ID
        viewModel.DeletePost(activity, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("DELETE POST Response : $it")
                val jsonObject = JSONObject(it.toString())
                if (jsonObject["code"] == 1) {
                    pageCount = 1
                    getUserProfile(false)
                }
            }
        })
    }

    companion object {
        fun newInstance() = MyProfileFragment()

        class ProfileWriterAsync(val context: Context) : AsyncTask<String, Void, Void>() {
            override fun doInBackground(vararg params: String): Void? {
                try {
                    Utils.writeProfileDataToFile(context, params[0].trim())
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }

        class ProfileReadAsync(private val myProfileFragment: MyProfileFragment) : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void?): String {
                return try {
                    Utils.readProfileDataFromFile(myProfileFragment.context!!)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    ""
                }
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                myProfileFragment.list.clear()
                myProfileFragment.rv_post?.removeOnScrollListener(myProfileFragment.onScrollListener)
                if(myProfileFragment.otherProfileAdapter != null) {
                    myProfileFragment.otherProfileAdapter?.notifyDataSetChanged()
                }
                if (result != "") {
                    Logger.print("result======================$result")
                    myProfileFragment.setProfileResponseData(result!!.trim(), true)
                } else {
                    if (!myProfileFragment.isLoading) {
                        Logger.print("else result======================$result")
                        myProfileFragment.pageCount = 1
                        myProfileFragment.getUserProfile(true)
                    }
                }
            }
        }
    }
}