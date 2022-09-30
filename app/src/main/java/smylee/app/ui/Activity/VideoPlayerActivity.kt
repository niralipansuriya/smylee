package smylee.app.ui.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.item_viewpager.*
import smylee.app.MusicListing.MusicListingForYouAlbumActivity
import smylee.app.R
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Logger

class VideoPlayerActivity : BaseActivity(), View.OnClickListener {

    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var dataSourceFactory: DataSource.Factory? = null
    var filePath: String = ""

    var audioFileNameFinal: String = ""
    private var isDownloadFinal: String = ""
    private var ISAUDIOMIX: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
//        supportActionBar!!.hide()

        filePath = intent.getStringExtra("filePath")!!
        playVideo()
        ivAudioSelect.setOnClickListener(this)
        tvDonePreview.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        audioFileNameFinal = MusicListingForYouAlbumActivity.final_Audio_File
        isDownloadFinal = MusicListingForYouAlbumActivity.is_Download

        Logger.print("final_Audio_File onresume==========$audioFileNameFinal")

        if (!audioFileNameFinal.contentEquals("")) {
            ivAudioRemove.visibility = View.VISIBLE
            ISAUDIOMIX = true
        } else {
            ivAudioRemove.visibility = View.GONE
            ISAUDIOMIX = false
        }
    }

    private fun playVideo() {
        Handler().postDelayed({
            initPlayer()
        }, 100)
    }

    private fun initPlayer() {
        if (simpleExoPlayer == null) {
            simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
        }
        if (dataSourceFactory == null) {
            dataSourceFactory = DefaultDataSourceFactory(this, "exoplayer-tristate")
        }
        playerView?.player = simpleExoPlayer
        simpleExoPlayer?.playWhenReady = true
        simpleExoPlayer?.seekTo(0, 0)

//        val file = File(filePath)
        val mediaItem = MediaItem.fromUri(filePath)
        simpleExoPlayer?.setMediaSource(
            ProgressiveMediaSource.Factory(dataSourceFactory!!).createMediaSource(mediaItem)
        )
        simpleExoPlayer?.prepare()
        simpleExoPlayer?.addListener(listener)
    }

    var listener = object : Player.EventListener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            ivVideoImage?.visibility = if (isPlaying) View.GONE else View.VISIBLE
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            error.printStackTrace()
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("audioFileNameFinal", audioFileNameFinal)
        intent.putExtra("isDownload", isDownloadFinal)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivAudioSelect -> {
                val intent = Intent(this, MusicListingForYouAlbumActivity::class.java)
                this.startActivity(intent)
            }
            R.id.tvDonePreview -> {
                val intent = Intent()
                intent.putExtra("audioFileNameFinal", audioFileNameFinal)
                intent.putExtra("isDownload", isDownloadFinal)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onPause() {
        simpleExoPlayer?.stop()
        simpleExoPlayer?.release()
        super.onPause()
    }
}