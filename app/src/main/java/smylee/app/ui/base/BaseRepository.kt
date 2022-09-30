package smylee.app.ui.base

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.urfeed.listener.OnResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import smylee.app.BuildConfig
import smylee.app.BuildConfig.BASE_URL
import smylee.app.URFeedApplication
import smylee.app.dialog.BlockedDialog
import smylee.app.login.LoginActivity
import smylee.app.retrofitclient.APIConstants
import smylee.app.retrofitclient.RetroClient2
import smylee.app.utils.*
import java.util.*

open class BaseRepository {

    var responseBody = MutableLiveData<String>()
    var cm: CustomLoaderDialog? = null

    fun callAPI(
        apiName: String, context: Context, requestMethod: Int, parameters: HashMap<String, String>,
        showDialog: Boolean, isDialogCancelable: Boolean, files: MultipartBody.Part?
    ): MutableLiveData<String> {

        if (!Utils.isOnline(context)) {
            Utils.showToastMessage(context, Constants.MESSAGE_CHECK_INTERNET_CONNECTION)
            responseBody.postValue(null)
            return responseBody
        }

        if (showDialog) {
            if (cm == null)
                cm = CustomLoaderDialog(context)
            cm!!.show(isDialogCancelable)
        }

        val headers = HashMap<String, String>()
        headers["Content-Type"] = "application/x-www-form-urlencoded"
        //headers["ios_app_version"] = VERSION_NAME
        headers["android_app_version"] = BuildConfig.VERSION_NAME
        when {
            apiName.contentEquals("userproceedAsGuest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            apiName.contentEquals("signinguest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            apiName.contentEquals("signup") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            apiName.contentEquals("forgotPassword") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            apiName.contentEquals("refreshToken") -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
            apiName.contentEquals("signin") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            else -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
        }

        headers["device_id"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DEVICE_ID, "")!!
        headers["language"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
        headers["device_type"] = "0"
        //  headers["os"] = "10.0"
        headers["os"] = "OS-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.OS_VERSION,
                ""
            )!! + " " + "manufacture-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.MANUFACTURER,
                ""
            )!! + " " + "Model-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DEVICE_MODEL, "")!!
        Logger.print("callAPI: headers $headers")

        var call: Call<String>? = null
        when (requestMethod) {
            APIConstants.POST_API -> {
                call = if (files != null) {
                    RetroClient2.getClient(BASE_URL)
                        ?.callWebserviceSingleMultipart(apiName, headers, files, parameters)
                } else {
                    RetroClient2.getClient(BASE_URL)?.callWebservice(apiName, headers, parameters)
                }
            }
            APIConstants.GET_API -> {
                //call = RetroClient2.getClient(BASE_URL)?.callWebserviceGetTerms(apiName)
                call = RetroClient2.getClient(BASE_URL)
                    ?.callWebserviceGet(apiName, headers, parameters)
            }
            APIConstants.DELETE_API -> {
                call =
                    RetroClient2.getClient(BASE_URL)
                        ?.callWebserviceDelete(apiName, headers, parameters)
            }
            APIConstants.PUT_API -> {
                call = RetroClient2.getClient(BASE_URL)
                    ?.callWebservicePut(apiName, headers, parameters)
            }
            APIConstants.DELETE_API_BODY -> {
                call = RetroClient2.getClient(BASE_URL)
                    ?.callWebserviceDeletebody(apiName, headers, parameters)
            }
        }

        call!!.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Logger.print("Request == ", call.request().toString())
                try {
                    if (cm != null && cm!!.isShowing) {
                        cm!!.hide()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                try {

                    when (response.code()) {
                        403 -> {
                            val error = response.errorBody()!!.string().trim()
                            Logger.print("Error response : $error")
                            val jsonObject = JSONObject(error)

                            val alertDialog = object : BlockedDialog(
                                context = context,
                                theme = android.R.style.Theme_Translucent_NoTitleBar
                            ) {
                                override fun okClicked() {
                                    if (context is LoginActivity) {
                                        dismiss()
                                    } else {
                                        SharedPreferenceUtils.getInstance(context).clear()
                                        val intent = Intent(context, LoginActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                    }
                                }
                            }
                            alertDialog.initDialog(
                                title = "",
                                message = jsonObject.getString("message")
                            )
                            alertDialog.show()
                            responseBody.postValue(null)
                        }
                        401 -> {
                            responseBody.postValue(null)

                        }
                        200 -> {
                            if (response.body() != null)
                                responseBody.postValue(response.body())

                        }
                        else -> {
                            Logger.print("Response code ===${response.code()}")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                if (apiName.contentEquals("getUserForYouList") || apiName.contentEquals("getfollowingList")) {
                    sendErrorMessage(t.message!!)
                }

                if (cm != null && cm!!.isShowing) {
                    cm!!.hide()
                }
                return responseBody.postValue(null)
            }
        })
        return responseBody
    }

    private fun sendErrorMessage(errorMessage: String) {
        val intent = Intent("errorMessage")
        intent.putExtra("apiErrorMessage", errorMessage)
        LocalBroadcastManager.getInstance(URFeedApplication.context!!).sendBroadcast(intent)
    }

    private fun sendErrorMessageForProfileUpload(errorMessage: String) {
        val intent = Intent("errorMessageProfileUpload")
        intent.putExtra("apiErrorMessageForUpload", errorMessage)
        LocalBroadcastManager.getInstance(URFeedApplication.context!!).sendBroadcast(intent)
    }

    fun callMultiPartAPI(
        relativeURL: String, context: Context,
        reqHashMap: HashMap<String, RequestBody>,
        methodName: String, showDialog: Boolean,
        isDialogCancelable: Boolean,
        methodType: Int,
        part: List<MultipartBody.Part>?
    ): MutableLiveData<String> {
        Logger.print("reqHashMap=================" + reqHashMap.keys)
        if (!Utils.isOnline(context)) {
            Utils.showToastMessage(context, Constants.MESSAGE_CHECK_INTERNET_CONNECTION)
            responseBody.postValue(null)
            return responseBody
        }

        if (showDialog) {
            if (cm == null)
                cm = CustomLoaderDialog(context)
            cm!!.show(isDialogCancelable)
        }

        val multiPartFileService = RetroClient2.multiFileUploadService()

        val headers = HashMap<String, String>()
        // headers["Content-Type"] = "application/x-www-form-urlencoded"
        // headers["ios_app_version"] = VERSION_NAME
        headers["android_app_version"] = BuildConfig.VERSION_NAME
        //headers["auth_token"] = BuildConfig.BASIC_AUTH
        when {
            methodName.contentEquals("userproceedAsGuest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signinguest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signup") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("forgotPassword") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signin") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("refreshToken") -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
            else -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
        }

        "OS - " + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.OS_VERSION,
                ""
            )!! + "manufacture - " + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.MANUFACTURER,
                ""
            )!! + "Model - " + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DEVICE_MODEL, "")!!
        headers["device_id"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DEVICE_ID, "")!!
        headers["device_type"] = "0"
        headers["language"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
        //  headers["os"] = "10.0"
        headers["os"] = "OS-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.OS_VERSION,
                ""
            )!! + " " + "manufacture-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.MANUFACTURER,
                ""
            )!! + " " + "Model-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DEVICE_MODEL, "")!!
        when {
            methodName.contentEquals("userproceedAsGuest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signinguest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signup") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("forgotPassword") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("refreshToken") -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
            methodName.contentEquals("signin") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            else -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
        }

        var call: Call<String>? = null
        if (APIConstants.POST_API == methodType) {
            call = if (part != null) {
                multiPartFileService?.postFileprofile(relativeURL, part, headers, reqHashMap)
            } else {
                multiPartFileService?.postFile(relativeURL, headers, reqHashMap)
            }
        } else if (APIConstants.PUT_API == methodType) {
            call = if (part != null) {
                multiPartFileService?.putFileProfile(relativeURL, part, headers, reqHashMap)
            } else {
                multiPartFileService?.putFile(relativeURL, headers, reqHashMap)
            }
        }

        call!!.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (cm!!.isShowing)
                    cm!!.hide()

                when (response.code()) {
                    403 -> {
                        val error = response.errorBody()!!.string().trim()
                        Logger.print("Error response : $error")
                        val jsonObject = JSONObject(error)

                        val alertDialog = object : BlockedDialog(
                            context = context,
                            theme = android.R.style.Theme_Translucent_NoTitleBar
                        ) {
                            override fun okClicked() {
                                if (context is LoginActivity) {
                                    dismiss()
                                } else {
                                    SharedPreferenceUtils.getInstance(context).clear()
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                }
                            }
                        }
                        alertDialog.initDialog(
                            title = "",
                            message = jsonObject.getString("message")
                        )
                        alertDialog.show()
                        responseBody.postValue(null) // blocked
                    }
                    401 -> {
                        responseBody.postValue(null) // logout
                        Logger.print("Error response 401: ${response.body().toString().length}")

                    }
                    200 -> {
                        if (response.body() != null)
                            Logger.print(
                                "Error response 200: ${response.errorBody().toString().trim()}"
                            )

                        responseBody.postValue(response.body())
                    }
                    else -> {
                        Logger.print("response code ==========${response.code()}")
                        Logger.print("response isSuccessful ==========${response.isSuccessful}")
                        if (response.code() == 524)//timeout
                        {
                            call.clone().enqueue(this)//retry
                        }
                    }
                }


            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                t.message
                if (cm!!.isShowing)
                    cm!!.hide()
                sendErrorMessageForProfileUpload(t.message!!)
                Logger.print("response", "<<onFailure --> " + t.message)
                //call.clone().enqueue(this);
            }
        })
        return responseBody
    }

    fun callMultiPartAPIVideo(
        relativeURL: String, context: Context,
        reqHashMap: HashMap<String, RequestBody>,
        methodName: String, showDialog: Boolean,
        isDialogCancelable: Boolean,
        methodType: Int,
        part: List<MultipartBody.Part>?,
        part_other: List<MultipartBody.Part>?
    ): MutableLiveData<String> {
        if (!Utils.isOnline(context)) {
            Utils.showToastMessage(context, Constants.MESSAGE_CHECK_INTERNET_CONNECTION)
            responseBody.postValue(null)
            return responseBody
        }

        if (showDialog) {
            if (cm == null)
                cm = CustomLoaderDialog(context)
            cm!!.show(isDialogCancelable)
        }

        val multiPartFileService = RetroClient2.multiFileUploadService()
        val headers = HashMap<String, String>()
        // headers["Content-Type"] = "application/x-www-form-urlencoded"
        //headers["ios_app_version"] = VERSION_NAME
        headers["android_app_version"] = BuildConfig.VERSION_NAME
        //headers["auth_token"] = BuildConfig.BASIC_AUTH
        when {
            methodName.contentEquals("userproceedAsGuest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signinguest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signup") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("forgotPassword") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signin") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("refreshToken") -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
            else -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
        }

        headers["device_id"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DEVICE_ID, "")!!
        headers["device_type"] = "0"
        headers["language"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
        // headers["os"] = "10.0"
        headers["os"] = "OS-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.OS_VERSION,
                ""
            )!! + " " + "manufacture-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.MANUFACTURER,
                ""
            )!! + " " + "Model-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DEVICE_MODEL, "")!!
        when {
            methodName.contentEquals("userproceedAsGuest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signinguest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signup") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("forgotPassword") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("refreshToken") -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
            methodName.contentEquals("signin") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            else -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
        }

        var call: Call<String>? = null
        if (APIConstants.POST_API == methodType) {
            call = if (part != null && part_other != null) {
                multiPartFileService?.postFile(relativeURL, part, part_other, headers, reqHashMap)
            } else {
                multiPartFileService?.postFile(relativeURL, headers, reqHashMap)
            }
        } else if (APIConstants.PUT_API == methodType) {
            call = if (part != null && part_other != null) {
                multiPartFileService?.putFile(relativeURL, part, part_other, headers, reqHashMap)
            } else {
                multiPartFileService?.putFile(relativeURL, headers, reqHashMap)
            }
        }

        call!!.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (cm!!.isShowing)
                    cm!!.hide()

                when (response.code()) {
                    403 -> {
                        val error = response.errorBody()!!.string().trim()
                        Logger.print("Error response : $error")
                        val jsonObject = JSONObject(error)
                        val alertDialog = object : BlockedDialog(
                            context = context,
                            theme = android.R.style.Theme_Translucent_NoTitleBar
                        ) {
                            override fun okClicked() {
                                if (context is LoginActivity) {
                                    dismiss()
                                } else {
                                    SharedPreferenceUtils.getInstance(context).clear()
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                }
                            }
                        }
                        alertDialog.initDialog(
                            title = "",
                            message = jsonObject.getString("message")
                        )
                        alertDialog.show()
                        responseBody.postValue(null) // blocked
                    }
                    401 -> {
                        responseBody.postValue(null) // logout

                    }
                    200 -> {
                        if (response.body() != null)
                            responseBody.postValue(response.body())

                    }
                    else -> {
                        Logger.print("response code ===${response.code()}")
                    }
                }


            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                if (cm!!.isShowing)
                    cm!!.hide()
                Logger.print("response", "<<onFailure --> " + t.message)
            }
        })
        return responseBody
    }

    fun callMultiPartAPIVideo(
        relativeURL: String, context: Context, reqHashMap: HashMap<String, RequestBody>,
        methodName: String, methodType: Int, part: List<MultipartBody.Part>?,
        part_other: List<MultipartBody.Part>?, onResponse: OnResponse
    ) {
        if (!Utils.isOnline(context)) {
            Utils.showToastMessage(context, Constants.MESSAGE_CHECK_INTERNET_CONNECTION)
            responseBody.postValue(null)
        }
        val multiPartFileService = RetroClient2.multiFileUploadService()
        val headers = HashMap<String, String>()
        //  headers["ios_app_version"] = VERSION_NAME
        headers["android_app_version"] = BuildConfig.VERSION_NAME
        when {
            methodName.contentEquals("userproceedAsGuest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signinguest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signup") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("forgotPassword") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("signin") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            methodName.contentEquals("refreshToken") -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
            else -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
        }

        headers["device_id"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DEVICE_ID, "")!!
        headers["device_type"] = "0"
        headers["language"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
        // headers["os"] = "10.0"
        headers["os"] = "OS-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.OS_VERSION,
                ""
            )!! + " " + "manufacture-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.MANUFACTURER,
                ""
            )!! + " " + "Model-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DEVICE_MODEL, "")!!

        Log.i("Headers", headers.toString())
        Log.i("RequestData", reqHashMap.toString())

        var call: Call<String>? = null
        if (APIConstants.POST_API == methodType) {
            call = if (part != null && part_other != null) {
                multiPartFileService?.postFile(relativeURL, part, part_other, headers, reqHashMap)
            } else {
                multiPartFileService?.postFile(relativeURL, headers, reqHashMap)
            }
        } else if (APIConstants.PUT_API == methodType) {
            call = if (part != null && part_other != null) {
                multiPartFileService?.putFile(relativeURL, part, part_other, headers, reqHashMap)
            } else {
                multiPartFileService?.putFile(relativeURL, headers, reqHashMap)
            }
        }

        call!!.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                when (response.code()) {
                    403 -> {
                        val error = response.errorBody()!!.string().trim()
                        //Logger.print("Error response : $error")
                        val jsonObject = JSONObject(error)
                        Logger.print("Response code 403 ==========${jsonObject.getString("message")}")
                        Logger.print("response 403=== ${response.body().toString()}")
                        onResponse.onFailure(
                            call,
                            Throwable(message = jsonObject.getString("message"))
                        )
                    }
                    200 -> {
                        if (response.body() != null)
                            onResponse.onSuccess(response.code(), response.body().toString())
                    }
                    else -> {
                        Logger.print("Response code ==========${response.code()}")
                        Logger.print("response else === ${response.body().toString()}")
                        if (response.code() == 524) {
                            call.clone().enqueue(this)
                        } else {
                            onResponse.onFailure(
                                call,
                                Throwable(message = "Error Uploading Video.Please Try again Later!!!")
                            )
                        }
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Logger.print("response", "<<onFailure --> " + t.message)
                onResponse.onFailure(call, t)
            }
        })
    }

    fun callAPIRefreshToken(
        apiName: String,
        context: Context,
        requestMethod: Int,
        showDialog: Boolean,
        isDialogCancelable: Boolean,
        files: MultipartBody.Part?
    ): MutableLiveData<String> {

        if (!Utils.isOnline(context)) {
            Utils.showToastMessage(context, Constants.MESSAGE_CHECK_INTERNET_CONNECTION)
            responseBody.postValue(null)
            return responseBody
        }

        if (showDialog) {
            if (cm == null)
                cm = CustomLoaderDialog(context)
            cm!!.show(isDialogCancelable)
        }

        val headers = HashMap<String, String>()
        //headers["ios_app_version"] = VERSION_NAME
        headers["android_app_version"] = BuildConfig.VERSION_NAME
        when {
            apiName.contentEquals("userproceedAsGuest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            apiName.contentEquals("signinguest") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            apiName.contentEquals("signup") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH

            }
            apiName.contentEquals("forgotPassword") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            apiName.contentEquals("refreshToken") -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
            apiName.contentEquals("signin") -> {
                headers["auth_token"] = BuildConfig.BASIC_AUTH
            }
            else -> {
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            }
        }
        headers["device_id"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DEVICE_ID, "")!!
        headers["device_type"] = "0"
        headers["language"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
        //headers["os"] = "10.0"
        headers["os"] = "OS-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.OS_VERSION,
                ""
            )!! + " " + "manufacture-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(
                Constants.MANUFACTURER,
                ""
            )!! + " " + "Model-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
            .getStringValue(Constants.DEVICE_MODEL, "")!!
        headers["refresh_token"] = BuildConfig.defaultrefreshtoken

        Logger.print("callAPI: headers $headers")

        var call: Call<String>? = null
        if (APIConstants.POST_API == requestMethod) {
            call = if (files != null) {
                RetroClient2.getClient(BASE_URL)
                    ?.callWebserviceSingleMultipartToken(apiName, headers, files)
            } else {
                RetroClient2.getClient(BASE_URL)?.callWebserviceToken(apiName, headers)
            }
        }

        call!!.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Logger.print("Request == ", call.request().toString())
                if (cm != null && cm!!.isShowing) {
                    cm!!.hide()
                }
                try {

                    when (response.code()) {
                        403 -> {
                            val error = response.errorBody()!!.string().trim()
                            Logger.print("Error response : $error")
                            val jsonObject = JSONObject(error)

                            val alertDialog = object : BlockedDialog(
                                context = context,
                                theme = android.R.style.Theme_Translucent_NoTitleBar
                            ) {
                                override fun okClicked() {
                                    if (context is LoginActivity) {
                                        dismiss()
                                    } else {
                                        SharedPreferenceUtils.getInstance(context).clear()
                                        val intent = Intent(context, LoginActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                    }
                                }
                            }
                            alertDialog.initDialog(
                                title = "",
                                message = jsonObject.getString("message")
                            )
                            alertDialog.show()
                            responseBody.postValue(null)
                        }
                        401 -> {
                            responseBody.postValue(null)

                        }
                        200 -> {
                            if (response.body() != null)
                                responseBody.postValue(response.body())

                        }
                        else -> {
                            Logger.print("response code ==${response.code()}")
                        }
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                if (cm != null && cm!!.isShowing) {
                    cm!!.hide()
                }
                return responseBody.postValue(null)
            }
        })
        return responseBody
    }


}