package ploiu.service;

import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ploiu.client.ApiClient;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.model.ApiInfo;
import ploiu.model.CreatePasswordRequest;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ApiService {
    private final ApiClient client;
    private final ServerConfig serverConfig;
    private final AuthenticationConfig authConfig;

    public Completable setPassword() {
        return Single.just(new CreatePasswordRequest(authConfig.getUsername(), authConfig.getPassword()))
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(client::setPassword);
    }

    public Single<ApiInfo> getApiInfo() {
        return client.getApiInfo()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());
    }

    public Single<Boolean> isCompatibleWithServer() {
        log.info("Checking if server is compatible with client (looking for pattern {})", serverConfig.getCompatibleVersion());
        return getApiInfo()
                .map(ApiInfo::version)
                .map(version -> {
                    log.info("server version is {}", version);
                    var matches = serverConfig.getVersionMatcher().matcher(version).find();
                    if (matches) {
                        log.info("We're compatible!");
                    } else {
                        log.error("Incompatible with current server version.");
                    }
                    return matches;
                })
            .doOnError(e -> log.error("Failed to check server version", e));
    }
}
