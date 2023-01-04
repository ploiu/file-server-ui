package ploiu.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.net.URIBuilder;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.model.FolderApi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class FolderClient {
    private final HttpClient client;
    private final AuthenticationConfig authConfig;
    private final ServerConfig serverConfig;

    public Optional<FolderApi> getFolder(Long id) {
        try {
            var url = new URIBuilder(serverConfig.getBaseUrl()).setPath("/folders/" + id).build();
            var request = HttpRequest.newBuilder(url)
                    .GET()
                    .header(HttpHeaders.AUTHORIZATION, authConfig.basicAuth())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Optional.of(new ObjectMapper().readValue(response.body(), FolderApi.class));
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }
}
