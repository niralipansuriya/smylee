package smylee.app.retrofitclient

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*
import smylee.app.BuildConfig
import smylee.app.URFeedApplication
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext


object RetroClient2 {

    private var emergencyContactInterFace: EmergencyContactInterFace? = null

    private var fileUploadingService: MultipleFileUploadingService? = null


    fun multiFileUploadService(): MultipleFileUploadingService? {
        if (fileUploadingService == null) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.NONE


            /*   val cs = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                     .tlsVersions(TlsVersion.TLS_1_2)

                     .build()

                 val specs: MutableList<ConnectionSpec> = ArrayList()
                 specs.add(cs)
                 specs.add(ConnectionSpec.COMPATIBLE_TLS)
   */


            val okHttpClient = OkHttpClient()


            val clientWith30sTimeout = okHttpClient.newBuilder()

                .readTimeout(10000, TimeUnit.SECONDS)
                .writeTimeout(10000, TimeUnit.SECONDS)
                .connectTimeout(10000, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .authenticator(TokenAuthenticator())
                .addInterceptor(interceptor)
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build()


            val client = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(clientWith30sTimeout)
                .addConverterFactory(MultiPartConverter())
                .build()
            fileUploadingService = client.create(MultipleFileUploadingService::class.java)
        }
        return fileUploadingService
    }


    //https://maps.googleapis.com/maps/api/place/queryautocomplete/json


    fun getClient(BASE_URL: String): EmergencyContactInterFace? {
        try {
            if (emergencyContactInterFace == null) {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

                val okHttpClient = OkHttpClient.Builder()
                    .readTimeout(30000, TimeUnit.SECONDS)
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .writeTimeout(30000, TimeUnit.SECONDS)
                    .authenticator(TokenAuthenticator())
                    .addInterceptor(loggingInterceptor)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ToStringConverter())
                    .client(okHttpClient)
                    .build()
                emergencyContactInterFace =
                    retrofit.create(EmergencyContactInterFace::class.java)
            }
        } catch (e: Exception) {
            Logger.print("Exception == " + e.localizedMessage)
            e.printStackTrace()
        }

        return emergencyContactInterFace
    }


    interface EmergencyContactInterFace {
        /*@FormUrlEncoded
        @POST
        fun getTokenFor(
            @Url url: String,
            @HeaderMap headers: Map<String, String>,
            @FieldMap params: Map<String, String>
        ): Call<String>*/

        @POST
        fun callWebserviceToken(
            @Url apiName: String,
            @HeaderMap headers: Map<String, String>
        ): Call<String>

        @FormUrlEncoded
        @POST
        fun callWebservice(
            @Url apiName: String,
            @HeaderMap headers: Map<String, String>,
            @FieldMap fields: Map<String, String>
        ): Call<String>

        /*@GET
        fun callWebserviceGetTerms(@Url apiName: String): Call<String>*/

        @GET
        fun callWebserviceGet(
            @Url apiName: String,
            @HeaderMap headers: Map<String, String>,
            @QueryMap params: Map<String, String>
        ): Call<String>

        @FormUrlEncoded
        @PUT
        fun callWebservicePut(
            @Url apiName: String,
            @HeaderMap headers: Map<String, String>,
            @FieldMap params: Map<String, String>
        ): Call<String>

        @DELETE
        fun callWebserviceDelete(
            @Url apiName: String,
            @HeaderMap headers: Map<String, String>,
            @QueryMap params: Map<String, String>
        ): Call<String>

        @FormUrlEncoded
        @HTTP(method = "DELETE", hasBody = true)
        fun callWebserviceDeletebody(
            @Url apiName: String,
            @HeaderMap headers: Map<String, String>,
            @FieldMap fields: Map<String, String>
        ): Call<String>

        /*@Multipart
        @POST
        fun callWebserviceMultipart(
            @Url apiName: String, @HeaderMap headers: Map<String, String>,
            @Part files: List<MultipartBody.Part>, @PartMap params: Map<String, String>
        ): Call<String>*/

        @Multipart
        @POST
        fun callWebserviceSingleMultipart(
            @Url apiName: String, @HeaderMap headers: Map<String, String>,
            @Part files: MultipartBody.Part, @PartMap params: Map<String, String>
        ): Call<String>

        @Multipart
        @POST
        fun callWebserviceSingleMultipartToken(
            @Url apiName: String,
            @HeaderMap headers: Map<String, String>,
            @Part files: MultipartBody.Part
        ): Call<String>
    }

    interface MultipleFileUploadingService {
        @Multipart
        @POST
        fun postFile(
            @Url url: String,
            @Part video: List<MultipartBody.Part>,
            @Part image: List<MultipartBody.Part>,
            @HeaderMap headers: Map<String, String>,
            @PartMap Files: Map<String, @JvmSuppressWildcards RequestBody>
        ): Call<String>

        @Multipart
        @POST
        fun postFileprofile(
            @Url url: String,
            @Part video: List<MultipartBody.Part>,
            @HeaderMap headers: Map<String, String>,
            @PartMap Files: Map<String, @JvmSuppressWildcards RequestBody>
        ): Call<String>

        @Multipart
        @PUT
        fun putFile(
            @Url url: String,
            @Part video: List<MultipartBody.Part>,
            @Part image: List<MultipartBody.Part>,
            @HeaderMap headers: Map<String, String>,
            @PartMap Files: Map<String, @JvmSuppressWildcards RequestBody>
        ): Call<String>

        @Multipart
        @PUT
        fun putFileProfile(
            @Url url: String,
            @Part image: List<MultipartBody.Part>,
            @HeaderMap headers: Map<String, String>,
            @PartMap Files: Map<String, @JvmSuppressWildcards RequestBody>
        ): Call<String>

        @Multipart
        @POST
        fun postFile(
            @Url url: String,
            @HeaderMap headers: Map<String, String>,
            @PartMap Files: Map<String, @JvmSuppressWildcards RequestBody>
        ): Call<String>

        @Multipart
        @PUT
        fun putFile(
            @Url url: String,
            @HeaderMap headers: Map<String, String>,
            @PartMap Files: Map<String, @JvmSuppressWildcards RequestBody>
        ): Call<String>
    }

    /**
     * This class authenticate the request and
     * if token expired then need to refresh that access token and refresh token.
     */
    class TokenAuthenticator : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            val hashMap = HashMap<String, String>()
            hashMap["refresh_token"] = BuildConfig.defaultrefreshtoken
            hashMap["auth_token"] = SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
            try {
                Logger.print(">>****", "" + response.code)
                Logger.print(">>request_body", "" + response.request.body)
                Logger.print(">>Req", "" + hashMap)

                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                headers["android_app_version"] = BuildConfig.VERSION_NAME
                headers["language"] = "EN"
                //  headers["device_id"] = "123"
                headers["device_id"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.DEVICE_ID, "")!!
                headers["device_type"] = "0"
                // headers["os"] = "10.1"
                headers["os"] =
                    "OS-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(
                            Constants.OS_VERSION,
                            ""
                        )!! + " " + "manufacture-" + SharedPreferenceUtils.getInstance(
                        URFeedApplication.context!!
                    )
                        .getStringValue(
                            Constants.MANUFACTURER,
                            ""
                        )!! + " " + "Model-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.DEVICE_MODEL, "")!!
                headers["auth_token"] =
                    SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                        .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
                headers["refresh_token"] = BuildConfig.defaultrefreshtoken
                headers["Proxy-Connection"] = "Keep-Alive"

                if (emergencyContactInterFace == null) {
                    Logger.print("interface>>", "null")
                    emergencyContactInterFace = getClient(BuildConfig.BASE_URL)
                }

                val callToken = emergencyContactInterFace!!.callWebservice(
                    APIConstants.REFRESH_TOKEN_API,
                    headers,
                    hashMap
                )
                val tokenResponse = callToken.execute().body()!!.toString()
                Logger.print("tokenRes>>", tokenResponse)
                val jsonObject = JSONObject(tokenResponse)
                val authToken = jsonObject.getJSONObject("data").getString("new_token")
                SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                    .setValue(Constants.AUTH_TOKEN_PREF, authToken)

                Logger.print(
                    "EXPIRE>>",
                    response.request.method + "   rew:" + response.request.body
                )
                return response.request.newBuilder()
                    .method(response.request.method, response.request.body)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header(
                        "auth_token", SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                            .getStringValue(Constants.AUTH_TOKEN_PREF, "").toString()
                    )
                    .header(
                        "device_id", SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                            .getStringValue(Constants.DEVICE_ID, "")!!
                    )
                    .header("language", "EN")
                    .header("device_type", "0")
                    .header("android_app_version", BuildConfig.VERSION_NAME)
                    .header(
                        "os", "OS-" + SharedPreferenceUtils.getInstance(URFeedApplication.context!!)
                            .getStringValue(
                                Constants.OS_VERSION,
                                ""
                            )!! + " " + "manufacture-" + SharedPreferenceUtils.getInstance(
                            URFeedApplication.context!!
                        )
                            .getStringValue(
                                Constants.MANUFACTURER,
                                ""
                            )!! + " " + "Model-" + SharedPreferenceUtils.getInstance(
                            URFeedApplication.context!!
                        )
                            .getStringValue(Constants.DEVICE_MODEL, "")!!
                    )
                    .header("refresh_token", Constants.REFRESH_TOKEN)
                    .header("Proxy-Connection", "Keep-Alive")
                    .build()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}
