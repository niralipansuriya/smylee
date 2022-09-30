package smylee.app.model

data class VideoDetailResponse
    (
    val user_id: String,
    val post_title: String,
    val post_video: String,
    val post_video_thumbnail: String,
    val post_view_count: String,
    val post_comment_count: String,
    val post_like_count: String,
    val post_spam_count: String,
    val post_video_duration: String,
    val category_color_code: String,
    val created_date: String,
    val profile_name: String,
    val profile_pic: String,
    val has_liked: String


)