package ploiu.client;

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
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFolderRequestException;
import ploiu.exception.BadFolderResponseException;
import ploiu.model.ApiMessage;
import ploiu.model.FolderApi;
import ploiu.model.FolderRequest;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Deprecated(forRemoval = true)
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
public class FolderClient {
    private final HttpClient client;
    private final ServerConfig serverConfig;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());

    public Optional<FolderApi> getFolder(long id) {
        try {
            var req = new HttpGet(serverConfig.getBaseUrl() + "/folders/" + id);
            return client.execute(req, res -> {
                var status = new StatusLine(res);
                if (status.getStatusCode() == 200) {
                    return Optional.of(mapper.readValue(res.getEntity().getContent(), FolderApi.class));
                }
                return Optional.empty();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FolderApi createFolder(FolderRequest request) throws BadFolderRequestException, BadFolderResponseException {
        try {
            var req = new HttpPost(serverConfig.getBaseUrl() + "/folders");
            req.setEntity(new StringEntity(mapper.writeValueAsString(request)));
            req.addHeader("Content-Type", "application/json");
            return client.execute(req, res -> {
                var status = new StatusLine(res);
                var body = res.getEntity().getContent();
                if (status.getStatusCode() == 201) {
                    return mapper.readValue(body, FolderApi.class);
                } else {
                    var message = mapper.readValue(body, ApiMessage.class);
                    log.error("Failed to create folder, message is {}", message.message());
                    throw new BadFolderResponseException(message.message());
                }
            });
        } catch (IOException e) {
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
            var req = new HttpPut(serverConfig.getBaseUrl() + "/folders");
            req.setEntity(new StringEntity(mapper.writeValueAsString(folder)));
            req.setHeader("Content-Type", "application/json");
            return client.execute(req, res -> {
                var status = new StatusLine(res);
                var body = res.getEntity().getContent();
                if (status.getStatusCode() == 200) {
                    return mapper.readValue(body, FolderApi.class);
                } else {
                    var error = mapper.readValue(body, ApiMessage.class);
                    throw new BadFolderResponseException(error.message());
                }
            });
        } catch (IOException e) {
            log.error("Unforeseen error with update folder", e);
            throw new RuntimeException(e);
        }
    }

    public boolean deleteFolder(long id) throws BadFolderRequestException, BadFolderResponseException {
        if (id < 1) {
            throw new BadFolderRequestException("id must be greater than 0");
        }
        try {
            var req = new HttpDelete(serverConfig.getBaseUrl() + "/folders/" + id);
            return client.execute(req, res -> {
                var status = new StatusLine(res);
                if (status.getStatusCode() == 204) {
                    return true;
                }
                var error = mapper.readValue(res.getEntity().getContent(), ApiMessage.class);
                throw new BadFolderResponseException(error.message());
            });
        } catch (IOException e) {
            log.error("Unforeseen error with delete folder", e);
            throw new RuntimeException(e);
        }
    }
}
