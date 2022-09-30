package smylee.app.api

import APICalls
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import smylee.app.BuildConfig
import java.util.concurrent.TimeUnit

class APIClient {

    companion object {
        private var retrofit: Retrofit? = null
        private var apiCalls: APICalls? = null

        fun getClient(): APICalls? {
            if (apiCalls == null) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level =
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

                val client = OkHttpClient
                    .Builder()
                    .addInterceptor(interceptor)
                    .readTimeout(3, TimeUnit.MINUTES)
                    .connectTimeout(3, TimeUnit.MINUTES)
                    .retryOnConnectionFailure(true)
                    .build()

                retrofit = Retrofit.Builder()
                    .baseUrl(APIConst.BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(client)
                    .build()

                apiCalls = retrofit?.create(APICalls::class.java)!!
            }

            return apiCalls
        }
    }
}