package ploiu.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.exception.ServerUnavailableException;
import ploiu.model.ApiInfo;
import ploiu.model.CreatePasswordRequest;

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
    private final AuthenticationConfig authConfig;

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

    public boolean setPassword() {
        var body = new CreatePasswordRequest(authConfig.getUsername(), authConfig.getPassword());
        try {
            var request = HttpRequest.newBuilder(URI.create(serverConfig.getBaseUrl() + "/password"))
                    .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(body)))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // 400 means that a password is already set, so we can ignore this
            return response.statusCode() == 201 || response.statusCode() == 400;
        } catch (IOException | InterruptedException e) {
            log.error("unforeseen error when setting password", e);
            throw new RuntimeException(e);
        }
    }

    public boolean isCompatibleWithServer() throws Exception {
        log.info("Checking if server is compatible with client (looking for pattern " + serverConfig.getCompatibleVersion() + ")");
        var serverVersion = this.getApiInfo().version();
        log.info("server version is " + serverVersion);
        var matches = serverConfig.getVersionMatcher().matcher(serverVersion).find();
        if (matches) {
            log.info("We're compatible!");
        } else {
            log.error("Incompatible with current server version.");
        }
        return matches;
    }

}
