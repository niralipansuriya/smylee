package smylee.app.listener

interface OnVideoPlayingListener {
    fun onVideoStartPlaying(position: Int)

    fun onVideoViewCountUpdate(position: Int, videoViewCount: String)
}