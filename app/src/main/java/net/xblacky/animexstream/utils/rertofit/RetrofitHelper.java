package net.xblacky.animexstream.utils.rertofit;

import net.xblacky.animexstream.BuildConfig;
import net.xblacky.animexstream.utils.constants.C;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RetrofitHelper {

    private static Retrofit retrofitInstance;

    static {
        try {
            RetrofitHelper retrofitHelper = new RetrofitHelper();
        } catch (Exception e) {
            throw new RuntimeException("Exception occured in creating singleton instance");
        }
    }

    private RetrofitHelper(){
        OkHttpClient client;

        if(BuildConfig.DEBUG){
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            client = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .addInterceptor(interceptor)
                    .build();
        }else{
            client = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .baseUrl(C.Companion.getBASE_URL())
                .build();
        retrofitInstance = retrofit;

    }


    public static Retrofit getRetrofitInstance(){

        return retrofitInstance;

    }
}
