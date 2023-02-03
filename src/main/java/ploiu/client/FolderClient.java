package ploiu.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.net.URIBuilder;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFolderRequestException;
import ploiu.exception.BadFolderResponseException;
import ploiu.model.ApiMessage;
import ploiu.model.FolderApi;
import ploiu.model.FolderRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
public class FolderClient {
    private final HttpClient client;
    private final AuthenticationConfig authConfig;
    private final ServerConfig serverConfig;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());

    public Optional<FolderApi> getFolder(Long id) {
        try {
            var url = new URIBuilder(serverConfig.getBaseUrl()).setPath("/folders/" + id).build();
            var request = HttpRequest.newBuilder(url)
                    .GET()
                    .header(HttpHeaders.AUTHORIZATION, authConfig.basicAuth())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Optional.of(mapper.readValue(response.body(), FolderApi.class));
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public FolderApi createFolder(FolderRequest request) throws BadFolderRequestException, BadFolderResponseException {
        try {
            var body = mapper.writeValueAsString(request);
            var clientRequest = HttpRequest.newBuilder(URI.create(serverConfig.getBaseUrl() + "/folders"))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Authorization", authConfig.basicAuth())
                    .build();
            var response = client.send(clientRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                return mapper.readValue(response.body(), FolderApi.class);
            } else if (response.statusCode() != 401) {
                var message = mapper.readValue(response.body(), ApiMessage.class);
                log.error("Failed to create folder, message is {}", message.message());
                throw new BadFolderRequestException(message.message());
            }
            throw new BadFolderRequestException("Received 401 from server");
        } catch (IOException | InterruptedException e) {
            log.error("Failed to build or send create folder request!", e);
            throw new BadFolderResponseException(e.getMessage());
        }
    }

    public FolderApi updateFolder(FolderRequest folder) throws BadFolderRequestException, BadFolderResponseException {
        if (folder.id().isEmpty()) {
            throw new BadFolderRequestException("Cannot update folder without id");
        }
        if (folder.id().get() == 0) {
            throw new BadFolderRequestException("0 is the root folder id, and cannot be updated");
        }
        try {
            var request = HttpRequest.newBuilder(URI.create(serverConfig.getBaseUrl() + "/folders"))
                    .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(folder)))
                    .header("Authorization", authConfig.basicAuth())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), FolderApi.class);
            } else {
                var errorMessage = mapper.readValue(response.body(), ApiMessage.class);
                throw new BadFolderResponseException(errorMessage.message());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Unforeseen error with update folder", e);
            throw new RuntimeException(e);
        }
    }

    public boolean deleteFolder(long id) {
        throw new UnsupportedOperationException();
    }
}
