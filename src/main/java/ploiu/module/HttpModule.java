package ploiu.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.message.BasicHeader;
import ploiu.config.AuthenticationConfig;

import java.util.List;

import static org.apache.hc.core5.http.ContentType.APPLICATION_JSON;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;
import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

public class HttpModule extends AbstractModule {

    @Inject
    @Provides
    @SuppressWarnings("unused")
    HttpClient apacheHttpClient(AuthenticationConfig authConfig) {
        return HttpClients
                .custom()
                .setDefaultHeaders(List.of(
                        new BasicHeader(ACCEPT, APPLICATION_JSON),
                        new BasicHeader(AUTHORIZATION, authConfig.basicAuth())
                ))
                .build();
    }

}
