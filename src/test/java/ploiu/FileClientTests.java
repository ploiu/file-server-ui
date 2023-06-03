package ploiu;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.FileClient;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.CreateFileRequest;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class FileClientTests {
    @Spy
    HttpClient httpClient = HttpClients.createDefault();
    @Mock
    ServerConfig serverConfig;
    @Mock
    AuthenticationConfig authConfig;

    @InjectMocks
    FileClient fileClient;

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
    void testGetMetadataThrowsExceptionIfIdIsNegative() {
        var e = assertThrows(BadFileRequestException.class, () -> fileClient.getMetadata(-1));
        assertEquals("Id cannot be negative.", e.getMessage());
    }

    @Test
    void testGetMetadataUsesGET() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("{}"));
        fileClient.getMetadata(1);
        verify(httpClient).execute(argThat(req -> req instanceof HttpGet), any(HttpClientResponseHandler.class));

    }

    @Test
    void testGetMetadataUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("{}"));
        fileClient.getMetadata(1);
        verify(httpClient).execute(argThat(req -> req.getPath().equals("/files/metadata/1")), any(HttpClientResponseHandler.class));
    }

    @Test
    void testGetMetadataThrowsExceptionIfFileIsNotFound() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(404).setBody("{\"message\":\"The file with the passed id could not be found.\"}"));
        var e = assertThrows(BadFileResponseException.class, () -> fileClient.getMetadata(1));
        assertEquals("The file with the passed id could not be found.", e.getMessage());
    }

    @Test
    void testDeleteFileThrowsExceptionIfIdIsNegative() {
        var e = assertThrows(BadFileRequestException.class, () -> fileClient.deleteFile(-1));
        assertEquals("Id cannot be negative.", e.getMessage());
    }

    @Test
    void testDeleteFileUsesDELETE() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(204));
        fileClient.deleteFile(1);
        verify(httpClient).execute(argThat(req -> req instanceof HttpDelete), any(HttpClientResponseHandler.class));
    }

    @Test
    void testDeleteFileUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(204));
        fileClient.deleteFile(1);
        verify(httpClient).execute(argThat(req -> req.getPath().equals("/files/1")), any(HttpClientResponseHandler.class));
    }

    @Test
    void testGetFileContentsThrowsExceptionIfIdIsNegative() {
        var e = assertThrows(BadFileRequestException.class, () -> fileClient.getFileContents(-1));
        assertEquals("Id cannot be negative.", e.getMessage());
    }

    @Test
    void testGetFileContentsUsesGET() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse());
        fileClient.getFileContents(1);
        verify(httpClient).execute(argThat(req -> req instanceof HttpGet), any(HttpClientResponseHandler.class));
    }

    @Test
    void testGetFileContentsUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse());
        fileClient.getFileContents(1);
        verify(httpClient).execute(argThat(req -> req.getPath().equals("/files/1")), any(HttpClientResponseHandler.class));
    }

    @ParameterizedTest(name = "Test that [{0}] is rejected for search")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\r", "\t"})
    void testSearchContentsThrowsExceptionIfQueryIsNullOrEmpty(String query) {
        var e = assertThrows(BadFileRequestException.class, () -> fileClient.search(query));
        assertEquals("Query cannot be null or empty.", e.getMessage());
    }

    @Test
    void testSearchContentsUsesGET() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("[]"));
        fileClient.search("whatever");
        verify(httpClient).execute(argThat(req -> req instanceof HttpGet), any(HttpClientResponseHandler.class));
    }

    @Test
    void testSearchContentsUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("[]"));
        fileClient.search("whatever");
        verify(httpClient).execute(argThat(req -> req.getPath().equals("/files/metadata?search=whatever")), any(HttpClientResponseHandler.class));
    }

    @Test
    void testCreateFileRequiresNonNullFile() {
        var e = assertThrows(NullPointerException.class, () -> fileClient.createFile(new CreateFileRequest(0, null)));
        assertEquals("File cannot be null.", e.getMessage());
    }

    @Test
    void testCreateFileRequiresFileToExist() {
        var e = assertThrows(BadFileRequestException.class, () -> fileClient.createFile(new CreateFileRequest(0, new File("bad file.bad file" + System.currentTimeMillis()))));
        assertEquals("The selected file does not exist.", e.getMessage());
    }

    @Test
    void testCreateFileUsesPOST() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(201).setBody("{}"));
        fileClient.createFile(new CreateFileRequest(0, new File(getClass().getClassLoader().getResource("test.txt").getPath())));
        verify(httpClient).execute(argThat(req -> req instanceof HttpPost), any(HttpClientResponseHandler.class));
    }

    @Test
    void testCreateFileUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(201).setBody("{}"));
        fileClient.createFile(new CreateFileRequest(0, new File(getClass().getClassLoader().getResource("test.txt").getPath())));
        verify(httpClient).execute(argThat(req -> req.getPath().equals("/files")), any(HttpClientResponseHandler.class));
    }

    @Test
    void testCreateFilePassesMultipart() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(201).setBody("{}"));
        fileClient.createFile(new CreateFileRequest(0, new File(getClass().getClassLoader().getResource("test.txt").getPath())));
        var req = backend.takeRequest();
        assertTrue(req.getHeader("Content-Type").contains("multipart/form-data"));
    }

}
