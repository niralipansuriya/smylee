package smylee.app.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Movie
import android.util.AttributeSet
import android.view.View
import smylee.app.R
import java.io.InputStream

class LoginGif : View {
    private var gifInputStream: InputStream? = null
    private var gifMovie: Movie? = null
    private var movieWidth = 0
    private var movieHeight: Int = 0
    private var movieDuration: Long = 0
    private var mMovieStart: Long = 0

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        isFocusable = true
        gifInputStream = context.resources.openRawResource(R.raw.splash_gif)
        gifMovie = Movie.decodeStream(gifInputStream)
        movieWidth = gifMovie!!.width()
        movieHeight = gifMovie!!.height()
        movieDuration = gifMovie!!.duration().toLong()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(movieWidth, movieHeight);
    }

    fun getMovieWidth(): Int {
        return movieWidth
    }

    fun getMovieHeight(): Int {
        return movieHeight
    }

    fun getMovieDuration(): Long {
        return movieDuration
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val now = android.os.SystemClock.uptimeMillis()
        if (mMovieStart == 0L) { // first time
            mMovieStart = now
        }

        if (gifMovie != null) {
            var dur = gifMovie!!.duration()
            if (dur == 0) {
                dur = 1000
            }
            var relTime: Long = now - mMovieStart
            relTime %= dur
            gifMovie!!.setTime(relTime.toInt())
            gifMovie!!.draw(canvas, 0f, 0f)
            invalidate()
        }
    }
}