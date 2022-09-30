package smylee.app.retrofitclient


class APIConstants {

    companion object {
        const val GET_API = 1
        const val POST_API = 2
        const val PUT_API = 3
        const val DELETE_API = 4
        const val DELETE_API_BODY = 5
        const val REFRESH_TOKEN_API = "refreshToken"
        /*const val PHOTOS_VIDEOS = "user"
        const val LOGIN = "login"*/
        const val SIGNIN = "signin"
        const val SIGNINWITHPHONENUMBER = "signinguest"
        const val signup = "signup"
//        const val FORGOTPASSWORD = "forgotPassword"
        const val VIDEOLANGUAGE = "getLanguageList"
        const val TRENDINGLIST = "getUserTrendingList"
        const val HASHTAGDETAIL = "getVideoListByHashtag"
        const val UPDATEVIDEOLANGUAGE = "updateUserLanguage"
        const val DISCOVERCATEGORY = "getDiscoverList"
        const val USERCATEGORY = "getCategoryList"
        const val UPDATEUSERCATEGORY = "updateUserCategory"

        //  const val DISCOVERCATEGORYVIEWALL = "getSeeMoreVideoCategoryIdList"
        const val GETCOMMENTS = "getVideoCommentsList"
        const val COUNTRY_CODE = "91"
        const val GETBLOCKUSERS = "getBlockedUserList"
        const val BLOCKUNBLOCKUSER = "blockUnblockUser"
        const val UPDATEUSERPROFILE = "updateUserProfile"
        const val DELETEPOST = "deleteUserPost"
        const val POSTVIDEOTOUSER = "postVideoToUser"//maximum size of video to upload = 15MB
        const val GETUSERANDOTHERUSERPROFILE = "getUserProfile"
        const val LIKEUNLIKEcomments = "likeUnlikeComment"
        const val ADDVIDEOCOMMENT = "addCommentToVideo"
        const val VERIFYCODE = "verifyCode"
        const val RESENDCODE = "resendCode"
        const val FLAGVIDEOORCOMMENT = "reportPostORComment"
        const val ALBUMLISTAUDIO = "getAlbumList" // audio album list
        const val FORYOUALBUMLIST = "getAudioForYoulist" //audio for merge
        const val SETUSERDEVICERELATION = "setUserDeviceRelation" // for fcm token
        const val SIGNOUT = "signout"
        const val USERPROCEEDASGUEST = "userproceedAsGuest" //for user procced as guest
        const val DISCOVERVIEWALLCATEGORY =
            "getUserVideoByCategoryList" // for view all categories related discover
        const val CHANGEPASSWORD = "changePassword"
        const val CHANGEMOBILE = "changeMobileNumber"
        const val VERIFYCHANGEMOBILE = "verifyOTPForMobileChange"
        const val FOLLOWINGVIDEOLIST = "getfollowingList" //home screen following list
        const val ALBUMAUDIODETAIL = "getAudioListByAlbumId" //For album detail screen
        const val FOLLOWERLISTUSER = "getUserFollowerList" //For list of follower
        const val FOLLOWUNFOLLOWUSER = "followUnfollowUser" //For list of follower
        const val FORYOULISTING = "getUserForYouList" //For list of foryou in home
        const val GETBANNER = "getBannerList" //For trending banner
        const val HASHTAGLIST = "getHashTagList" //For trending banner
        const val VIDEODETAILS = "getVideoDetails" //For video details list
        const val LIKEUNLIKEVIDEO = "likeUnlikeVideo" //like unlike video
        const val NOTIFICATIONLISTING = "getUserNotificationList" //get notification
        /*const val PRIVACYPOLICY = "privacypolicy"
        const val TERMSCONDITION = "termsandcondition"*/
        const val SEARCHVIDEOLIST = "getSearchVideoList" //for explore search
        const val SEARCHVIEWMOREVIDEOS = "getSearchRecordedVideoList" //for explore search view more videos
        const val SEARCHVIEWMOREPEOPLE = "getSearchUserList" //for explore search view more peoples
    }
}