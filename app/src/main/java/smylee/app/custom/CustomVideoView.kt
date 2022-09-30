package smylee.app.custom

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.widget.VideoView
import java.util.logging.Logger


class CustomVideoView : VideoView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )


    private var mListener: PlayPauseListener? = null

    fun setPlayPauseListener(listener: PlayPauseListener) {
        mListener = listener
    }

    override fun pause() {
        super.pause()
        mListener?.onPause()
    }

//    override fun resume() {
//        super.resume()
//        mListener?.on
//
//    }


//    override fun setOnCompletionListener(l: MediaPlayer.OnCompletionListener?) {
//
//        l?.onCompletion(mp : MediaPlayer())
//        super.setOnCompletionListener(l)
//
//    }


    override fun start() {
        super.start()
        mListener?.onPlay()
    }

    interface PlayPauseListener {
        fun onPlay()
        fun onPause()
        fun onComPlete()
    }
}