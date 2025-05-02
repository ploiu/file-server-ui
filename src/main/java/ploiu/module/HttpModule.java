package ploiu.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import ploiu.client.ApiClient;
import ploiu.client.FileClient;
import ploiu.client.FolderClient;
import ploiu.client.TagClient;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class HttpModule extends AbstractModule {

    @Inject
    @Provides
    Retrofit retrofitClient(ServerConfig serverConfig, AuthenticationConfig authConfig) {
        // use okhttp so I can add headers to every request
        var client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    var req = chain.request().newBuilder().addHeader("Authorization", authConfig.basicAuth()).build();
                    return chain.proceed(req);
                })
                .connectTimeout(1, TimeUnit.DAYS)
                .callTimeout(Duration.of(1, ChronoUnit.DAYS))
                .readTimeout(Duration.of(1, ChronoUnit.DAYS))
                .writeTimeout(Duration.of(1, ChronoUnit.DAYS))
                .build();
        return new Retrofit.Builder()
                .baseUrl(serverConfig.getBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper().registerModule(new Jdk8Module())))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(client)
                .build();

    }

    @Inject
    @Provides
    TagClient tagClient(Retrofit retrofit) {
        return retrofit.create(TagClient.class);
    }

    @Inject
    @Provides
    FileClient retrofitFileClient(Retrofit retrofit) {
        return retrofit.create(FileClient.class);
    }

    @Inject
    @Provides
    FolderClient retrofitFolderClient(Retrofit retrofit) {
        return retrofit.create(FolderClient.class);
    }

    @Inject
    @Provides
    ApiClient retrofitApiClient(Retrofit retrofit) {
        return retrofit.create(ApiClient.class);
    }
}
