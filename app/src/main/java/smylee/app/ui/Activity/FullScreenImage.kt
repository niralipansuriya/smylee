package smylee.app.ui.Activity

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_full_screen_image.*
import smylee.app.R
import smylee.app.ui.base.BaseActivity


class FullScreenImage : BaseActivity(), View.OnClickListener, View.OnTouchListener {

    private var imageUrl: String = ""
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        imageUrl = intent.getStringExtra("imageUrl")!!
        smylee.app.utils.Logger.print("imageUrl!!$imageUrl")

        Glide.with(this).load(imageUrl).into(imgZoom)

        imgBack.setOnClickListener(this)
        imgZoom.setOnTouchListener(this)
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.imgBack) {
            finish()
        }
    }

    private inner class ScaleListener :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            mScaleFactor *= scaleGestureDetector.scaleFactor
            mScaleFactor = 0.1f.coerceAtLeast(mScaleFactor.coerceAtMost(10.0f))
            imgZoom.scaleX = mScaleFactor
            imgZoom.scaleY = mScaleFactor
            return true
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        scaleGestureDetector?.onTouchEvent(event)
        return true
    }
}