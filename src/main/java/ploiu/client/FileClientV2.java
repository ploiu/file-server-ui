package ploiu.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class FileClientV2 {
    private final HttpClient httpClient;
    private final ServerConfig serverConfig;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());

    public Single<FileApi> getMetadata(long id) {
        if (id < 0) {
            return Single.error(new BadFileRequestException("Id cannot be negative."));
        }
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
        })).observeOn(Schedulers.newThread());
    }

    public Completable deleteFile(long id) {
        if (id < 0) {
            return Completable.error(new BadFileRequestException("Id cannot be negative."));
        }
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
        })).observeOn(Schedulers.newThread());
    }

    public Observable<? extends InputStream> getFileContents(long id) {
        if (id < 0) {
            return Observable.error(new BadFileRequestException("Id cannot be negative."));
        }
        return Observable.fromCallable(() -> {
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
        }).observeOn(Schedulers.newThread());
    }

    public Single<FileApi> updateFile(UpdateFileRequest request) {
        if (request.id() < 0) {
            return Single.error(new BadFileRequestException("Id cannot be negative."));
        }
        if (request.name().isBlank()) {
            return Single.error(new BadFileRequestException("Name cannot be blank."));
        }
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
        }).observeOn(Schedulers.newThread());
    }

    public Observable<Collection<FileApi>> search(String query) {
        if (query == null || query.isBlank()) {
            return Observable.error(new BadFileRequestException("Query cannot be null or empty."));
        }
        var req = new HttpGet(serverConfig.getBaseUrl() + "/files/metadata?search=" + query);
        return Observable.fromCallable(() -> httpClient.execute(req, res -> {
                    var status = new StatusLine(res);
                    if (!status.isSuccessful()) {
                        var message = mapper.readValue(res.getEntity().getContent(), ApiMessage.class).message();
                        throw new BadFileResponseException(message);
                    }
                    return mapper.readValue(res.getEntity().getContent(), new TypeReference<Collection<FileApi>>() {
                    });
                }))
                .observeOn(Schedulers.newThread());
    }

    public Single<FileApi> createFile(CreateFileRequest request) {
        var file = request.file();
        Objects.requireNonNull(file, "File cannot be null.");
        if (!file.exists()) {
            return Single.error(new BadFileRequestException("The selected file does not exist."));
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
        return Single.fromCallable(() -> {
            try (var built = multipart.build()) {
                var req = new HttpPost(serverConfig.getBaseUrl() + "/files");
                req.setEntity(built);
                return httpClient.execute(req, res -> {
                    var status = new StatusLine(res);
                    if (status.getStatusCode() != 201) {
                        var message = mapper.readValue(res.getEntity().getContent(), ApiMessage.class).message();
                        throw new BadFileResponseException(message);
                    }
                    return mapper.readValue(res.getEntity().getContent(), FileApi.class);
                });

            }
        }).observeOn(Schedulers.newThread());
    }
}
