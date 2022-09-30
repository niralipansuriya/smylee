package smylee.app.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BlockuserResponse {


    /*  val user_id: String,
      val profile_name: String,
      val profile_pic: String*/

    @SerializedName("user_id")
    @Expose
    private var user_id: String? = null

    @SerializedName("profile_name")
    @Expose
    private var profile_name: String? = null

    @SerializedName("profile_pic")
    @Expose
    private var profile_pic: String? = null

    @SerializedName("profile_pic_compres")
    @Expose
    private var profile_pic_compres: String? = null

    @SerializedName("mark_as_verified_badge")
    @Expose
    private var mark_as_verified_badge: Int? = null


    private var rowType: Int = 0


    fun getRowType(): Int {
        return rowType
    }

    fun setRowType(rowType: Int) {
        this.rowType = rowType
    }

    fun getuser_id(): String? {
        return user_id
    }

    fun getprofile_pic_compres(): String? {
        return profile_pic_compres
    }

    fun setuser_id(user_id: String?) {
        this.user_id = user_id
    }


    fun getprofile_pic(): String? {
        return profile_pic
    }

    fun setprofile_pic(profile_pic: String?) {
        this.profile_pic = profile_pic
    }

    fun getmark_as_verified_badge(): Int? {
        return mark_as_verified_badge
    }

    fun setmark_as_verified_badge(mark_as_verified_badge: Int?) {
        this.mark_as_verified_badge = mark_as_verified_badge
    }


    fun getprofile_name(): String? {
        return profile_name
    }

    fun setprofile_name(profile_name: String?) {
        this.profile_name = profile_name
    }


}

