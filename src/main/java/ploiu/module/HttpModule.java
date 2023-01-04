package ploiu.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.net.http.HttpClient;

public class HttpModule extends AbstractModule {
    @Provides
    HttpClient defaultHttpClient() {
        return HttpClient.newHttpClient();
    }
}
