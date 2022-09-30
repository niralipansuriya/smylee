package smylee.app.engine

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridLayoutSpacing : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        if (parent.getChildLayoutPosition(view) == 0 || parent.getChildLayoutPosition(view) % 3 == 0) {
            outRect.top = 10
        } else {
            outRect.left = 10
            outRect.top = 10
        }
    }
}