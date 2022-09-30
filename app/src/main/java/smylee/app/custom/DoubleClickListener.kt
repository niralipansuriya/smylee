package smylee.app.custom

import android.view.View
import smylee.app.utils.Logger

abstract class DoubleClickListener : View.OnClickListener {
    var lastClickTime: Long = 0
    var tap = true

    override fun onClick(v: View?) {
        // val clickTime = System.currentTimeMillis()
        val clickTime = System.currentTimeMillis()


        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {

            onDoubleClick(v)
            lastClickTime = 0
            tap = false


        } else {

            tap = true

            v!!.postDelayed({ if (tap) onSingleClick(v) }, DELAY)

            /*if (tap)
            {
                onSingleClick(v)

            }*/

        }

        lastClickTime = clickTime

    }

    abstract fun onSingleClick(v: View?)
    abstract fun onDoubleClick(v: View?)

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
        private const val DELAY: Long = 300 //milliseconds
    }
}
