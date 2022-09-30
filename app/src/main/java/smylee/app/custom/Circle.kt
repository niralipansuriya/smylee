package smylee.app.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import smylee.app.R
import uz.jamshid.library.IGRefreshLayout
import uz.jamshid.library.progress_bar.BaseProgressBar


class Circle @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseProgressBar(context, attrs, defStyleAttr) {

    var bar = ImageView(context)

    init {

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.setMargins(0, -10, 0, 0)
        // bar.layoutParams =layoutParams
        bar.setPadding(0, 30, 0, 0)
        bar.setBackgroundColor(ContextCompat.getColor(context, R.color.black))
        Glide.with(context)
            .asGif()
            .load(R.raw.emoji)
            .into(bar)

        //bar.setImageResource(R.drawable.ic_smile)
        //bar.alpha = 0f
    }

    override fun setParent(parent: IGRefreshLayout) {
        mParent = parent
        setUpView()
    }

    override fun setPercent(percent: Float) {
//        mPercent = percent
//        bar.alpha = percent/100
    }

    private fun setUpView() {
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.setMargins(0, -40, 0, 0)
        //   mParent.layoutParams=layoutParams
        mParent.setCustomView(bar, dp2px(80), dp2px(80))

        mParent.setBackgroundColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun start() {
//        val animation1 = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//        animation1.duration = 500
//        animation1.repeatCount = Animation.INFINITE
//        bar.startAnimation(animation1)
    }

    override fun stop() {
//        bar.alpha = 0f
//        bar.clearAnimation()
    }
}