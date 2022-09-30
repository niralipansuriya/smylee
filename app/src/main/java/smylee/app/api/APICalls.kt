import retrofit2.Call
import retrofit2.http.*

interface APICalls {

    @POST
    fun postRequest(
        @Url url: String,
        @HeaderMap headers: HashMap<String, Any>,
        @Body query: String
    ): Call<String>


    /*@Multipart
    @POST("updateUserProfile")
    fun ProfilePicUpdate(
        @HeaderMap headers: HashMap<String, Any>,
        @Part file: MultipartBody.Part?
    ): Call<ProfileUpdateResponse>?


    @POST
    fun postRequest1(
        @Url url: String,
        @HeaderMap headers: HashMap<String, Any>,
        @QueryMap query: HashMap<String, Any?>
    ): Call<String>*/

    @PUT
    fun putRequest(
        @Url url: String,
        @HeaderMap headers: HashMap<String, Any>,
        @Body query: String
    ): Call<String>

    @GET
    fun getRequest(
        @Url url: String, @HeaderMap headers: HashMap<String, Any>,
        @QueryMap query: HashMap<String, Any?>
    ): Call<String>

    /*@Multipart
    @POST
    fun uploadImage(
        @Url url: String, @HeaderMap headers: HashMap<String, Any>,
        @Part image: MultipartBody.Part, @Part("email") email: RequestBody
    ): Call<String>*/
}