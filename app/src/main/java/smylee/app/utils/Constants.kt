package smylee.app.utils

import android.os.Environment
import java.io.File

object Constants {
    //const val PRIVACY_POLICY_URL = "https://api.smylee.app:8443/privacypolicy"
    const val PRIVACY_POLICY_URL = "https://smylee.app/privacy-policy.html"

    // const val TERMS_AND_CONDITION_URL = "https://api.smylee.app:8443/termsandcondition"
    const val TERMS_AND_CONDITION_URL = "https://smylee.app/terms.html"

    const val App_Folder = "Tri-state"
    const val App_Folder_Record_Video = "RecordVideo"
    private const val App_Folder_Extract_Video = "ExtractVideo"
    private const val App_Folder_Merge_Video = "MERGED"
    private const val App_Folder_Compress_Video = "COMPRESS_FILES"
    const val AudioFile = "AudioFile"
    const val MixAudioVideo = "MixAudioVideo"
    const val GifPath = "GifPath"
    const val SharePath = "SharePost"
    const val GifName = "thumb.GIF"
    const val MESSAGE_CHECK_INTERNET_CONNECTION = "Please check your internet connection"

    //const val MESSAGE_CHECK_INTERNET_CONNECTION = R.string.check_connection
    const val APP_NAME = "Smylee"

    //    const val DEFAULT_AUTH_TOKEN: String = "SlnBk@PvuPDM7$8@2G8EX6kq"
    const val AUTH_TOKEN_PREF: String = "AUTH_TOKEN_PREF"
    const val EMAILPREF: String = "EMAIL_PREF"
    const val APP_STORE_ID: String = "1544068564"
    const val CHANGEDMOBILENUMBER: String = "CHANGE_PHONE_NUMBER"
    const val changeCountryCode: String = "CHANGE_countrycode"
    const val PHONE_NUMBER_PREF: String = "PHONE_NUMBER"
    const val COUNTRY_CODE_PREF: String = "COUNTRY_CODE"
    const val GENDER_PREF: String = "GENDER"
    const val DOB_PREF: String = "BIRTH_DATE"
    const val FIRST_NAME_PREF: String = "FIRST_NAME"
    const val LAST_NAME_PREF: String = "LAST_NAME"
    const val PROFILE_PIC_PREF: String = "PROFILE_PIC"
    const val CROPED_PROFILE_PIC: String = "CROPED_PROFILE_PIC"
    const val FACEBOOK_URL: String = "FACEBOOK_URL"
    const val MARK_AS_VERIFIED_BADGE: String = "MARK_AS_VERIFIED_BADGE"
    const val INSTAGRAM_URL: String = "INSTAGRAM_URL"
    const val YOUTUBE_URL: String = "YOUTUBE_URL"
    const val LANGUAGE_PREF: String = "LANGUAGE"
    const val LANGUAGE_CODE_PREF: String = "LANGUAGE_CODE"
    const val LANGUAGE_APP_ID: String = "LANGUAGE_ID"
    const val FIREBASE_TOKEN = "firebase_token"
    const val DEVICE_ID = "device_id"
    const val FIRST_TIME_INSTALL = "first_time_install"
    const val FIRST_INSTALL = "first_install"
    const val CURRENT_DATE = "current_date"
    const val OS_VERSION = "android_os_verison"
    const val U_ID = "U_ID_PREF"
    const val FIREBASE_TOKEN_CHANGED = "firebase_token_changed"
    const val MANUFACTURER = "DEVICE_MANUFACTURER"
    const val DEVICE_MODEL = "DEVICE_MODEL"

    const val USERNAME_PREF: String = "USERNAME"
    const val COUNTRY_PREF: String = "COUNTRY"
    const val CATEGORY_PREF: String = "CATEGORY"
    const val ORGANISATION_NAME_PREF: String = "ORGANISATION_NAME"
    const val USER_REGISTER_STATUS_PREF: String = "USER_REGISTER_STATUS"
//    const val REGISTER_SOCIAL_ID_PREF: String = "REGISTER_SOCIAL_ID"
    const val FOLLOWER_COUNT_PREF: String = "FOLLOWERS"
    const val FOLLOWING_COUNT_PREF: String = "FOLLOWINGS"
    const val BLOCK_USER_PREF: String = "BLOCK_USER"
    const val USER_ID_PREF: String = "USER_ID"
    const val ALLOW_NOTIFY: String = "allow_notify"
    const val IS_BLOCKED_PREF: String = "IS_BLOCKED"
    const val IS_VERIFIED_PREF: String = "IS_VERIFIED"
    const val REG_ID_PREF: String = "REG_ID"
    const val SHARE_URI: String = "SHARE_URI"
    const val PROFILE_DATA_API_TIME = "profileDataAPITime"
    const val IS_ANYTHING_CHANGE_MYPROFILE = "isAnythingChangeMyProfile"
    const val IS_CHANGE_LIKE_OR_COMMENT_OR_FOLLOW = "isChangeLikeCommentFollow"
    const val IS_ANYTHING_CHANGE_DISCOVER = "isAnythingChangeDiscover"
    const val IS_ANYTHING_CHANGE_TRENDING = "isAnythingChangeTrending"
    const val IS_ANYTHING_CHANGE_FOLLOWING = "isAnythingChangeForYou"
    const val IS_ANYTHING_CHANGE_FORYOU_FOR_BLOCK = "isAnythingChangeForYouBlock"
    const val IS_ANYTHING_CHANGE_NOTIFICATION = "isAnythingChangeNotification"
    const val IS_ANYTHING_CHANGE_FOLLOWING_ = "isAnythingChangeFollowing"
    const val IS_ANYTHING_CHANGE_FOR_You = "isAnythingChangeForYouData"

    //const val AUTH_TOKEN: String = "@#Slsjpoq$S1o08#MnbAiB%UVUV&Y*5EU@exS1o!08L9TSlsjpo#BEHEARDQY"
//    const val AUTH_TOKEN = "AUTH_TOKEN"
    const val REFRESH_TOKEN = "REFRESH_TOKEN"
    const val DELAY = 1000L
//    const val TYPE_ROW_LOADING = 1

    const val TYPE_IMAGE = "1"
//    const val TYPE_VIDEO = "2"

//    const val LOGINUSERDATA = "userData"

//    const val USER_TYPE = "1"
    const val IS_LOGIN = "is_login"
    const val skip_login = "skip_login"

    const val LOGIN_ACTIVITY_REQUEST_CODE = 1233
    const val OTP_ACTIVITY_REQUEST_CODE = 1234

    val RECORD_PATH = Environment.getExternalStorageDirectory()
        .toString() + File.separator + App_Folder + File.separator + App_Folder_Record_Video + File.separator
    val EXTRACT_PATH = Environment.getExternalStorageDirectory()
        .toString() + File.separator + App_Folder + File.separator + App_Folder_Extract_Video + File.separator
    val ROOT_PATH = Environment.getExternalStorageDirectory()
        .toString() + File.separator + App_Folder + File.separator
    val COMPRESSEDFILES_PATH = Environment.getExternalStorageDirectory()
        .toString() + File.separator + App_Folder + File.separator + App_Folder_Compress_Video + File.separator
    val MERGE_PATH = Environment.getExternalStorageDirectory()
        .toString() + File.separator + App_Folder + File.separator + App_Folder_Merge_Video + File.separator
    val Audio_PATH = Environment.getExternalStorageDirectory()
        .toString() + File.separator + App_Folder + File.separator + AudioFile + File.separator
    val MixAudioVideo_PATH = Environment.getExternalStorageDirectory()
        .toString() + File.separator + App_Folder + File.separator + MixAudioVideo + File.separator

    val GIF_Path = Environment.getExternalStorageDirectory()
        .toString() + File.separator + App_Folder + File.separator + GifPath + File.separator

    val DOWNLOADED_PATH = Environment.getExternalStorageDirectory()
        .toString() + File.separator + APP_NAME + File.separator

    val Share_Path = Environment.getExternalStorageDirectory()
        .toString() + File.separator + App_Folder + File.separator + SharePath + File.separator

    /*val MAINDiRECTORY = Environment.getExternalStorageDirectory()
        .toString() + File.separator + App_Folder + File.separator*/
}