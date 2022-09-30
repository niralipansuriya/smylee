package smylee.app.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SignupResponse {
    @SerializedName("auth_token")
    @Expose
    private var auth_token: String? = null

    @SerializedName("email_id")
    @Expose
    private var email_id: String? = null

    fun getEmailID(): String? {
        return email_id
    }

    fun getEmailID(email_id: String) {
        this.email_id = email_id
    }

    @SerializedName("first_name")
    @Expose
    private var first_name: String? = null

    fun getfirst_name(): String? {
        return first_name
    }


    @SerializedName("user_name")
    @Expose
    private var user_name: String? = null

    fun getuser_name(): String? {
        return user_name
    }

    @SerializedName("last_name")
    @Expose
    private var last_name: String? = null

    fun getlast_name(): String? {
        return last_name
    }


    @SerializedName("country")
    @Expose
    private var country: String? = null

    fun getcountry(): String? {
        return country
    }


    @SerializedName("date_of_birth")
    @Expose
    private var date_of_birth: String? = null

    fun getdate_of_birth(): String? {
        return date_of_birth
    }


    @SerializedName("gender")
    @Expose
    private var gender: String? = null

    fun getgender(): String? {
        return gender
    }


    @SerializedName("language")
    @Expose
    private var language: String? = null

    fun getlanguage(): String? {
        return language
    }


    @SerializedName("category_list")
    @Expose
    private var category_list: String? = null

    fun getcategory_list(): String? {
        return category_list
    }

    @SerializedName("profile_pic")
    @Expose
    private var profile_pic: String? = null

    fun getprofile_pic(): String? {
        return profile_pic
    }


    @SerializedName("blocked_user_id")
    @Expose
    private var blocked_user_id: String? = null

    fun getblocked_user_id(): String? {
        return blocked_user_id
    }


    @SerializedName("user_register_status")
    @Expose
    private var user_register_status: String? = null

    fun getuser_register_status(): String? {
        return user_register_status
    }

    @SerializedName("is_verified")
    @Expose
    private var is_verified: String? = null

    fun getis_verified(): String? {
        return is_verified
    }


    @SerializedName("register_social_id")
    @Expose
    private var register_social_id: String? = null

    fun getregister_social_id(): String? {
        return register_social_id
    }


    @SerializedName("organisation_name")
    @Expose
    private var organisation_name: String? = null

    fun getorganisation_name(): String? {
        return organisation_name
    }

    @SerializedName("no_of_followers")
    @Expose
    private var no_of_followers: String? = null

    fun getno_of_followers(): String? {
        return no_of_followers
    }

    @SerializedName("no_of_followings")
    @Expose
    private var no_of_followings: String? = null

    fun getno_of_followings(): String? {
        return no_of_followings
    }


}