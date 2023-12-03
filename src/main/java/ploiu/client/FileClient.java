package ploiu.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
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
import ploiu.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FileClient {
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
                }))
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
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
                }))
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    public Single<ByteArrayInputStream> getFileContents(long id) {
        if (id < 0) {
            return Single.error(new BadFileRequestException("Id cannot be negative."));
        }
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
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
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
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    // I don't like this and would prefer an Observable<FileApi>...but I'd have to change the backend server to allow streaming and that would take a lot of effort
    // also probably not worth it because of the small scale this project fits
    public Single<Collection<FileApi>> search(FileSearch query) {
        if (query.isEmpty()) {
            return Single.error(new BadFileRequestException("Query cannot be null or empty."));
        }
        var req = new HttpGet(serverConfig.getBaseUrl() + "/files/metadata" + query.toQueryString());
        return Single.fromCallable(() -> httpClient.execute(req, res -> {
                    var status = new StatusLine(res);
                    if (!status.isSuccessful()) {
                        var message = mapper.readValue(res.getEntity().getContent(), ApiMessage.class).message();
                        throw new BadFileResponseException(message);
                    }
                    return mapper.readValue(res.getEntity().getContent(), new TypeReference<Collection<FileApi>>() {
                    });
                }))
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
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
                .addBinaryBody("file", file, ContentType.create(mimeType == null ? "text/plain" : mimeType), file.getName().replace("(", "leftParenthese").replace(")", "rightParenthese"))
                .addTextBody("folderId", String.valueOf(request.folderId()));
        if (splitName.length > 1) {
            // file has extension, add it
            multipart.addTextBody("extension", splitName[splitName.length - 1]);
        }
        try (var built = multipart.build()) {
            var req = new HttpPost(serverConfig.getBaseUrl() + "/files" + (request.force() ? "?force" : ""));
            req.setEntity(built);
            return Single.just(
                            httpClient.execute(req, res -> {
                                var status = new StatusLine(res);
                                if (status.getStatusCode() != 201) {
                                    var message = mapper.readValue(res.getEntity().getContent(), ApiMessage.class).message();
                                    throw new BadFileResponseException(message);
                                }
                                // by returning the Single here, we prevent us from spamming the backend server all at once
                                return Single.just(mapper.readValue(res.getEntity().getContent(), FileApi.class));
                            })
                    )
                    .flatMap(it -> it)
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io());

        } catch (IOException e) {
            return Single.error(e);
        }
    }
}
