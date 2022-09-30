package smylee.app.listener

import android.graphics.Bitmap

interface GifCompeleteListner {
    fun onComplete(sucess: Boolean, bitmap: Bitmap, video_file: String?, videoDuration: Long)
}