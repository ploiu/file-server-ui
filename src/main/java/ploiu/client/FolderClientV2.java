package ploiu.client;

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
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFolderRequestException;
import ploiu.exception.BadFolderResponseException;
import ploiu.model.ApiMessage;
import ploiu.model.FolderApi;
import ploiu.model.FolderRequest;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__({@Inject}))
public class FolderClientV2 {
    private final HttpClient client;
    private final ServerConfig serverConfig;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());

    public Single<FolderApi> getFolder(long id) {
        return Single.fromCallable(() -> {
                    var req = new HttpGet(serverConfig.getBaseUrl() + "/folders/" + id);
                    return client.execute(req, res -> {
                        var status = new StatusLine(res);
                        if (status.getStatusCode() == 200) {
                            return mapper.readValue(res.getEntity().getContent(), FolderApi.class);
                        } else {
                            throw new BadFolderResponseException("Failed to retrieve folder.");
                        }
                    });
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    public Single<FolderApi> createFolder(FolderRequest request) throws BadFolderRequestException, BadFolderResponseException {
        return Single.fromCallable(() -> {
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
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    public Single<FolderApi> updateFolder(FolderRequest folder) throws BadFolderRequestException, BadFolderResponseException {
        if (folder.id().isEmpty()) {
            return Single.error(new BadFolderRequestException("Cannot update folder without id"));
        }
        if (folder.id().get() == 0) {
            return Single.error(new BadFolderRequestException("0 is the root folder id, and cannot be updated"));
        }
        return Single.fromCallable(() -> {
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
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteFolder(long id) throws BadFolderRequestException, BadFolderResponseException {
        if (id < 1) {
            return Completable.error(new BadFolderRequestException("id must be greater than 0"));
        }
        return Completable.fromCallable(() -> {
                    var req = new HttpDelete(serverConfig.getBaseUrl() + "/folders/" + id);
                    return client.execute(req, res -> {
                        var status = new StatusLine(res);
                        if (status.getStatusCode() == 204) {
                            return true;
                        }
                        var error = mapper.readValue(res.getEntity().getContent(), ApiMessage.class);
                        throw new BadFolderResponseException(error.message());
                    });
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }
}
