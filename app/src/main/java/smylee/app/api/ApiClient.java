package smylee.app.api;

import com.github.simonpercic.oklog3.OkLogInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import smylee.app.utils.FileUtils;

public class ApiClient {
    private static final String BASE_URL = FileUtils.MAIN_URL;
    private static Retrofit retrofit = null;

    /*private static Gson gson = new GsonBuilder()
            .setLenient()
            .create();*/

    public static Retrofit getClient() {
        // for signed app
        OkLogInterceptor okLogInterceptor = OkLogInterceptor.builder().build();
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.addInterceptor(okLogInterceptor);
        OkHttpClient okHttpClient = okHttpBuilder.build();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /*public static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(300, TimeUnit.SECONDS)
            .connectTimeout(300, TimeUnit.SECONDS)
            .build();*/
}