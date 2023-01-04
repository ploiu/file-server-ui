package ploiu.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ploiu.config.ServerConfig;
import ploiu.exception.ServerUnavailableException;
import ploiu.model.ApiInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
public class ApiClient {
    private final ServerConfig serverConfig;
    private final HttpClient httpClient;

    public ApiInfo getApiInfo() {
        try {
            var request = HttpRequest.newBuilder(new URI(serverConfig.getBaseUrl() + "/api/version")).GET().build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return new ObjectMapper().readValue(response.body(), ApiInfo.class);
            } else {
                // only way this fails is if the server is down
                throw new ServerUnavailableException();
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            log.error("Failed to get api info from server", e);
            throw new RuntimeException(e);
        }
    }
}
