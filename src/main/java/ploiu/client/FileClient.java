package ploiu.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.exception.BadFolderResponseException;
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
            if (res.statusCode() == 200) {
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

    public boolean deleteFile(long id) {
        throw new UnsupportedOperationException();
    }

    public Optional<InputStream> downloadFile(long id) {
        throw new UnsupportedOperationException();
    }

    public Optional<FileApi> updateFile(UpdateFileRequest request) {
        throw new UnsupportedOperationException();
    }

    public Collection<FileApi> search(String query) {
        throw new UnsupportedOperationException();
    }

    public FileApi createFile(File file, String name) {
        throw new UnsupportedOperationException();
    }
}
