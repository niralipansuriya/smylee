package smylee.app.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

class FlowLayout : ViewGroup {

    private var line_height_space = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    companion object
    class LayoutParams : ViewGroup.LayoutParams {
        var horizontalSpacing: Int = 0
        var verticalSpacing: Int = 0

        constructor(horizontal_spacing: Int, vertical_spacing: Int) : super(0, 0) {
            horizontalSpacing = horizontal_spacing
            verticalSpacing = vertical_spacing
        }

        public class LayoutParams
        /**
         * @param horizontal_spacing Pixels between items, horizontally
         * @param vertical_spacing   Pixels between items, vertically
         */(var horizontal_spacing: Int, var vertical_spacing: Int) :
            ViewGroup.LayoutParams(0, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        assert(MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED)

        val width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        var height = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom
        val count = childCount
        var line_height_space = 0

        var xpos = paddingLeft
        var ypos = paddingTop

        val childHeightMeasureSpec: Int
        childHeightMeasureSpec =
            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
                MeasureSpec.makeMeasureSpec(
                    height,
                    MeasureSpec.AT_MOST
                )
            } else {
                MeasureSpec.makeMeasureSpec(
                    0,
                    MeasureSpec.UNSPECIFIED
                )
            }


        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val lp =
                    child.layoutParams as LayoutParams
                child.measure(
                    MeasureSpec.makeMeasureSpec(
                        width,
                        MeasureSpec.AT_MOST
                    ), childHeightMeasureSpec
                )
                val childw = child.measuredWidth
                line_height_space = Math.max(
                    line_height_space,
                    child.measuredHeight + lp.verticalSpacing
                )
                if (xpos + childw > width) {
                    xpos = paddingLeft
                    ypos += line_height_space
                }
                xpos += childw + lp.horizontalSpacing
            }
        }
        this.line_height_space = line_height_space

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = ypos + line_height_space
        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            if (ypos + line_height_space < height) {
                height = ypos + line_height_space
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(1, 1)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        if (p is LayoutParams) {
            return true
        }
        return false
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val width = r - l
        var xpos = paddingLeft
        var ypos = paddingTop

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility !== View.GONE) {
                val childw = child.measuredWidth
                val childh = child.measuredHeight
                val lp = child.layoutParams as LayoutParams
                if (xpos + childw > width) {
                    xpos = paddingLeft
                    ypos += line_height_space
                }
                child.layout(xpos, ypos, xpos + childw, ypos + childh)
                xpos += childw + lp.horizontalSpacing
            }
        }
    }
}