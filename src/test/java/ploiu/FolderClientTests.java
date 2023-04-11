package ploiu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.FolderClient;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFolderRequestException;
import ploiu.model.FolderRequest;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderClientTests {
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
        verify(httpClient).send(argThat(req -> req.uri().toString().endsWith("/1")), any());
    }

    @Test
    void testGetFolderPassesAuth() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        folderClient.getFolder(null);
        verify(httpClient).send(argThat(req -> req.headers().firstValue("Authorization").get().equals("Basic dGVzdDp0ZXN0")), any());
    }

    @Test
    void testGetFolderUsesGet() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        folderClient.getFolder(null);
        verify(httpClient).send(argThat(req -> req.method().equals("GET")), any());
    }

    @Test
    void testGetFolderUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        folderClient.getFolder(null);
        verify(httpClient).send(argThat(req -> req.uri().toString().endsWith("/folders/null")), any());
    }

    @Test
    void testCreateFolderUsesPost() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        when(dummyResponse.statusCode()).thenReturn(201);
        when(dummyResponse.body()).thenReturn("{}");
        folderClient.createFolder(new FolderRequest(Optional.empty(), Optional.empty(), "test"));
        verify(httpClient).send(argThat(req -> req.method() == "POST"), any());
    }

    @Test
    void testCreateFolderUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        when(dummyResponse.statusCode()).thenReturn(201);
        when(dummyResponse.body()).thenReturn("{}");
        folderClient.createFolder(new FolderRequest(Optional.empty(), Optional.empty(), "test"));
        verify(httpClient).send(argThat(req -> req.uri().toString().endsWith("/folders")), any());
    }

    @Test
    void testCreateFolderPassesAuth() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        when(dummyResponse.statusCode()).thenReturn(201);
        when(dummyResponse.body()).thenReturn("{}");
        folderClient.createFolder(new FolderRequest(Optional.empty(), Optional.empty(), "test"));
        verify(httpClient).send(argThat(req -> req.headers().firstValue("Authorization").get().equals("Basic dGVzdDp0ZXN0")), any());
    }

    @Test
    void testUpdateFolderRequiresFolderId() {
        var request = new FolderRequest(Optional.empty(), Optional.empty(), "test");
        var exception = Assertions.assertThrows(BadFolderRequestException.class, () -> folderClient.updateFolder(request));
        assertEquals("Cannot update folder without id", exception.getMessage());
    }

    @Test
    void testUpdateFolderRequiresNonZeroId() {
        var request = new FolderRequest(Optional.of(0L), Optional.empty(), "test");
        var exception = Assertions.assertThrows(BadFolderRequestException.class, () -> folderClient.updateFolder(request));
        assertEquals("0 is the root folder id, and cannot be updated", exception.getMessage());
    }

    @Test
    void testUpdateFolderUsesPut() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        when(dummyResponse.statusCode()).thenReturn(200);
        when(dummyResponse.body()).thenReturn("{}");
        folderClient.updateFolder(new FolderRequest(Optional.of(1L), Optional.empty(), "test"));
        verify(httpClient).send(argThat(req -> req.method() == "PUT"), any());
    }

    @Test
    void testUpdateFolderUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        when(dummyResponse.statusCode()).thenReturn(200);
        when(dummyResponse.body()).thenReturn("{}");
        folderClient.updateFolder(new FolderRequest(Optional.of(1L), Optional.empty(), "test"));
        verify(httpClient).send(argThat(req -> req.uri().toString().endsWith("/folders")), any());
    }

    @Test
    void testUpdateFolderPassesAuth() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        when(dummyResponse.statusCode()).thenReturn(200);
        when(dummyResponse.body()).thenReturn("{}");
        folderClient.updateFolder(new FolderRequest(Optional.of(1L), Optional.empty(), "test"));
        verify(httpClient).send(argThat(req -> req.headers().firstValue("Authorization").get().equals("Basic dGVzdDp0ZXN0")), any());
    }

    @Test
    void testDeleteFolderRequiresPositiveId() {
        var exception = Assertions.assertThrows(BadFolderRequestException.class, () -> folderClient.deleteFolder(0L));
        assertEquals("id must be greater than 0", exception.getMessage());
    }

    @Test
    void testDeleteFolderUsesDelete() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        when(dummyResponse.statusCode()).thenReturn(204);
        folderClient.deleteFolder(1L);
        verify(httpClient).send(argThat(req -> req.method() == "DELETE"), any());
    }

    @Test
    void testDeleteFolderUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        when(dummyResponse.statusCode()).thenReturn(204);
        folderClient.deleteFolder(1L);
        verify(httpClient).send(argThat(req -> req.uri().toString().endsWith("/folders/1")), any());
    }

    @Test
    void testDeleteFolderPassesAuth() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        when(dummyResponse.statusCode()).thenReturn(204);
        folderClient.deleteFolder(1L);
        verify(httpClient).send(argThat(req -> req.headers().firstValue("Authorization").get().equals("Basic dGVzdDp0ZXN0")), any());
    }
}
