package ploiu;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.FolderClient;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFolderRequestException;
import ploiu.model.FolderRequest;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderClientTests {
    @Mock
    ServerConfig serverConfig;
    @Spy
    HttpClient httpClient = HttpClients.createDefault();
    @InjectMocks
    FolderClient folderClient;

    MockWebServer backend;

    @BeforeEach
    void setupAll() throws Exception {
        backend = new MockWebServer();
        backend.start();
    }

    @AfterEach
    void teardownAll() throws IOException {
        backend.shutdown();
    }

    @Test
    void testGetFolderWithIdUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("{}"));
        folderClient.getFolder(1L);
        var req = backend.takeRequest();
        assertEquals("/folders/1", req.getPath());
    }

    @Test
    void testGetFolderUsesGet() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("{}"));
        folderClient.getFolder(null);
        var req = backend.takeRequest();
        assertEquals("GET", req.getMethod());
    }

    @Test
    void testGetFolderUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("{}"));
        folderClient.getFolder(null);
        var req = backend.takeRequest();
        assertEquals("/folders/null", req.getPath());
    }

    @Test
    void testCreateFolderUsesPost() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(201).setBody("{}"));
        folderClient.createFolder(new FolderRequest(Optional.empty(), Optional.empty(), "test"));
        var req = backend.takeRequest();
        assertEquals("POST", req.getMethod());
    }

    @Test
    void testCreateFolderUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(201).setBody("{}"));
        folderClient.createFolder(new FolderRequest(Optional.empty(), Optional.empty(), "test"));
        var req = backend.takeRequest();
        assertEquals("/folders", req.getPath());
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
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("{}"));
        folderClient.updateFolder(new FolderRequest(Optional.of(1L), Optional.empty(), "test"));
        var req = backend.takeRequest();
        assertEquals("PUT", req.getMethod());
    }

    @Test
    void testUpdateFolderUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("{}"));
        folderClient.updateFolder(new FolderRequest(Optional.of(1L), Optional.empty(), "test"));
        var req = backend.takeRequest();
        assertEquals("/folders", req.getPath());
    }

    @Test
    void testDeleteFolderRequiresPositiveId() {
        var exception = Assertions.assertThrows(BadFolderRequestException.class, () -> folderClient.deleteFolder(0L));
        assertEquals("id must be greater than 0", exception.getMessage());
    }

    @Test
    void testDeleteFolderUsesDelete() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(204));
        folderClient.deleteFolder(1L);
        var req = backend.takeRequest();
        assertEquals("DELETE", req.getMethod());
    }

    @Test
    void testDeleteFolderUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(204));
        folderClient.deleteFolder(1L);
        var req = backend.takeRequest();
        assertEquals("/folders/1", req.getPath());
    }

}
