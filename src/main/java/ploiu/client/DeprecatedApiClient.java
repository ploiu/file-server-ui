package ploiu.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.exception.ServerUnavailableException;
import ploiu.model.ApiInfo;
import ploiu.model.CreatePasswordRequest;

import java.io.IOException;

@Slf4j
@Deprecated(forRemoval = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DeprecatedApiClient {
    private final ServerConfig serverConfig;
    private final org.apache.hc.client5.http.classic.HttpClient httpClient;
    private final AuthenticationConfig authConfig;

    public ApiInfo getApiInfo() {
        try {
            var req = new HttpGet(serverConfig.getBaseUrl() + "/api/version");
            return httpClient.execute(req, res -> {
                var status = new StatusLine(res);
                if (status.getStatusCode() == 200) {
                    var entity = res.getEntity();
                    return new ObjectMapper().readValue(entity.getContent(), ApiInfo.class);
                }
                // only way this fails is if the server is down
                throw new ServerUnavailableException();
            });
        } catch (IOException e) {
            log.error("Failed to get api info from server", e);
            throw new RuntimeException(e);
        }
    }

    public boolean setPassword() {
        try {
            var body = new ObjectMapper().writeValueAsString(new CreatePasswordRequest(authConfig.getUsername(), authConfig.getPassword()));
            var req = new HttpPost(serverConfig.getBaseUrl() + "/password");
            req.setEntity(new StringEntity(body));
            return httpClient.execute(req, res -> {
                var status = new StatusLine(res);
                EntityUtils.consume(res.getEntity());
                // 400 means a password is already set, so it's ignorable
                return status.getStatusCode() == 201 || status.getStatusCode() == 400;
            });
        } catch (IOException e) {
            log.error("unforeseen error when setting password", e);
            throw new RuntimeException(e);
        }
    }

    public boolean isCompatibleWithServer() {
        log.info("Checking if server is compatible with client (looking for pattern " + serverConfig.getCompatibleVersion() + ")");
        var serverVersion = this.getApiInfo().version();
        log.info("server version is " + serverVersion);
        var matches = serverConfig.getVersionMatcher().matcher(serverVersion).find();
        if (matches) {
            log.info("We're compatible!");
        } else {
            log.error("Incompatible with current server version.");
        }
        return matches;
    }

}
