package click.tagit.data.remote;

import click.tagit.BuildConfig;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 *   * User: Anurag Singh
 *   * Date: 18/8/17
 *   * Time: 19:45 PM
 *
 */
public enum ClickTagitRESTClientSingleton {

    INSTANCE;
    private APIClickTagitClient mAPIClickTagitClient;

    ClickTagitRESTClientSingleton() {
    }

    public APIClickTagitClient getRESTClient() {
        Timber.d("getRESTClient() called");

        if (mAPIClickTagitClient == null) {
            init();
        }
        return mAPIClickTagitClient;
    }

    public void init() {
        Timber.d("init() called");

        if (mAPIClickTagitClient == null) {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(BuildConfig.URL_BASE)
                    .client(getHttpClient())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(getConverterFactory())
                    .build();
            mAPIClickTagitClient = retrofit.create(APIClickTagitClient.class);
        }
    }

    /**
     * Custom Http Client to define connection timeouts.
     */
    private OkHttpClient getHttpClient() {
        Timber.d("getHttpClient() called");

        OkHttpClient.Builder client =
                new OkHttpClient.Builder().readTimeout(APIConstants.READ_TIME_OUT, TimeUnit.MINUTES)
                        .writeTimeout(APIConstants.WRITE_TIME_OUT, TimeUnit.MINUTES)
                        .connectTimeout(APIConstants.CONNECTION_TIME_OUT, TimeUnit.MINUTES);

        client.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();

                requestBuilder.header(APIConstants.KEY_USER_AGENT_HEADER,
                        APIConstants.VALUE_USER_AGENT_HEADER);
                return chain.proceed(requestBuilder.build());
            }
        });

        if (BuildConfig.DEBUG) {
            client.networkInterceptors().add(new StethoInterceptor());
            //add logging as last interceptor
            client.networkInterceptors()
                    .add(getLoggingInterceptor());// <-- this is the important line!
        }
        return client.build();
    }

    /**
     * Creates the Converter factory by setting custom HttpClient.
     */
    private Converter.Factory getConverterFactory() {
        Timber.d("getConverterFactory() called");

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .create();
        return GsonConverterFactory.create(gson);
    }

    /**
     * Custom Logging Interceptor for Logs
     */

    private HttpLoggingInterceptor getLoggingInterceptor() {
        Timber.d("getLoggingInterceptor() called");

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return logging;
    }
}
