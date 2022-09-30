package smylee.app.utils

import android.util.Log

//singlton class
object Logger {
    val isLogEnable = true
    fun print(Tag: String, value: String) {
        if (isLogEnable) {
            Log.d(Tag, value)
        }
    }

    fun print(value: String) {
        if (isLogEnable) {
            Log.d("Video Sharing App >>", value)
        }
    }

}
