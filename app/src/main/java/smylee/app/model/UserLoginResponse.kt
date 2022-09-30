package smylee.app.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class UserLoginResponse {

    @SerializedName("is_blocked")
    @Expose
    private var is_blocked: Int? = null

    @SerializedName("is_verified")
    @Expose
    private var is_verified: Int? = null

    @SerializedName("no_of_followers")
    @Expose
    private var no_of_followers: String? = null

    @SerializedName("no_of_followings")
    @Expose
    private var no_of_followings: String? = null

    @SerializedName("category_list")
    @Expose
    private var category_list: String? = null

    @SerializedName("blocked_user_id")
    @Expose
    private var blocked_user_id: String? = null

    @SerializedName("registered_by")
    @Expose
    private var registered_by: String? = null

    @SerializedName("email_id")
    @Expose
    private var email_id: String? = null

    @SerializedName("first_name")
    @Expose
    private var first_name: String? = null

    @SerializedName("last_name")
    @Expose
    private var last_name: String? = null

    @SerializedName("profile_pic")
    @Expose
    private var profile_pic: String? = null

    @SerializedName("mark_as_verified_badge")
    @Expose
    private var mark_as_verified_badge: Int? = null

    @SerializedName("user_register_status")
    @Expose
    private var user_register_status: Int? = null

    @SerializedName("language")
    @Expose
    private var language: String? = null

    @SerializedName("date_of_birth")
    @Expose
    private var date_of_birth: String? = null

    @SerializedName("gender")
    @Expose
    private var gender: Int? = null

    @SerializedName("user_id")
    @Expose
    private var user_id: String? = null

    @SerializedName("phone_number")
    @Expose
    private var phone_number: String? = null

    @SerializedName("country_code")
    @Expose
    private var country_code: String? = null

    @SerializedName("allow_notify")
    @Expose
    private var allow_notify: Int? = null

    fun getisBlocked(): Int? {
        return is_blocked
    }

    fun getuser_id(): String? {
        return user_id
    }

    fun getallow_notify(): Int? {
        return allow_notify
    }


    fun getis_verified(): Int? {
        return is_verified
    }


    fun getno_of_followers(): String? {
        return no_of_followers
    }

    fun getphone_number(): String? {
        return phone_number
    }

    fun getcountry_code(): String? {
        return country_code
    }


    fun no_of_followings(): String? {
        return no_of_followings
    }


    fun getcategory_list(): String? {
        return category_list
    }


    fun getblocked_user_id(): String? {
        return blocked_user_id
    }


    fun getregistered_by(): String? {
        return registered_by
    }


    fun getemail_id(): String? {
        return email_id
    }


    fun getfirst_name(): String? {
        return first_name
    }


    fun getlast_name(): String? {
        return last_name
    }


    fun getprofile_pic(): String? {
        return profile_pic
    }


    fun mark_as_verified_badge(): Int? {
        return mark_as_verified_badge
    }


    fun user_register_status(): Int? {
        return user_register_status
    }


    fun language(): String? {
        return language
    }


    fun date_of_birth(): String? {
        return date_of_birth
    }


    fun getgender(): Int? {
        return gender
    }

}