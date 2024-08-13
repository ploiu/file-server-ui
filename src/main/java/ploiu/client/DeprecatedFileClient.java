package ploiu.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
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
import ploiu.config.ServerConfig;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.ApiMessage;
import ploiu.model.CreateFileRequest;
import ploiu.model.FileApi;
import ploiu.model.UpdateFileRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Deprecated(forRemoval = true)
public class DeprecatedFileClient {
    private final HttpClient httpClient;
    private final ServerConfig serverConfig;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());

    public Single<FileApi> getMetadata(long id) {
        var req = new HttpGet(serverConfig.getBaseUrl() + "/files/metadata/" + id);
        return Single.fromCallable(() -> httpClient.execute(req, res -> {
            var status = new StatusLine(res);
            var body = res.getEntity().getContent();
            if (status.isSuccessful()) {
                return mapper.readValue(body, FileApi.class);
            } else {
                var message = mapper.readValue(body, ApiMessage.class).message();
                throw new BadFileResponseException(message);
            }
        }));
    }

    public Completable deleteFile(long id) {
        var req = new HttpDelete(serverConfig.getBaseUrl() + "/files/" + id);
        return Completable.fromCallable(() -> httpClient.execute(req, res -> {
            var status = new StatusLine(res);
            if (!status.isSuccessful()) {
                var message = mapper.readValue(res.getEntity().getContent(), ApiMessage.class).message();
                throw new BadFileResponseException(message);
            } else {
                EntityUtils.consume(res.getEntity());
            }
            return true;
        }));
    }

    public Single<ByteArrayInputStream> getFileContents(long id) {
        return Single.fromCallable(() -> {
            var req = new HttpGet(serverConfig.getBaseUrl() + "/files/" + id);
            return httpClient.execute(req, res -> {
                var status = new StatusLine(res);
                if (!status.isSuccessful()) {
                    var message = mapper.readValue(res.getEntity().getContent(), ApiMessage.class).message();
                    throw new BadFileResponseException(message);
                }
                // we can't read it as a string because that messes up the encoding
                return new ByteArrayInputStream(res.getEntity().getContent().readAllBytes());
            });
        });
    }

    public Single<FileApi> updateFile(UpdateFileRequest request) {
        var req = new HttpPut(serverConfig.getBaseUrl() + "/files");
        return Single.fromCallable(() -> {
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
        });
    }

    public Single<FileApi> createFile(CreateFileRequest request) {
        throw new UnsupportedOperationException();
    }
}
