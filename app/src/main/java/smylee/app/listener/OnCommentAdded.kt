package smylee.app.listener

import androidx.appcompat.widget.AppCompatTextView

interface OnCommentAdded {
    fun onCommentAdded(postId: Int, tvCommnetCountBottomSheet: AppCompatTextView)
    fun onDialogDismiss()
    fun onSpamComment(commentId: String)
    fun onSelfProfileClick()
}