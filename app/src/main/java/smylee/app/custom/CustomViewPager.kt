package smylee.app.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import smylee.app.discovercategory.ExploreFragment
import smylee.app.utils.Logger


class CustomViewPager : ViewPager {
    private var isPagingEnabled = true

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Logger.print("onTouchEvent !!!!!!!!!!!!!!!!!!!!!!!")

        return isPagingEnabled && super.onTouchEvent(event)

    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        Logger.print("onInterceptTouchEvent !!!!!!!!!!!!!!!!!!!!!!!")


        return isPagingEnabled && super.onInterceptTouchEvent(event)
    }


    fun setPagingEnabled(b: Boolean) {
        isPagingEnabled = b
    }
}