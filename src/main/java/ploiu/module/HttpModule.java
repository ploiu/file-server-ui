package ploiu.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.message.BasicHeader;
import ploiu.client.TagClient;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;

import static org.apache.hc.core5.http.ContentType.APPLICATION_JSON;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;
import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

public class HttpModule extends AbstractModule {

    @Inject
    @Provides
    @Deprecated
    HttpClient apacheHttpClient(AuthenticationConfig authConfig) {
        return HttpClients
                .custom()
                .setDefaultHeaders(List.of(
                        new BasicHeader(ACCEPT, APPLICATION_JSON),
                        new BasicHeader(AUTHORIZATION, authConfig.basicAuth())
                ))
                .build();
    }

    @Inject
    @Provides
    @SuppressWarnings("unused")
    Retrofit retrofitClient(ServerConfig serverConfig, AuthenticationConfig authConfig) {
        // use okhttp so I can add headers to every request
        var client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    var req = chain.request().newBuilder().addHeader("Authorization", authConfig.basicAuth()).build();
                    return chain.proceed(req);
                })
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
    @SuppressWarnings("unused")
    TagClient tagClient(Retrofit retrofit) {
        return retrofit.create(TagClient.class);
    }
}
