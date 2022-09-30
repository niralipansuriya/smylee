package smylee.app.model

data class CommentResponse(
    val comment_like_count: String?,
    val comment_report_count: String?,
    val created_date: Long?,
    val has_liked: Int?,
    val post_comment_id: Int?,
    val mark_as_verified_badge: Int?,
    val profile_name: String?,
    val user_id: String?,
    val profile_pic: String?,
    val user_comment: String?
)

