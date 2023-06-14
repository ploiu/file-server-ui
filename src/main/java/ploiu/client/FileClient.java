package ploiu.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.ApiMessage;
import ploiu.model.CreateFileRequest;
import ploiu.model.FileApi;
import ploiu.model.UpdateFileRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Objects;

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
        var req = new HttpGet(serverConfig.getBaseUrl() + "/files/metadata/" + id);
        try {
            return httpClient.execute(req, res -> {
                var status = new StatusLine(res);
                var body = res.getEntity().getContent();
                if (status.isSuccessful()) {
                    return mapper.readValue(body, FileApi.class);
                } else {
                    var message = mapper.readValue(body, ApiMessage.class).message();
                    throw new BadFileResponseException(message);
                }
            });
        } catch (IOException e) {
            log.error("Unforeseen error getting file metadata", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(long id) throws BadFileRequestException, BadFileResponseException {
        if (id < 0) {
            throw new BadFileRequestException("Id cannot be negative.");
        }
        var req = new HttpDelete(serverConfig.getBaseUrl() + "/files/" + id);
        try {
            httpClient.execute(req, res -> {
                var status = new StatusLine(res);
                if (!status.isSuccessful()) {
                    var message = mapper.readValue(res.getEntity().getContent(), ApiMessage.class).message();
                    throw new BadFileResponseException(message);
                } else {
                    EntityUtils.consume(res.getEntity());
                }
                return null;
            });
        } catch (IOException e) {
            log.error("Unforeseen deleting file", e);
            throw new RuntimeException(e);
        }
    }

    public InputStream getFileContents(long id) throws BadFileRequestException, BadFileResponseException {
        if (id < 0) {
            throw new BadFileRequestException("Id cannot be negative.");
        }
        var req = new HttpGet(serverConfig.getBaseUrl() + "/files/" + id);
        try {
            return httpClient.execute(req, res -> {
                var status = new StatusLine(res);
                if (!status.isSuccessful()) {
                    var message = mapper.readValue(res.getEntity().getContent(), ApiMessage.class).message();
                    throw new BadFileResponseException(message);
                }
                // we can't read it as a string because that messes up the encoding
                return new ByteArrayInputStream(res.getEntity().getContent().readAllBytes());
            });
        } catch (IOException e) {
            log.error("Unforeseen error getting file contents", e);
            throw new RuntimeException(e);
        }
    }

    public FileApi updateFile(UpdateFileRequest request) throws BadFileRequestException, BadFileResponseException {
        if (request.id() < 0) {
            throw new BadFileRequestException("Id cannot be negative.");
        }
        if (request.name().isBlank()) {
            throw new BadFileRequestException("Name cannot be blank.");
        }
        var req = new HttpPut(serverConfig.getBaseUrl() + "/files");
        try {
            req.setEntity(new StringEntity(mapper.writeValueAsString(request)));
            req.setHeader("Content-Type", "application/json");
            return httpClient.execute(req, res -> {
                var status = new StatusLine(res);
                var body = res.getEntity().getContent();
                if (status.getStatusCode() == 200) {
                    return mapper.readValue(body, FileApi.class);
                } else {
                    var message = mapper.readValue(body, ApiMessage.class);
                    log.error("Failed to update file, message is {}", message.message());
                    throw new BadFileResponseException(message.message());
                }
            });
        } catch (IOException e) {
            log.error("Failed to build or send update file request!", e);
            throw new BadFileResponseException(e.getMessage());
        }
    }

    public Collection<FileApi> search(String query) throws BadFileRequestException, BadFileResponseException {
        if (query == null || query.isBlank()) {
            throw new BadFileRequestException("Query cannot be null or empty.");
        }
        var req = new HttpGet(serverConfig.getBaseUrl() + "/files/metadata?search=" + query);
        try {
            return httpClient.execute(req, res -> {
                var status = new StatusLine(res);
                if (!status.isSuccessful()) {
                    var message = mapper.readValue(res.getEntity().getContent(), ApiMessage.class).message();
                    throw new BadFileResponseException(message);
                }
                return mapper.readValue(res.getEntity().getContent(), new TypeReference<>() {
                });
            });
        } catch (IOException e) {
            log.error("Unforeseen error getting file contents", e);
            throw new RuntimeException(e);
        }
    }

    public FileApi createFile(CreateFileRequest request) throws BadFileRequestException, BadFileResponseException {
        var file = request.file();
        Objects.requireNonNull(file, "File cannot be null.");
        if (!file.exists()) {
            throw new BadFileRequestException("The selected file does not exist.");
        }
        var splitName = file.getName().split("\\.");
        var mimeType = URLConnection.guessContentTypeFromName(file.getName());
        var multipart = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.STRICT)
                .addBinaryBody("file", file, ContentType.create(mimeType == null ? "text/plain" : mimeType), file.getName())
                .addTextBody("folder_id", String.valueOf(request.folderId()));
        if (splitName.length > 1) {
            // file has extension, add it
            multipart.addTextBody("extension", splitName[splitName.length - 1]);
        }
        try (var built = multipart.build()) {
            var req = new HttpPost(serverConfig.getBaseUrl() + "/files");
            req.setEntity(built);
            return httpClient.execute(req, res -> {
                var status = new StatusLine(res);
                if (status.getStatusCode() != 201) {
                    var message = mapper.readValue(res.getEntity().getContent(), ApiMessage.class).message();
                    throw new BadFileResponseException(message);
                }
                return mapper.readValue(res.getEntity().getContent(), new TypeReference<>() {
                });
            });

        } catch (IOException e) {
            log.error("Unforeseen error creating file", e);
            throw new RuntimeException(e);
        }
    }

}
