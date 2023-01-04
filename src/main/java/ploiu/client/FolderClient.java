package ploiu.client;

import ploiu.config.AuthenticationConfig;
import ploiu.model.FolderApi;
import ploiu.service.FolderService;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public final class FolderClient {
    private final HttpClient client;

    public FolderClient(AuthenticationConfig authConfig) {
        this.client = HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(authConfig.getUsername(), authConfig.getPassword().toCharArray());
                    }
                })
                .build();
    }

    public FolderApi getFolder(Long id) {
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI(""))
    }
}
