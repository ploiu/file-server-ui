package ploiu.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.message.BasicHeader;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;

import java.net.http.HttpClient;
import java.util.List;

import static org.apache.hc.core5.http.ContentType.APPLICATION_JSON;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;

public class HttpModule extends AbstractModule {
    @Provides
    @Deprecated(forRemoval = true)
    HttpClient defaultHttpClient() {
        return HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @Inject
    @Provides
    org.apache.hc.client5.http.classic.HttpClient apacheHttpClient(AuthenticationConfig config, ServerConfig serverConfig) {
        var credsProvider = new BasicCredentialsProvider();
        var authScope = new AuthScope(serverConfig.getHost(), serverConfig.getPort());
        var creds = new UsernamePasswordCredentials(config.getUsername(), config.getPassword().toCharArray());
        credsProvider.setCredentials(authScope, creds);
        return HttpClients
                .custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setDefaultHeaders(List.of(
                        new BasicHeader(ACCEPT, APPLICATION_JSON),
                        new BasicHeader(CONTENT_TYPE, APPLICATION_JSON)
                ))
                .build();
    }

}
