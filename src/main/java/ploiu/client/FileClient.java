package ploiu.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.ApiMessage;
import ploiu.model.FileApi;
import ploiu.model.UpdateFileRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class FileClient {
    private final HttpClient httpClient;
    private final ServerConfig serverConfig;
    private final AuthenticationConfig authenticationConfig;

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());


    public FileApi getMetadata(long id) throws BadFileRequestException, BadFileResponseException {
        if (id < 0) {
            throw new BadFileRequestException("Id cannot be negative.");
        }
        var request = HttpRequest.newBuilder(URI.create(serverConfig.getBaseUrl() + "/files/metadata/" + id))
                .GET()
                .header("Authorization", authenticationConfig.basicAuth())
                .build();
        try {
            var res = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (isStatus2xxOk(res.statusCode())) {
                return mapper.readValue(res.body(), FileApi.class);
            } else {
                var message = mapper.readValue(res.body(), ApiMessage.class).message();
                throw new BadFileResponseException(message);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Unforeseen error getting file metadata", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(long id) throws BadFileRequestException, BadFileResponseException {
        if (id < 0) {
            throw new BadFileRequestException("Id cannot be negative.");
        }
        var request = HttpRequest.newBuilder(URI.create(serverConfig.getBaseUrl() + "/files/" + id))
                .DELETE()
                .header("Authorization", authenticationConfig.basicAuth())
                .build();
        try {
            var res = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (!isStatus2xxOk(res.statusCode())) {
                var message = mapper.readValue(res.body(), ApiMessage.class).message();
                throw new BadFileResponseException(message);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Unforeseen deleting file", e);
            throw new RuntimeException(e);
        }
    }

    public InputStream getFileContents(long id) throws BadFileRequestException, BadFileResponseException {
        if (id < 0) {
            throw new BadFileRequestException("Id cannot be negative.");
        }
        var request = HttpRequest.newBuilder(URI.create(serverConfig.getBaseUrl() + "/files/" + id))
                .GET()
                .header("Authorization", authenticationConfig.basicAuth())
                .build();
        try {
            var res = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (!isStatus2xxOk(res.statusCode())) {
                var message = mapper.readValue(res.body(), ApiMessage.class).message();
                throw new BadFileResponseException(message);
            }
            return res.body();
        } catch (IOException | InterruptedException e) {
            log.error("Unforeseen error getting file contents", e);
            throw new RuntimeException(e);
        }
    }

    public Optional<FileApi> updateFile(UpdateFileRequest request) {
        throw new UnsupportedOperationException();
    }

    public Collection<FileApi> search(String query) throws BadFileRequestException, BadFileResponseException {
        if (query == null || query.isBlank()) {
            throw new BadFileRequestException("Query cannot be null or empty.");
        }
        var request = HttpRequest.newBuilder(URI.create(serverConfig.getBaseUrl() + "/files?query=" + query))
                .GET()
                .header("Authorization", authenticationConfig.basicAuth())
                .build();
        try {
            var res = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (!isStatus2xxOk(res.statusCode())) {
                var message = mapper.readValue(res.body(), ApiMessage.class).message();
                throw new BadFileResponseException(message);
            }
            return mapper.readValue(res.body(), new TypeReference<>() {
            });
        } catch (IOException | InterruptedException e) {
            log.error("Unforeseen error getting file contents", e);
            throw new RuntimeException(e);
        }
    }

    public FileApi createFile(File file, String name) {
        throw new UnsupportedOperationException();
    }

    private boolean isStatus2xxOk(int statusCode) {
        return List.of(200, 201, 202, 203, 204, 205, 206, 207, 208, 226).contains(statusCode);
    }
}
