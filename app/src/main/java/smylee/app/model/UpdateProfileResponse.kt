package smylee.app.model

data class UpdateProfileResponse(

    val blocked_user_id: String?,
    val category_list: String,
    val date_of_birth: String,
    val email_id: String?,
    val first_name: String,
    val gender: Int,
    val is_blocked: Int,
    val is_verified: Int,
    val language: String,
    val last_name: String,
    val mark_as_verified_badge: Int,
    val no_of_followers: String?,
    val no_of_followings: String?,
    val facebook_url: String?,
    val instagram_url: String?,
    val youtube_url: String?,
    val profile_pic: String,
    val profile_pic_compres: String,
    val registered_by: Int,
    val user_id: String,
    val user_register_status: Int,
    val allow_notify: Int

)

