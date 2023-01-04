package ploiu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.FolderClient;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileClientTests {
    @Mock
    AuthenticationConfig authConfig;
    @Mock
    ServerConfig serverConfig;
    @Mock
    HttpClient httpClient;
    @Mock
    HttpResponse dummyResponse;
    @InjectMocks
    FolderClient folderClient;

    @Test
    void testGetFolderWithId() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("whatever");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        folderClient.getFolder(1L);
        Mockito.verify(httpClient).send(ArgumentMatchers.argThat(req -> req.uri().toString().endsWith("/1")), any());
    }
}
