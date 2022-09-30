package smylee.app.ui.Fragment

import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.danikula.videocache.HttpProxyCacheServer
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.item_viewpager.*
import org.json.JSONObject
import smylee.app.CallBacks.DoubleTapCallBacks
import smylee.app.R
import smylee.app.URFeedApplication
import smylee.app.custom.DoubleClickListener
import smylee.app.home.HomeViewModel
import smylee.app.listener.OnVideoPlayingListener
import smylee.app.model.ForYouResponse
import smylee.app.model.VideoDetailResponse
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Logger
import smylee.app.utils.Utils

class PlayerFragment : Fragment() {

    companion object {
        fun newInstance(
            position: Int,
            topic: ForYouResponse?,
            onVideoPlayingListener: OnVideoPlayingListener,
            doubleTapCallBacks: DoubleTapCallBacks
        ): PlayerFragment {
            val args = Bundle()
            val fragment = PlayerFragment()
            fragment.arguments = args.apply {
                putInt("position", position)
                putParcelable("topic", topic)
            }

            fragment.setOnPlayingListener(onVideoPlayingListener)
            fragment.setCallback(doubleTapCallBacks)
            return fragment
        }

        /*const val MIN_BUFFER_DURATION = 1000
        //Max Video you want to buffer during PlayBack
        const val MAX_BUFFER_DURATION = 3000
        //Min Video you want to buffer before start Playing it
        const val MIN_PLAYBACK_START_BUFFER = 1000
        //Min video You want to buffer when user resumes video
        const val MIN_PLAYBACK_RESUME_BUFFER = 1000*/

        //        private var dataSourceFactory: DataSource.Factory? = null
        private var cacheDataSourceFactory: CacheDataSourceFactory? = null
//        private var cacheDataSource: CacheDataSource.Factory? = null
    }

    var position: Int = 0

    //    var currentPlayingPos: Int = 0
    private var playingPosition: Int = 0
    var topic: ForYouResponse? = null
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private lateinit var videoDetailResponse: VideoDetailResponse
    lateinit var viewModel: HomeViewModel
    lateinit var activity: BaseActivity
    private var onVideoPlayingListener: OnVideoPlayingListener? = null
    private var doubleTapCallBacks: DoubleTapCallBacks? = null
    private var isPauseFromCode = false
    private var isCallFromException = false
    var width = 0
    var height = 0
    private lateinit var handler: Handler
    private var centerX = 0.0f
    private var centerY = 0.0f

    private var isPlayCalled = false
    private var isSetVolume = false
    private var isListenerCalled = false
    private var proxy: HttpProxyCacheServer? = null
    private var currentVolume = 0.0f

    /*private val loadControl: LoadControl = DefaultLoadControl.Builder()
        .setAllocator(DefaultAllocator(true, 10))
        .setBufferDurationsMs(
            MIN_BUFFER_DURATION,
            MAX_BUFFER_DURATION,
            MIN_PLAYBACK_START_BUFFER,
            MIN_PLAYBACK_RESUME_BUFFER
        )
        .setTargetBufferBytes(-1)
        .setPrioritizeTimeOverSizeThresholds(true)
        .build()*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt("position", 0)!!
        Logger.print("PlayerFragment onCreate $position")
        topic = arguments?.getParcelable("topic")
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

        height = displayMetrics.heightPixels
        width = displayMetrics.widthPixels

        val viewRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        centerX = viewRect.centerX()
        centerY = viewRect.centerY()
        proxy = BaseActivity.getProxy(activity)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.item_viewpager, container, false)
        if (topic?.post_video_thumbnail != null) {
            playerView?.player = simpleExoPlayer
        }



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        simpleExoPlayer = activity.let {
            val builder = SimpleExoPlayer.Builder(activity)
            builder.build()
        }
        playerView?.player = simpleExoPlayer

        simpleExoPlayer?.seekTo(0, 0)
        simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_ONE


        initExoPlayer()
        viewHandleClick?.setOnClickListener(
            object : DoubleClickListener() {
                override fun onSingleClick(v: View?) {
                    try {
                        doubleTapCallBacks!!.onDoubleTAP(1)
                        if (isPauseFromCode) {
                            isPauseFromCode = false
                            imgPlayVideo?.visibility = View.GONE
                            simpleExoPlayer?.playWhenReady = true
                        } else {
                            isPauseFromCode = true
                            imgPlayVideo?.visibility = View.VISIBLE
                            simpleExoPlayer?.playWhenReady = !simpleExoPlayer!!.isPlaying
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                override fun onDoubleClick(v: View?) {
                    doubleTapCallBacks!!.onDoubleTAP(2)
                }
            })
    }

    private fun createExoPlayer() {
        simpleExoPlayer = activity.let {
            val builder = SimpleExoPlayer.Builder(activity)
            builder.build()
        }
        playerView?.player = simpleExoPlayer

        simpleExoPlayer?.seekTo(0, 0)
        simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
    }

    private fun initExoPlayer() {
        if (cacheDataSourceFactory == null) {
            cacheDataSourceFactory = CacheDataSourceFactory(
                URFeedApplication.simpleCache!!,
                DefaultHttpDataSourceFactory(Util.getUserAgent(activity, "smylee"))
            )
        }


        if (topic?.post_video != null) {

            val mediaItem = MediaItem.fromUri(topic?.post_video!!)
            //   val mediaItem = MediaItem.fromUri(urlVideo)
//            simpleExoPlayer?.setMediaSource(ProgressiveMediaSource.Factory(cacheDataSource!!).createMediaSource(mediaItem))
            simpleExoPlayer?.setMediaSource(
                ProgressiveMediaSource.Factory(cacheDataSourceFactory!!)
                    .createMediaSource(mediaItem)
            )
            simpleExoPlayer?.prepare()
            simpleExoPlayer?.addListener(listener)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as BaseActivity
    }

    fun setVolumeOfVideo() {
        Logger.print("Going to set volume $currentVolume at position $position")
        if (simpleExoPlayer != null && !simpleExoPlayer?.isPlaying!! && !isPlayCalled) {
            playVideo(true)
        } else if (simpleExoPlayer != null && simpleExoPlayer?.isPlaying!!) {
            if (onVideoPlayingListener != null) {
                onVideoPlayingListener!!.onVideoStartPlaying(position)
            }
            simpleExoPlayer?.volume = 1.0f
        } else {
            isSetVolume = true
        }
    }

    fun playVideo(isInitExoPlayer: Boolean) {
        playingPosition = position
        isListenerCalled = false
        Logger.print("playVideo $position")
        if (playerView != null) {
            if (topic?.post_height != null && topic?.post_width != null) {
                if (topic?.post_height ?: 0 > topic?.post_width ?: 0) {
                    val ration = topic?.post_height!!.toFloat() / topic?.post_width!!.toFloat()
                    if (ration > 1.5f) {
                        val displayMetrics = DisplayMetrics()
                        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                        val heightDisplay: Float = displayMetrics.heightPixels.toFloat()
                        val widthDisplay: Float = displayMetrics.widthPixels.toFloat()
                        val displayRation = heightDisplay / widthDisplay
                        if (ration > displayRation) {
                            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                        } else {
                            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                        }
                    } else {
                        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    }
                } else if (topic?.post_height ?: 0 == topic?.post_width ?: 0) {
                    playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                } else {
                    playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                }
            }

        } else {
            Logger.print("playVideo playerView is null")
        }
        initPlayer(isInitExoPlayer)
    }

    fun setCallback(doubleTapCallBacks: DoubleTapCallBacks) {
        this.doubleTapCallBacks = doubleTapCallBacks
    }

    fun setOnPlayingListener(onVideoPlayingListener: OnVideoPlayingListener) {
        this.onVideoPlayingListener = onVideoPlayingListener
    }

    private fun initPlayer(isEnableVolume: Boolean) {
        if (::activity.isInitialized) {
            isPlayCalled = true
            simpleExoPlayer?.playWhenReady = true
            if (isEnableVolume) {
                if (onVideoPlayingListener != null) {
                    onVideoPlayingListener!!.onVideoStartPlaying(position)
                }
                simpleExoPlayer?.volume = 1.0f
            } else {
                simpleExoPlayer?.volume = 0.0f
            }
        } else {
            Logger.print("initPlayer activity not initialized")
        }
    }


    fun retryPlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer?.retry()

            Logger.print("retryPlayer=================")
        }
    }

    var listener = object : Player.EventListener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                if (playingPosition == -1) {
                    simpleExoPlayer?.playWhenReady = false
                } else {
                    isCallFromException = false
                    if (isSetVolume) {
                        isSetVolume = false
                        if (onVideoPlayingListener != null) {
                            onVideoPlayingListener!!.onVideoStartPlaying(position)
                        }
                        simpleExoPlayer?.volume = 1.0f
                    }
                    imgPlayVideo?.visibility = View.GONE
                    Logger.print("PlayerFragment > isPlaying at $position")
                    if (!isListenerCalled) {
                        isListenerCalled = true
                        if (!topic!!.isViewed) {
                            Handler().postDelayed({
                                videoDetails(topic!!.post_id)
                            }, 1000)
                        }
                    }
                }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            Log.i("PlayerFragment", "onPlayerError ${error.message}")
            Log.i("PlayerFragment", topic?.post_video!!)
            error.printStackTrace()
            if (!isCallFromException && playingPosition != -1) {
                isCallFromException = true
                releasePlayer()
//                cacheDataSource = null
                cacheDataSourceFactory = null
                if (::handler.isInitialized) {
                    handler.removeCallbacks(reInitRunnable)
                }
                System.gc()
                handler = Handler()
                handler.postDelayed(reInitRunnable, 1000)
                //retryPlayer()
            }
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    Logger.print("== state STATE_BUFFERING")
                    showLoadingGif(true)

                    //   ivVideoImage?.visibility = View.VISIBLE


                }
                Player.STATE_READY -> {

                    //  ivVideoImage?.visibility = View.VISIBLE


                    Logger.print("== state STATE_READY")
                    showLoadingGif(false)
                }
                Player.STATE_ENDED -> {
                    Logger.print("== state STATE_ENDED")
                }
                Player.STATE_IDLE -> {
                    Logger.print("== state STATE_IDLE")
                }
            }
        }
    }

    private val reInitRunnable: Runnable = Runnable {
        if (playingPosition != -1) {
            createExoPlayer()
            initExoPlayer()
            playVideo(true)
        }
    }

    private fun videoDetails(post_id: Int?) {
        Logger.print("post_id=======" + post_id.toString())
        val hashMap = HashMap<String, String>()
        hashMap["post_id"] = post_id.toString()

        viewModel.getVideoDetails(activity, hashMap, false).observe(this, Observer {
            if (it != null) {
//                Logger.print("VIDEO DETAILS ======$it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("data")) {
                        videoDetailResponse =
                            Gson().fromJson(jsonObject.getString("data").toString(), object :
                                TypeToken<VideoDetailResponse>() {}.type)

                        if (!videoDetailResponse.post_view_count.contentEquals("")) {
                            onVideoPlayingListener!!.onVideoViewCountUpdate(
                                position,
                                videoDetailResponse.post_view_count
                            )
                        }
                    }
                } else if (code == 0) {
                    Utils.showAlert(activity, "", jsonObject["message"].toString())
                }
            }
        })
    }

    private fun showLoadingGif(isShow: Boolean) {
        if (gifViewLoader != null) {
            gifViewLoader.visibility = if (isShow) View.VISIBLE else View.GONE
        }
    }

    override fun onDetach() {
        Logger.print("PlayerFragment > onDetach $position")
        super.onDetach()
    }

    override fun onPause() {
        Logger.print("PlayerFragment > onPause $position")
        super.onPause()
    }

    override fun onStop() {
        Logger.print("PlayerFragment > onStop $position")
        stopPlayer()
        super.onStop()
    }

    override fun onDestroy() {
        Logger.print("PlayerFragment > onDestroy $position")
        simpleExoPlayer?.removeListener(listener)
        simpleExoPlayer?.stop(true)
        simpleExoPlayer?.release()
        super.onDestroy()
    }

    fun releasePlayer() {
        Logger.print("PlayerFragment > releasePlayer")
        isPlayCalled = false
        playingPosition = -1
        simpleExoPlayer?.playWhenReady = false
        simpleExoPlayer?.removeListener(listener)
        simpleExoPlayer?.stop(true)
        simpleExoPlayer?.release()
    }

    fun stopPlayer() {
        isPlayCalled = false
        isSetVolume = false
        playingPosition = -1
        Logger.print("PlayerFragment > stopPlayer $position")
        simpleExoPlayer?.playWhenReady = false
    }
}