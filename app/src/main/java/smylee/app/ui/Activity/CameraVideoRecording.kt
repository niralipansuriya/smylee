package smylee.app.ui.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.CamcorderProfile
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.webkit.MimeTypeMap
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daasuu.gpuv.camerarecorder.*
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_camera_video_recording.*
import smylee.app.CallBacks.MergerCallBacks
import smylee.app.MusicListing.MusicListingForYouAlbumActivity
import smylee.app.R
import smylee.app.adapter.FilterListAdapter
import smylee.app.adapter.SpeedAdapter
import smylee.app.camerfilters.FilterType
import smylee.app.camerfilters.OnFilterSelectListener
import smylee.app.camerfilters.SampleCameraGLView
import smylee.app.dialog.CommonAlertDialog
import smylee.app.engine.VideoCombiner
import smylee.app.model.FilterModel
import smylee.app.postvideo.PostVideoViewModel
import smylee.app.services.VideoUploadService
import smylee.app.startPost.StartNewPostActivity
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class CameraVideoRecording : BaseActivity(), OnFilterSelectListener, MergerCallBacks {
    private lateinit var viewModel: PostVideoViewModel
    private var player = MediaPlayer()
    private var audioFileNameFinal: String = ""
    private var finalAudioId: String = ""
    private var isDownloadFinal: String = ""
    private val mCombineListener: VideoCombiner.VideoCombineListener? = null
    private var currentPlayerTime: Int = 0
    var data: ByteArray? = null

    private val audioFiles = ArrayList<String>() //ArrayList cause you don't know how many files there is
    private var mOutputFilePath: String? = null
    private var mPauseVideo: Boolean = false
    private var isAudioMix: Boolean = false
    private var isTimerSelected: Boolean = false
    private var mResumeVideo: Boolean = false
    private var mStopVideo: Boolean = false
    private var isResumeFromPickGallery = false
    private var isResumeFromPreview = false
    val result = ArrayList<String>() //ArrayList cause you don't know how many files there is
    private var mixAudioDir: File? = null
    private var audioDir: File? = null
    private val speedArray = ArrayList<String>() //ArrayList cause you don't know how many files there is
    private val timerArray = ArrayList<String>() //ArrayList cause you don't know how many files there is
    lateinit var audio: File

    var milliLeft: Long = 0
    private var trimSec: Int = 30
    var i: Int = 0
    private var timeLengthMilli: Long? = null
    private var timeLengthMilliVideo: Long? = null

    private var progressDialogLocal: ProgressDialog? = null

    var mIsRecordingVideo: Boolean = false
    private var mIsFirstRecord: Boolean = true

    private var currentFile: File? = null

    private var mMediaRecorder: MediaRecorder? = null
    val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P)!!
    private var isFlashSupported: Boolean = false

    private var isMergeFile: Boolean = false
    private val requestVideoTrimmer = 101
    private val requestTrimmerVideo = 102
    private val requestVideoPlay = 103
    private val requestPreviewCamera = 104
    private val requestCreatePost = 105

    private var videoRotation = 0

    private var postTitle: String = ""
    private var selectedCatIDSFinal = ""
    private var selectedLangIDSFinal = ""

    private var gpuCameraRecorder: GPUCameraRecorder? = null
    private var sampleGLView: SampleCameraGLView? = null
    private var toggleClick = false
    private var cameraWidth = 1280
    private var cameraHeight = 720
    private var videoWidth = 720
    private var videoHeight = 1280

    private var lensFacing = LensFacing.BACK
    private lateinit var filterListAdapter: FilterListAdapter
    private var durationHandler: Handler? = null
    private var durationHandlerTimer: Handler? = null
    private var animationEnter: ScaleAnimation? = null
    private var animationEnterTimer: ScaleAnimation? = null
    private var animationExit: ScaleAnimation? = null
    private var animationExitTimer: ScaleAnimation? = null

    private var isSettingDialogShowing: Boolean = false
    private var isPickFromGallery: Boolean = false
    private var extractAudio: Boolean = false

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_video_recording)

        cameraVideoRecording = this
        mixAudioDir = Path.createFileDir(this, Constants.MixAudioVideo)
        audioDir = Path.createFileDir(this, Constants.AudioFile)
        viewModel = ViewModelProviders.of(this).get(PostVideoViewModel::class.java)

        imgTimer.visibility = View.VISIBLE
        timeLengthMilliVideo = 0L

        durationHandler = Handler()
        durationHandlerTimer = Handler()
        animationEnter = ScaleAnimation(0.5F, 1F, 0.5F, 1F,
            Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5F)
        animationEnter?.duration = 200
        animationExit = ScaleAnimation(1F, 0.5F, 1F, 0.5F,
            Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5F)
        animationExit?.duration = 200
        animationEnterTimer = ScaleAnimation(0.5F, 1F, 0.5F, 1F, Animation.RELATIVE_TO_SELF,
            0.5F, Animation.RELATIVE_TO_SELF, 0.5F)
        animationEnterTimer?.duration = 200
        animationExitTimer = ScaleAnimation(
            1F, 0.5F, 1F, 0.5F, Animation.RELATIVE_TO_SELF,
            0.5F, Animation.RELATIVE_TO_SELF, 0.5F
        )
        animationExitTimer?.duration = 200
        progressDialogLocal = ProgressDialog(this)
        progressDialogLocal?.setCancelable(false)

        val filterTypes: List<FilterModel> = prepareFilterList()
        filterListAdapter = FilterListAdapter(this, filterTypes)
        filterListAdapter.setOnFilterSelectListener(this)
        val layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rvFilters.layoutManager = layoutManager
        rvFilters.adapter = filterListAdapter

        audioFiles.add("Audio 1")
        audioFiles.add("Audio 2")
        audioFiles.add("Audio 3")
        audioFiles.add("Audio 4")

        speedArray.add("0.25")
        speedArray.add("0.50")
        speedArray.add("1.0")
        speedArray.add("2.0")
        speedArray.add("4.0")
        speedArray.add("6.0")

        timerArray.add("5")
        timerArray.add("10")
        timerArray.add("15")

       /*nirali if (intent != null) {
            isAudioMix = intent.getBooleanExtra("ISAUDIOMIX", false)
            extractAudio = intent.getBooleanExtra("extractAudio", false)
            audioFileNameFinal = intent.getStringExtra("audioFileNameFinal")!!
            Logger.print("ISAUDIOMIX==========$isAudioMix")
            Logger.print("audioFileNameFinal==========$audioFileNameFinal")
        }*/

        imgSpeed.setOnClickListener {
            showDialogForSpeed(this, speedArray)
        }
        imgPickVideo.setOnClickListener {
            pickFromGallery()
        }
        iv_audio_remove.setOnClickListener {
            showDialogForRemoveAudio(imgChangeDuration, iv_audio_remove)
        }

        m_flash_light.setOnClickListener {
            gpuCameraRecorder?.switchFlashMode(m_flash_light)
            Logger.print("CameraThread.isFlashTorch==============" + CameraThread.isFlashTorch)
            if (CameraThread.isFlashTorch) {
                m_flash_light.setImageResource(R.drawable.flash_on)
            }
            if (!CameraThread.isFlashTorch) {
                m_flash_light.setImageResource(R.drawable.flash_off)
            }
        }

        ivClose?.setOnClickListener {
            onBackPressed()
        }
        mspeed.setOnClickListener {
            showDialogForSpeed(this, speedArray)
        }

        imgChangeDuration.setOnClickListener {
            if (timeLengthMilli == 15000L) {
                tvShowDuration.setText(R.string.thirty_seconds)
                tvShowDuration.visibility = View.VISIBLE
                tvShowDuration.startAnimation(animationEnter)
                durationHandler?.postDelayed(durationRunnable, 500)
                imgChangeDuration.setImageResource(R.drawable.duration_thirdty)
                timeLengthMilli = 30000L
                trimSec = 30
            } else {
                tvShowDuration.setText(R.string.fifteen_seconds)
                tvShowDuration.visibility = View.VISIBLE
                tvShowDuration.startAnimation(animationEnter)
                durationHandler?.postDelayed(durationRunnable, 500)
                imgChangeDuration.setImageResource(R.drawable.duration_fifteen)
                timeLengthMilli = 15000L
                trimSec = 30
            }
        }


        // shareVideoFB(fbSharePath!!)

        imgTimer.setOnClickListener {
            when (timeLengthMilliVideo) {
                0L -> {
                    isTimerSelected = true
                    timeLengthMilliVideo = 5000L
                    tvTimerLength.visibility = View.VISIBLE
                    tvTimerLength.setText(R.string.five_seconds)
                    tvTimerLength.startAnimation(animationEnterTimer)
                    durationHandlerTimer?.postDelayed(durationRunnableTimer, 500)
                    imgTimer.setImageResource(R.drawable.timer_five_sec_final)
                }
                5000L -> {
                    isTimerSelected = true
                    timeLengthMilliVideo = 10000L
                    tvTimerLength.visibility = View.VISIBLE
                    tvTimerLength.setText(R.string.ten_seconds)
                    tvTimerLength.startAnimation(animationEnterTimer)
                    durationHandlerTimer?.postDelayed(durationRunnableTimer, 500)
                    imgTimer.setImageResource(R.drawable.timer_ten_sec_final)
                }
                10000L -> {
                    isTimerSelected = false
                    timeLengthMilliVideo = 0L
                    tvTimerLength.visibility = View.VISIBLE
                    tvTimerLength.setText(R.string.off_timer)
                    tvTimerLength.startAnimation(animationEnterTimer)
                    durationHandlerTimer?.postDelayed(durationRunnableTimer, 500)
                    imgTimer.setImageResource(R.drawable.timer_off_final)
                }
            }
        }

        mcamera_rear_front.setOnClickListener {
            lensFacing = if (lensFacing == LensFacing.BACK) {
                LensFacing.FRONT
            } else {
                LensFacing.BACK
            }
            gpuCameraRecorder?.setCameraLens(lensFacing)
        }

        timeLengthMilli = 15000
        progressbar.progress = 0

        iv_audio_select.setOnClickListener {
            val intent = Intent(this, MusicListingForYouAlbumActivity::class.java)
            this.startActivity(intent)
        }
        mTextureView.setOnClickListener {
            if (rlFilters.visibility == View.VISIBLE) {
                rlFilters.visibility = View.GONE
            }
        }
        imgFilters.setOnClickListener {
            if (rlFilters.visibility == View.VISIBLE) {
                rlFilters.visibility = View.GONE
            } else {
                rlFilters.visibility = View.VISIBLE
            }
        }
        imgCloseFilter.setOnClickListener {
            rlFilters.visibility = View.GONE
        }
        imgApplyFilter.setOnClickListener {
            gpuCameraRecorder?.setFilter(
                FilterType.createGlFilter(
                    FilterType.DEFAULT,
                    applicationContext
                )
            )
            rlFilters.visibility = View.GONE
        }

        mstop_video.setOnClickListener {
            mStopVideo = true
            progressbar.progress = 0
            if (mIsRecordingVideo) {
                stopTimer()
                mIsRecordingVideo = false
                gpuCameraRecorder?.stop()
                mRecordVideo.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.round_camera
                    )
                )
            } else {
                mRecordVideo.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.round_camera
                    )
                )
            }
            gpuCameraRecorder?.flashOFF()
            if (CameraThread.isFlashTorch) {
                m_flash_light.setImageResource(R.drawable.flash_on)
            }
            if (!CameraThread.isFlashTorch) {
                m_flash_light.setImageResource(R.drawable.flash_off)
            }
            combine()
        }

        mRecordVideo.setOnClickListener {
            disableIcons()
            progressbar.visibility = View.VISIBLE
            if (mIsFirstRecord) {
                val folder = File(Constants.RECORD_PATH)
                if (folder.listFiles() != null && folder.listFiles()!!.isNotEmpty()) {
                    val filesInFolder =
                        folder.listFiles()!! // This returns all the folders and files in your path
                    for (file in filesInFolder) {
                        file.delete()
                    }
                }

                val folder1 = File(Constants.MERGE_PATH)
                if (folder1.listFiles() != null && folder1.listFiles()!!.isNotEmpty()) {
                    val filesInFolder =
                        folder1.listFiles()!! // This returns all the folders and files in your path
                    for (file in filesInFolder) {
                        file.delete()
                    }
                }

                val folder2 = File(Constants.COMPRESSEDFILES_PATH)
                if (folder2.listFiles() != null && folder2.listFiles()!!.isNotEmpty()) {
                    val filesInFolder =
                        folder2.listFiles()!! // This returns all the folders and files in your path
                    for (file in filesInFolder) {
                        file.delete()
                    }
                }
                mIsFirstRecord = false
            }

            if (mIsRecordingVideo) {
                mstop_video.visibility = View.VISIBLE
                Log.d("call mIsRecordingVideo", "mIsRecordingVideo")
                try {
                    if (isAudioMix) {
                        mPauseVideo = true
                        pauseMediaPlayer()
                        pauseTimer()
                        mIsRecordingVideo = false
                        gpuCameraRecorder?.stop()
                        mRecordVideo.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.play_icon
                            )
                        )
                    } else {
                        mPauseVideo = true
                        pauseTimer()
                        mIsRecordingVideo = false
                        gpuCameraRecorder?.stop()
                        mRecordVideo.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.play_icon
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (mPauseVideo) {
                Log.d("call mpausevideo", "mpausevideo")
                iv_audio_select.visibility = View.GONE
                imgTimer.visibility = View.GONE
                iv_audio_remove.visibility = View.GONE
                try {
                    if (isAudioMix) {
                        mPauseVideo = false
                        mResumeVideo = true
                        resumeMediaPlayer()
                        resumeTimer()
                        currentFile = outputMediaFile
                        mOutputFilePath = currentFile?.absolutePath
                        mIsRecordingVideo = true
                        Log.i("CameraVideoRecording", "File $mOutputFilePath")
                        gpuCameraRecorder?.start(mOutputFilePath)
                        mRecordVideo.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.pause_icon
                            )
                        )
                        mstop_video.visibility = View.GONE
                    } else {
                        mPauseVideo = false
                        mResumeVideo = true
                        resumeTimer()
                        currentFile = outputMediaFile
                        mOutputFilePath = currentFile?.absolutePath
                        mIsRecordingVideo = true
                        Log.i("CameraVideoRecording", "File $mOutputFilePath")
                        gpuCameraRecorder?.start(mOutputFilePath)
                        mRecordVideo.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.pause_icon
                            )
                        )
                        mstop_video.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (mResumeVideo) {
                iv_audio_select.visibility = View.VISIBLE
                iv_audio_remove.visibility = View.GONE
                Log.d("call mResumeVideo", "mResumeVideo")
                try {
                    mPauseVideo = true
                    mResumeVideo = false
                    pauseTimer()
                    mIsRecordingVideo = false
                    gpuCameraRecorder?.stop()
                    mRecordVideo.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.pause_icon
                        )
                    )
                    mstop_video.visibility = View.GONE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (isTimerSelected) {
                if (timeLengthMilliVideo != null) {
                    startTimerForVideo(timeLengthMilliVideo!!)
                    view.visibility = View.VISIBLE
                    disableAllButtons()
                }
            } else if (isTimerSelected && isAudioMix) {
                if (timeLengthMilliVideo != null) {
                    startTimerForVideo(timeLengthMilliVideo!!)
                    view.visibility = View.VISIBLE
                    disableAllButtons()
                }
            } else {
                iv_audio_select.visibility = View.GONE
                iv_audio_remove.visibility = View.GONE
                Log.d("call startrecoding", "startrecoding")
                try {
                    if (isAudioMix) {
                        i = 0
                        startTimer(timeLengthMilli!!)
                        var pathAudio: String = Constants.Audio_PATH
                        pathAudio += audioFileNameFinal
                        Logger.print("android mRecordVideo click !!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                        Logger.print("path_audio==========$pathAudio")
                        setUpMediaPlayer(pathAudio)
                        currentFile = outputMediaFile
                        mOutputFilePath = currentFile?.absolutePath
                        mIsRecordingVideo = true
                        Log.i("CameraVideoRecording", "File $mOutputFilePath")
                        gpuCameraRecorder?.start(mOutputFilePath)
                        mRecordVideo.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.pause_icon
                            )
                        )
                        mOutputFilePath = currentFile!!.absolutePath
                    } else {
                        i = 0
                        startTimer(timeLengthMilli!!)
                        currentFile = outputMediaFile
                        mOutputFilePath = currentFile?.absolutePath
                        mIsRecordingVideo = true
                        Log.i("CameraVideoRecording", "File $mOutputFilePath")
                        gpuCameraRecorder?.start(mOutputFilePath)
                        mRecordVideo.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                R.drawable.pause_icon
                            )
                        )
                        mOutputFilePath = currentFile!!.absolutePath
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        animationExit?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                tvShowDuration.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        animationExitTimer?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                tvTimerLength.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
    }

    private val durationRunnable = Runnable {
        tvShowDuration.startAnimation(animationExit)
    }
    private val durationRunnableTimer = Runnable {
        tvTimerLength.startAnimation(animationExitTimer)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i(TAG, "onActivityResult $data")
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == requestVideoTrimmer) {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null && data.data != null) {
                        try {
                            val selectedUri = data.data
                            if (selectedUri != null && !selectedUri.path?.contains("com.google.android.apps.photos")!!) {
                                startTrimActivity(selectedUri)
                            } else {
                                Toast.makeText(
                                    this@CameraVideoRecording,
                                    R.string.cannot_retrieve_video,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: RuntimeException) {
                            e.printStackTrace()
                        }

                    }
                } else {
                    requestPermission()
                }
            } else if (requestCode == requestTrimmerVideo) {
                if (resultCode == Activity.RESULT_OK) {
                    isResumeFromPreview = true
                    val intent = Intent(this@CameraVideoRecording, PreviewActivity::class.java)
                    intent.putExtra("audioFile", audioFileNameFinal)
                    intent.putExtra("IS_AUDIO_CAMERA", isAudioMix)
                    intent.putExtra("IS_MERGE", false)
                    intent.putExtra("extractAudio", extractAudio)
                    intent.putExtra("shareUri", "")
                    isMergeFile = false
                    intent.putExtra("IS_CAMERA_PREVIEW", false)
                    intent.putExtra("mStopVideo", mStopVideo)
                    intent.putExtra("videoRotation", videoRotation)
                    startActivity(intent)
                } else {
                    requestPermission()
                }
            } else if (requestCode == requestPreviewCamera) {
                if (resultCode == Activity.RESULT_OK) {
                    //showDialogForPostVideo(false)
                    startSelectionActivity(false)
                }
            } else if (requestCode == requestCreatePost) {
                if (resultCode == Activity.RESULT_OK) {
                    postTitle = data?.getStringExtra("postTitle")!!
                    selectedLangIDSFinal = data.getStringExtra("selectedLanguageId")!!
                    selectedCatIDSFinal = data.getStringExtra("selectedcategoryId")!!
                    isPickFromGallery = data.getBooleanExtra("isFromPickGallery", false)
                    showDialogForPostVideo(isPickFromGallery)
                }
            } else if (requestCode == requestVideoPlay) {
                if (resultCode == Activity.RESULT_OK) {
                    audioFileNameFinal = data?.getStringExtra("audioFileNameFinal")!!
                    isDownloadFinal = data.getStringExtra("isDownload")!!
                    Logger.print("final_Audio_File onresume==========$audioFileNameFinal")
                    isAudioMix = !audioFileNameFinal.contentEquals("")
                    startSelectionActivity(true)
                    //showDialogForPostVideo(true)
                } else {
                    requestPermission()
                }
            }
        }
    }

    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    @Throws(IOException::class)
    fun readBytes(dataPath: String?): ByteArray? {
        val inputStream: InputStream = FileInputStream(dataPath)
        val byteBuffer = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int = 0
        while (inputStream.read(buffer).also({ len = it }) != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    fun shareVideoFB(videoPath: String, accessTokenLogin: String) {
        val accessToken = AccessToken.getCurrentAccessToken()
        Logger.print("accessToken===============$accessToken")
        val fileUri: Uri = Uri.parse(videoPath)
        val fileName =
            videoPath.substring(videoPath.lastIndexOf('/') + 1, videoPath.length)
        //  val mimeType: String = this.getMimeType(videoPath)!!
        try {
            data = readBytes(videoPath)!!
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val videoSize: Int = data!!.size
        val privacy: String = this.getPrivacy("Friends Only")!!
        val request = GraphRequest.newPostRequest(
            accessToken,
            "https://graph-video.facebook.com/me/videos",
            null
        ) { response ->
            try {
                // if (pd.isShowing) pd.dismiss()
            } catch (e: Exception) {
            }
            Logger.print("response ====== ${response.toString()}")
            Logger.print("error response ====== ${response.error}")
            // onFBShareVideoCompleted(response)
        }
        var params = request.parameters
        params = request.parameters
        params.putByteArray(fileName, data)
        params.putString("title", "This is video you can check")
        params.putString("description", "Some Description...")

//        params.putString("upload_phase", "start");
        params.putInt("file_size", data!!.size)
        request.parameters = params
        request.executeAsync()
    }

    private fun getPrivacy(privacy: String): String? {
        var str: String
        if (privacy.equals("Everyone", ignoreCase = true)) str = "EVERYONE"
        str = if (privacy.equals(
                "Friends and Networks",
                ignoreCase = true
            )
        ) "NETWORKS_FRIENDS" else if (privacy.equals(
                "Friends of Friends",
                ignoreCase = true
            )
        ) "FRIENDS_OF_FRIENDS" else if (privacy.equals(
                "Friends Only",
                ignoreCase = true
            )
        ) "ALL_FRIENDS" else if (privacy.equals(
                "Custom",
                ignoreCase = true
            )
        ) "CUSTOM" else if (privacy.equals(
                "Specific People...",
                ignoreCase = true
            )
        ) "SOME_FRIENDS" else "SELF"
        return str
    }


    private fun disableIcons() {
        imgTimer.isEnabled = false
        imgTimer.setColorFilter(ContextCompat.getColor(this, R.color.tab_color))

        imgChangeDuration.isEnabled = false
        imgChangeDuration.setColorFilter(ContextCompat.getColor(this, R.color.tab_color))

        imgPickVideo.isEnabled = false
        imgPickVideo.setColorFilter(ContextCompat.getColor(this, R.color.tab_color))

        imgFilters.isEnabled = false
        imgFilters.setColorFilter(ContextCompat.getColor(this, R.color.tab_color))
    }

    private fun enableIcons() {
        imgTimer.isEnabled = true
        imgTimer.setColorFilter(ContextCompat.getColor(this, R.color.white))

        imgPickVideo.isEnabled = true
        imgPickVideo.setColorFilter(ContextCompat.getColor(this, R.color.white))

        imgFilters.isEnabled = true
        imgFilters.setColorFilter(ContextCompat.getColor(this, R.color.white))

        imgChangeDuration.isEnabled = true
        imgChangeDuration.setColorFilter(ContextCompat.getColor(this, R.color.white))
    }

    private fun startSelectionActivity(isFromPickGallery: Boolean) {
        val intent = Intent(this, StartNewPostActivity::class.java)
        intent.putExtra("isFromPickGallery", isFromPickGallery)
        startActivityForResult(intent, requestCreatePost)
    }

    private fun pickFromGallery() {
        MusicListingForYouAlbumActivity.final_Audio_File = ""
        MusicListingForYouAlbumActivity.is_Download = ""
        MusicListingForYouAlbumActivity.AUDIO_ID = ""

        iv_audio_remove.visibility = View.GONE

        isResumeFromPickGallery = true
        val intent: Intent =
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            } else {
                Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI)
            }
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_video)),
            requestVideoTrimmer
        )
    }

    private fun startTrimActivity(@NonNull uri: Uri) {
        isResumeFromPickGallery = true
        Logger.print("uri!!!!!!!!!!!!!!!!!${uri.path}")
        Log.i("CameraVideoRecording", "Uri ${uri.path}")

        val path = FileUtils.getPathFromUri(this@CameraVideoRecording, uri)
        Logger.print("path!!!!!!!!!!!$path")
        val file = File(path)
        file.setReadable(true)
        file.setWritable(true)
        if (file.exists()) {
            val sizeInBytes = file.length()
            val sizeInMb = sizeInBytes / (1024 * 1024)
            Logger.print("fileSize!!!!!!!!!!$sizeInMb")
            if (sizeInMb > 256) {
                Utils.showToastMessage(this, resources.getString(R.string.bigfilevalidation))
            } else {
                Logger.print("file path error !!!!!!!!${file.absolutePath}")
                val intent = Intent(this@CameraVideoRecording, TrimmerActivity::class.java)
                if (file.name.length > 30) {
                    Logger.print("file.name.length!!!!!!!!!!!${file.name.length}")
                    var currentFileName: String = path.substring(path.lastIndexOf("/"), path.length)
                    currentFileName = currentFileName.substring(1)

                    Logger.print("currentFileName after substring!!!!!!!!!!!!!!!!$currentFileName")

                    val directory = file.parent ?: Environment.getExternalStorageState()
                    Logger.print("directory!!!!!!!!!!!!!!$directory")
                    val from = File(directory, currentFileName)
                    Logger.print("exist or not !!!!!!${from.parentFile?.exists()}")
                    val to = File(directory, "Video_${System.currentTimeMillis()}_.mp4")
                    try {
                        if (rename(from, to)) {
                            Logger.print("renamed sucessfully!!!!!!!!!!!")
                        } else {
                            Logger.print("error in renamed!!!!!!!!!!!")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // from.renameTo(to)
                    Logger.print("to absolute path !!!!!!!!!!!${to.absolutePath}")
                    if (to.exists()) {
                        intent.putExtra(EXTRA_VIDEO_PATH, to.absolutePath)
                        Logger.print("currentFileName!!!!!!!!${to.absolutePath}")
                        intent.putExtra("trimSec", trimSec)
                        this@CameraVideoRecording.startActivityForResult(
                            intent,
                            requestTrimmerVideo
                        )
                    } else {
                        Utils.showToastMessage(this, resources.getString(R.string.filevalidation))
                    }
                } else {
                    intent.putExtra(
                        EXTRA_VIDEO_PATH,
                        FileUtils.getPathFromUri(this@CameraVideoRecording, uri)
                    )
                    intent.putExtra("trimSec", trimSec)
                    this@CameraVideoRecording.startActivityForResult(intent, requestTrimmerVideo)
                }
            }
        } else {
            Utils.showToastMessage(this, resources.getString(R.string.filevalidation))
            Logger.print("file path error not exist!!!!!!!!${file.absolutePath}")
        }
    }

    private fun rename(from: File, to: File): Boolean {
        return from.parentFile?.exists()!! && from.exists() && from.renameTo(to)
    }

    private fun stopMediaPlayer() {
        player.stop()
    }

    private fun pauseMediaPlayer() {
        player.pause()
        currentPlayerTime = player.currentPosition
    }

    private fun resumeMediaPlayer() {
        player.seekTo(currentPlayerTime)
        player.start()
    }

    private fun setUpMediaPlayer(path_audio: String) {
        try {
            player.setAudioStreamType(AudioManager.ADJUST_LOWER)
            player.reset()
            player.setDataSource(path_audio)
            player.isLooping = true
            player.prepare()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            println("Exception of type : $e")
            e.printStackTrace()
        }
        player.start()
    }

    override fun onResume() {
        super.onResume()
        progressbar.progress = 0
        Log.i(TAG, "isResumeFromGallery $isResumeFromPickGallery")
        when {
            isResumeFromPreview -> {
                isResumeFromPreview = false
                audioFileNameFinal = ""
                isDownloadFinal = ""
                finalAudioId = ""
                iv_audio_remove.visibility = View.GONE
                isAudioMix = false
                iv_audio_select.visibility = View.VISIBLE
                enableIcons()

                progressbar.progress = 0
                milliLeft = 0
                mRecordVideo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.round_camera))
                mIsRecordingVideo = false
                mIsFirstRecord = true
                mResumeVideo = false
                mPauseVideo = false
                isTimerSelected = false
                mstop_video.visibility = View.GONE
            }
            isResumeFromPickGallery -> {
                isResumeFromPickGallery = false
            }
            else -> {
                if (MusicListingForYouAlbumActivity.final_Audio_File != "") {
                    audioFileNameFinal = MusicListingForYouAlbumActivity.final_Audio_File
                }
                if (MusicListingForYouAlbumActivity.is_Download != "") {
                    isDownloadFinal = MusicListingForYouAlbumActivity.is_Download
                }
                if (MusicListingForYouAlbumActivity.AUDIO_ID != "") {
                    finalAudioId = MusicListingForYouAlbumActivity.AUDIO_ID
                }

                val audioDuration = MusicListingForYouAlbumActivity.audioDuration
                Logger.print("audioDuration *******************$audioDuration")
                Logger.print("final_Audio_File onresume==========$audioFileNameFinal")
                Log.i("CameraVideoRecording", "Audio Duration $audioDuration")

                if (!audioFileNameFinal.contentEquals("")) {
                    iv_audio_remove.visibility = View.VISIBLE
                    isAudioMix = true
                    if (audioDuration != 0L) {
                        timeLengthMilli = audioDuration
                        imgChangeDuration.isEnabled = false
                        imgChangeDuration.setColorFilter(ContextCompat.getColor(this, R.color.tab_color))
                    }
                } else {
                    iv_audio_remove.visibility = View.GONE
                    isAudioMix = false
                    imgChangeDuration.isEnabled = true
                    imgChangeDuration.setColorFilter(ContextCompat.getColor(this, R.color.white))
                }
                requestPermission()
            }
        }
    }

    override fun onBackPressed() {
        if (CameraThread.isFlashTorch) {
            gpuCameraRecorder?.flashOFF()
        }

        if (mIsRecordingVideo) {
            gpuCameraRecorder?.stop()
            stopTimer()
            stopMediaPlayer()
        }

        setResult(Activity.RESULT_OK)
        finish()
        super.onBackPressed()
    }

    override fun onDestroy() {
        MusicListingForYouAlbumActivity.final_Audio_File = ""
        MusicListingForYouAlbumActivity.is_Download = ""
        MusicListingForYouAlbumActivity.AUDIO_ID = ""
        super.onDestroy()
    }

    private fun showDialogForRemoveAudio(
        imgChangeDuration1: AppCompatImageView,
        iv_audio_remove1: AppCompatImageView
    ) {
        val alertDialog =
            object : CommonAlertDialog(this, theme = android.R.style.Theme_Translucent_NoTitleBar) {
                override fun okClicked() {
                    imgChangeDuration1.isEnabled = true
                    imgChangeDuration1.setColorFilter(
                        ContextCompat.getColor(
                            this@CameraVideoRecording,
                            R.color.white
                        )
                    )
                    iv_audio_remove1.visibility = View.GONE
                    audioFileNameFinal = ""
                    finalAudioId = ""
                    isAudioMix = false
                    MusicListingForYouAlbumActivity.is_Download = ""
                    MusicListingForYouAlbumActivity.final_Audio_File = ""
                    MusicListingForYouAlbumActivity.audioDuration = 0L
                    MusicListingForYouAlbumActivity.AUDIO_ID = ""

                }

                override fun cancelClicked() {}
            }
        alertDialog.initDialog(
            resources.getString(R.string.ok),
            resources.getString(R.string.remove_audio)
        )
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    private fun showDialogForPostVideo(isFromPickGallery: Boolean) {
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

        show.setOnDismissListener {
            setResult(Activity.RESULT_OK)
            this@CameraVideoRecording.finish()
            Log.i("CameraVideoRecording", "onDismiss Popup")
        }

        tvNo.setOnClickListener {
            show.dismiss()
        }

        tvYes.setOnClickListener {
            val serviceIntent = Intent(this, VideoUploadService::class.java)
            Logger.print("ISAUDIOMIX upload service=========$isAudioMix")
            serviceIntent.putExtra("audioFileName", audioFileNameFinal)
            serviceIntent.putExtra("finalAudioId", finalAudioId)
            serviceIntent.putExtra("isAudioMix", isAudioMix)
            serviceIntent.putExtra("IS_MERGE_FILE", isMergeFile)
            serviceIntent.putExtra("mStopVideo", mStopVideo)
            serviceIntent.putExtra("selectedCatIds", selectedCatIDSFinal)
            serviceIntent.putExtra("selectedLanguageIds", selectedLangIDSFinal)
            serviceIntent.putExtra("postTitle", postTitle)
            serviceIntent.putExtra("videoRotation", videoRotation)
            serviceIntent.putExtra("isFromPickGallery", isFromPickGallery)
            ContextCompat.startForegroundService(this, serviceIntent)
            show.dismiss()
        }
    }

    var speedApter: SpeedAdapter? = null

    private fun showDialogForSpeed(context: Context?, audioFiles: ArrayList<String>) {
        println("audioFiles in show dialog:::::::::::::::::: $audioFiles")
        val inflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(R.layout.audio_main_diaog, null)

        speedApter = SpeedAdapter(this, speedArray, object : SpeedAdapter.ManageClick {
            override fun manageRadioClick(IDp: String?) {
                IDS_speed.clear()
                IDS_speed.add(IDp!!)

                speedApter!!.notifyDataSetChanged()
                Log.d("ID OF AUDIO!!", IDS_speed.toString())
            }
        })

        val lvLeavePopup = dialogView.findViewById<View>(R.id.lv_leave_popup) as ListView
        val txtSaveAndPay: TextView = dialogView.findViewById<View>(R.id.txt_saveandpay) as TextView
        val tvCancel: TextView = dialogView.findViewById<View>(R.id.tv_cancel) as TextView
        lvLeavePopup.adapter = speedApter
        lvLeavePopup.choiceMode = ListView.CHOICE_MODE_SINGLE
        val builder = AlertDialog.Builder(context)

        val b = builder.create()
        builder.setTitle("Select Speed")
        builder.setCancelable(true)
        builder.setView(dialogView)
        b.setCanceledOnTouchOutside(true)
        b.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val show = builder.show()
        show.setCancelable(true)
        show.setCanceledOnTouchOutside(true)
        tvCancel.setOnClickListener { show.dismiss() }
        txtSaveAndPay.setOnClickListener {
            show.dismiss()
            var iddd: String = IDS_speed.toString()
            if (iddd.contains("[")) {
                iddd = iddd.replace("[", "")
            }
            if (iddd.contains("]")) {
                iddd = iddd.replace("]", "")
            }
            val id: Float = iddd.toFloat()
            mMediaRecorder!!.setCaptureRate(profile.videoFrameRate / (id).toDouble())
        }
    }

//    var timerAdapter: CountDownTimerAdapter? = null

    /**
     * Create directory and return file
     * returning video file
     */
    private val outputMediaFile: File?
        get() {
            val mediaStorageDir = File(
                Environment.getExternalStorageDirectory(),
                VIDEO_DIRECTORY_NAME + File.separator + VIDEO_RECORD_DIRECTORY_NAME
            )
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(
                        TAG,
                        "Oops! Failed create " + VIDEO_DIRECTORY_NAME + File.separator + VIDEO_RECORD_DIRECTORY_NAME + " directory"
                    )
                    return null
                }
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            return File(mediaStorageDir.path + File.separator + "VID_" + timeStamp + ".mp4")
        }

    private fun pauseTimer() {
        countDownTimer!!.cancel()
    }

    private fun resumeTimer() {
        startTimer(milliLeft)
    }

    private var countDownTimer: CountDownTimer? = null
    private var countDownTimerForVideo: CountDownTimer? = null

    private fun startTimer(timeLengthMilli: Long) {
        progressbar.progress = 0
        countDownTimer = object : CountDownTimer(timeLengthMilli, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                milliLeft = millisUntilFinished
                i++
                progressbar.progress = i * 100 / (timeLengthMilli / 1000).toInt()
            }

            override fun onFinish() {
                try {
                    i++
                    progressbar.progress = 100
                    mRecordVideo.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@CameraVideoRecording,
                            R.drawable.round_camera
                        )
                    )
                    mIsRecordingVideo = false
                    gpuCameraRecorder?.stop()
                    gpuCameraRecorder?.flashOFF()
                    if (CameraThread.isFlashTorch) {
                        m_flash_light.setImageResource(R.drawable.flash_on)
                    }
                    if (!CameraThread.isFlashTorch) {
                        m_flash_light.setImageResource(R.drawable.flash_off)
                    }
                    stopMediaPlayer()
                    combine()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun startTimerForVideo(timeLengthMillisssssssss: Long) {
        progressbar.progress = 0
        countDownTimerForVideo = object : CountDownTimer(timeLengthMillisssssssss, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsFinished = millisUntilFinished / 1000
                tvSeconds.visibility = View.VISIBLE
                tvSeconds.text = secondsFinished.toString()
                tvSeconds.startAnimation(animationEnter)
            }

            override fun onFinish() {
                try {
                    isTimerSelected = false
                    view.visibility = View.GONE
                    enableAllButtons()

                    timerList.clear()
                    tvSeconds.visibility = View.GONE
                    i = 0

                    if (isAudioMix) {
                        var pathAudio: String = Constants.Audio_PATH
                        pathAudio += audioFileNameFinal
                        Logger.print("path_audio==========$pathAudio")
                        setUpMediaPlayer(pathAudio)
                    }

                    startTimer(timeLengthMilli!!)
                    currentFile = outputMediaFile
                    mOutputFilePath = currentFile?.absolutePath
                    mIsRecordingVideo = true
                    Log.i("CameraVideoRecording", "File $mOutputFilePath")
                    gpuCameraRecorder?.start(mOutputFilePath)
                    mRecordVideo.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@CameraVideoRecording,
                            R.drawable.pause_icon
                        )
                    )
                    mOutputFilePath = currentFile!!.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun stopTimer() {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
        }
    }

    /**
     * Requesting permissions storage, audio and camera at once
     */
    private fun requestPermission() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted or not
                    if (report.areAllPermissionsGranted()) {
                        setUpCamera()
                    }
                    // check for permanent denial of any permission show alert dialog
                    if (report.isAnyPermissionPermanentlyDenied && !isSettingDialogShowing) {
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
                Toast.makeText(this.applicationContext, "Error occurred! ", Toast.LENGTH_SHORT)
                    .show()
            }.onSameThread().check()
    }

    override fun onPause() {
        super.onPause()
        Logger.print("CameraThread.isFlashTorch==============" + CameraThread.isFlashTorch)
        if (CameraThread.isFlashTorch) {
            gpuCameraRecorder?.switchFlashMode(m_flash_light)
            m_flash_light.setImageResource(R.drawable.flash_off)
        }
        lensFacing = LensFacing.BACK
        gpuCameraRecorder?.setCameraLens(lensFacing)


    }

    fun setUpCamera() {
        setUpCameraView()
        gpuCameraRecorder = GPUCameraRecorderBuilder(this, sampleGLView)
            .cameraRecordListener(object : CameraRecordListener {
                override fun onGetFlashSupport(flashSupport: Boolean) {
                    runOnUiThread {
                        isFlashSupported = flashSupport
                    }
                }

                override fun onRecordComplete() {
                    Log.i("CameraRecordListener", "onRecordCompleted")
                    val file = File(mOutputFilePath!!)
                    if (file.length() > 0) {
                        Log.i("CameraRecordListener", "onRecordCompleted file is not empty")
                    } else {
                        Log.i("CameraRecordListener", "onRecordCompleted file is empty")
                    }
                    exportMp4ToGallery(applicationContext, mOutputFilePath!!)
                }

                override fun onRecordStart() {
                    runOnUiThread {
                        rlFilters.visibility = View.GONE
                        Log.i("CameraRecordListener", "onRecordStart")
                    }
                }

                override fun onError(exception: java.lang.Exception) {
                    Log.i("CameraRecordListener", "onRecordError ${exception.printStackTrace()}")
                    Log.e("GPUCameraRecorder", exception.toString())
                }

                override fun onCameraThreadFinish() {
                    Log.i("CameraRecordListener", "onCameraThreadFinish")
                    if (toggleClick) {
                        runOnUiThread { setUpCamera() }
                    }
                    toggleClick = false
                }

                override fun onVideoFileReady() {
                    Log.i("CameraRecordListener", "onVideoFileReady")
                }
            })
            .videoSize(videoWidth, videoHeight)
            .cameraSize(cameraWidth, cameraHeight)
            .lensFacing(lensFacing)
            .build()
    }

    private fun setUpCameraView() {
        runOnUiThread {
            mTextureView.removeAllViews()
            sampleGLView = null
            sampleGLView = SampleCameraGLView(applicationContext)
            sampleGLView!!.setTouchListener { _, _, _ ->
                if (gpuCameraRecorder == null) return@setTouchListener
            }
            mTextureView.addView(sampleGLView)
        }
    }

    open fun exportMp4ToGallery(context: Context, filePath: String) {
        val values = ContentValues(2)
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        values.put(MediaStore.Video.Media.DATA, filePath)
        context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        context.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://$filePath")
            )
        )
    }

    /**
     * Showing Alert Dialog with Settings option in case of deny any permission
     */
    private fun showSettingsDialog() {
        isResumeFromPickGallery = true
        isSettingDialogShowing = true
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.message_need_permission))
        builder.setMessage(getString(R.string.message_permission))
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.title_go_to_setting)) { dialog, _ ->
            dialog.cancel()
            openSettings()
        }
        builder.setOnDismissListener {
            isSettingDialogShowing = false
        }
        builder.show()
    }

    // navigating settings app
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    companion object {
        var cameraVideoRecording: CameraVideoRecording? = null
        private const val TAG = "CameraFragment"
        const val EXTRA_VIDEO_PATH = "EXTRA_VIDEO_PATH"
        var IDS_speed = ArrayList<String>()
        var timerList = ArrayList<String>()
        private const val VIDEO_MERGE_DIRECTORY_NAME = "MERGED"

        private val INVERSE_ORIENTATIONS = SparseIntArray()
        private val DEFAULT_ORIENTATIONS = SparseIntArray()

        init {
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0)
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90)
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180)
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270)
        }

        init {
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0)
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90)
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180)
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270)
        }

        private const val VIDEO_DIRECTORY_NAME = "Tri-state"
        private const val VIDEO_RECORD_DIRECTORY_NAME = "RecordVideo"
    }

    override fun onFilterSelected(position: Int, filterType: FilterType) {
        gpuCameraRecorder?.setFilter(FilterType.createGlFilter(filterType, applicationContext))
    }

    private fun combine() {
        val timeStamp: String =
            java.lang.String.valueOf(java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
        Logger.print("START TIME COMBINE**********************$timeStamp")
        val result = ArrayList<String>() //ArrayList cause you don't know how many files there is
        val path: String = Environment.getExternalStorageDirectory()
            .toString() + File.separator + Constants.App_Folder + File.separator
        val pathRecord: String = Environment.getExternalStorageDirectory()
            .toString() + File.separator + Constants.App_Folder + File.separator + Constants.App_Folder_Record_Video + File.separator
        val mediaStorageDirMerged = File(path, VIDEO_MERGE_DIRECTORY_NAME)
        if (!mediaStorageDirMerged.exists()) {
            if (!mediaStorageDirMerged.mkdirs()) {
                Log.d(TAG, "Oops! Failed create $VIDEO_MERGE_DIRECTORY_NAME directory")
            }
        }

        val pathVideo = ArrayList<String>() //ArrayList cause you don't know how many files there is
        val folder = File(Constants.RECORD_PATH)
        if (folder.listFiles() != null && folder.listFiles()!!.isNotEmpty()) {
            if (folder.listFiles()!!.size > 1) {
                val filesInFolder =
                    folder.listFiles()!! // This returns all the folders and files in your path
                result.clear()
                for (file in filesInFolder) {
                    if (file.length() <= 0) {
                        file.delete()
                    } else {
                        result.add(file.name) //push the filename as a string
                        Log.d("File Name : ", file.name)
                    }
                }

                if (result.size == 0) {
                    Utils.showToastMessage(
                        applicationContext,
                        resources.getString(R.string.video_files_empty)
                    )
                    return
                }
                result.sort()

                print("file names !!!!!$result")
                Log.d("File Names", result.toString())
                var finalPath: String? = null
                val folderVideos = File(pathRecord)
                if (!folderVideos.exists()) {
                    folder.mkdirs()
                } else {
                    for (i in result.indices) { //For each of the entries do:
                        finalPath = pathRecord + result[i]
                        pathVideo.add(pathRecord + result[i])
                    }
                    print("Final Path::$finalPath")
                    Log.d("Final Path Names", pathVideo.toString())

                    val timeStamp =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val mergedPath =
                        path + VIDEO_MERGE_DIRECTORY_NAME + File.separator + "merged_" + timeStamp + ".mp4"
                    startVideoCombiner(pathVideo, mergedPath, mCombineListener)
                }
            } else {
                if (folder.listFiles()!!.size == 1) {
                    val filesInFolder = folder.listFiles()!!
                    if (filesInFolder[0].length() <= 0) {
                        filesInFolder[0].delete()
                        Utils.showToastMessage(
                            applicationContext,
                            resources.getString(R.string.video_files_empty)
                        )
                    } else {
                        isResumeFromPreview = true
                        val intent = Intent(this@CameraVideoRecording, PreviewActivity::class.java)
                        intent.putExtra("audioFile", audioFileNameFinal)
                        intent.putExtra("IS_AUDIO_CAMERA", isAudioMix)
                        intent.putExtra("IS_MERGE", false)
                        intent.putExtra("extractAudio", extractAudio)
                        intent.putExtra("shareUri", "")
                        isMergeFile = false
                        intent.putExtra("IS_CAMERA_PREVIEW", true)
                        intent.putExtra("mStopVideo", mStopVideo)
                        intent.putExtra("videoRotation", videoRotation)
                        startActivity(intent)
                    }
                }
            }
        } else {
            Utils.showToastMessage(
                applicationContext,
                resources.getString(R.string.no_video_file_found)
            )
        }
    }

    private fun disableAllButtons() {
        m_flash_light.isEnabled = false
        mcamera_rear_front.isEnabled = false
        imgFilters.isEnabled = false
        imgTimer.isEnabled = false
        imgChangeDuration.isEnabled = false
        imgPickVideo.isEnabled = false
        iv_audio_select.isEnabled = false
        iv_audio_remove.isEnabled = false
        iv_audio_remove.isEnabled = false
        mRecordVideo.isEnabled = false
    }

    fun enableAllButtons() {
        m_flash_light.isEnabled = true
        mcamera_rear_front.isEnabled = true
        imgFilters.isEnabled = true
        imgTimer.isEnabled = true
        imgChangeDuration.isEnabled = true
        imgPickVideo.isEnabled = true
        iv_audio_select.isEnabled = true
        iv_audio_remove.isEnabled = true
        iv_audio_remove.isEnabled = true
        mRecordVideo.isEnabled = true
    }

    private fun startVideoCombiner(
        pathVideo: ArrayList<String>,
        path: String,
        listener: VideoCombiner.VideoCombineListener?
    ) {
        Thread(Runnable {
            Log.d("PathVideoList Size : ", "" + pathVideo.size)
            Log.d("Destination path : ", "" + path)
            val videoCombiner = VideoCombiner(pathVideo, path, listener)
            videoCombiner.setCallback(this)
            videoCombiner.combineVideo()
        }).start()
    }

    override fun onSuccess(sucess: Boolean) {
        isMergeFile = true
        isResumeFromPreview = true
        val intent = Intent(this@CameraVideoRecording, PreviewActivity::class.java)
        intent.putExtra("audioFile", audioFileNameFinal)
        intent.putExtra("shareUri", "")
        intent.putExtra("IS_AUDIO_CAMERA", isAudioMix)
        intent.putExtra("IS_MERGE", true)
        intent.putExtra("extractAudio", extractAudio)
        intent.putExtra("IS_CAMERA_PREVIEW", true)
        intent.putExtra("mStopVideo", mStopVideo)
        intent.putExtra("videoRotation", videoRotation)
        startActivity(intent)
    }
}