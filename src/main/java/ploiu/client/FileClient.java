package ploiu.client;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.model.FileApi;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class FileClient {
    private final HttpClient httpClient;
    private final ServerConfig serverConfig;
    private final AuthenticationConfig authenticationConfig;

    public Optional<FileApi> getMetadata(long id) {
        throw new UnsupportedOperationException();
    }

    public boolean deleteFile(long id) {
        throw new UnsupportedOperationException();
    }

    public Optional<InputStream> downloadFile(long id) {
        throw new UnsupportedOperationException();
    }

    public Optional<FileApi> updateFile(FileApi request) {
        throw new UnsupportedOperationException();
    }

    public Collection<FileApi> search(String query) {
        throw new UnsupportedOperationException();
    }
}
