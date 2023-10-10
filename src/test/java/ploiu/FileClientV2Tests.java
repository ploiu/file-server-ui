package ploiu;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.FileClientV2;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.CreateFileRequest;
import ploiu.model.UpdateFileRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileClientV2Tests {
    @Spy
    HttpClient httpClient = HttpClients.createDefault();
    @Mock
    ServerConfig serverConfig;

    @InjectMocks
    FileClientV2 fileClient;

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
        fileClient.getMetadata(-1)
                .doOnError(e -> {
                    assertInstanceOf(BadFileRequestException.class, e);
                    assertEquals("Id cannot be negative.", e.getMessage());
                })
                .subscribe();
    }

    @Test
    void testGetMetadataUsesGET() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("{}"));
        fileClient.getMetadata(1).blockingGet();
        verify(httpClient).execute(argThat(req -> req instanceof HttpGet), any(HttpClientResponseHandler.class));

    }

    @Test
    void testGetMetadataUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("{}"));
        fileClient.getMetadata(1).blockingGet();
        verify(httpClient).execute(argThat(req -> req.getPath().equals("/files/metadata/1")), any(HttpClientResponseHandler.class));
    }

    @Test
    void testGetMetadataThrowsExceptionIfFileIsNotFound() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(404).setBody("{\"message\":\"The file with the passed id could not be found.\"}"));
        var e = assertThrows(BadFileResponseException.class, () -> fileClient.getMetadata(1).blockingGet());
        assertEquals("The file with the passed id could not be found.", e.getMessage());
    }

    @Test
    void testDeleteFileThrowsExceptionIfIdIsNegative() {
        fileClient
                .deleteFile(-1)
                .doOnError(e -> {
                    assertInstanceOf(BadFileRequestException.class, e);
                    assertEquals("Id cannot be negative.", e.getMessage());
                })
                .subscribe();
    }

    @Test
    void testDeleteFileUsesDELETE() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(204));
        fileClient.deleteFile(1).blockingAwait();
        verify(httpClient).execute(argThat(req -> req instanceof HttpDelete), any(HttpClientResponseHandler.class));
    }

    @Test
    void testDeleteFileUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(204));
        fileClient.deleteFile(1).blockingAwait();
        verify(httpClient).execute(argThat(req -> req.getPath().equals("/files/1")), any(HttpClientResponseHandler.class));
    }

    @Test
    void testGetFileContentsThrowsExceptionIfIdIsNegative() {
        fileClient.getFileContents(-1)
                .doOnError(ex -> {
                    assertInstanceOf(BadFileRequestException.class, ex);
                    assertEquals("Id cannot be negative.", ex.getMessage());
                })
                .subscribe(ignored -> fail("getFileContents with negative id succeeded when it should fail" + ignored))
                .dispose();
    }

    @Test
    void testGetFileContentsUsesGET() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse());
        fileClient.getFileContents(1)
                .subscribe(ignored -> verify(httpClient).execute(argThat(req -> req instanceof HttpGet), any(HttpClientResponseHandler.class)))
                .dispose();

    }

    @Test
    void testGetFileContentsUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse());
        fileClient.getFileContents(1)
                .subscribe(ignored -> verify(httpClient).execute(argThat(req -> req.getPath().equals("/files/1")), any(HttpClientResponseHandler.class)))
                .dispose();
    }

    @ParameterizedTest(name = "Test that [{0}] is rejected for search")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\r", "\t"})
    void testSearchContentsThrowsExceptionIfQueryIsNullOrEmpty(String query) {
        fileClient.search(query)
                .doOnError(e -> {
                    assertInstanceOf(BadFileRequestException.class, e);
                    assertEquals("Query cannot be null or empty.", e.getMessage());
                })
                .subscribe();
    }

    @Test
    void testSearchContentsUsesGET() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("[]"));
        fileClient.search("whatever").blockingFirst();
        verify(httpClient).execute(argThat(req -> req instanceof HttpGet), any(HttpClientResponseHandler.class));
    }

    @Test
    void testSearchContentsUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setBody("[]"));
        fileClient.search("whatever").blockingFirst();
        verify(httpClient).execute(argThat(req -> req.getPath().equals("/files/metadata?search=whatever")), any(HttpClientResponseHandler.class));
    }

    @Test
    void testCreateFileRequiresNonNullFile() {
        var e = assertThrows(NullPointerException.class, () -> fileClient.createFile(new CreateFileRequest(0, null)).blockingGet());
        assertEquals("File cannot be null.", e.getMessage());
    }

    @Test
    void testCreateFileRequiresFileToExist() {
        fileClient.createFile(new CreateFileRequest(0, new File("bad file.bad file" + System.currentTimeMillis())))
                .doOnError(e -> {
                    assertInstanceOf(BadFileRequestException.class, e);
                    assertEquals("The selected file does not exist.", e.getMessage());
                }).subscribe();
    }

    @Test
    void testCreateFileUsesPOST() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(201).setBody("{}"));
        fileClient.createFile(new CreateFileRequest(0, new File(getClass().getClassLoader().getResource("test.txt").getPath()))).blockingGet();
        verify(httpClient).execute(argThat(req -> req instanceof HttpPost), any(HttpClientResponseHandler.class));
    }

    @Test
    void testCreateFileUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(201).setBody("{}"));
        fileClient.createFile(new CreateFileRequest(0, new File(getClass().getClassLoader().getResource("test.txt").getPath()))).blockingGet();
        verify(httpClient).execute(argThat(req -> req.getPath().equals("/files")), any(HttpClientResponseHandler.class));
    }

    @Test
    void testCreateFilePassesMultipart() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(201).setBody("{}"));
        fileClient.createFile(new CreateFileRequest(0, new File(getClass().getClassLoader().getResource("test.txt").getPath()))).blockingGet();
        var req = backend.takeRequest();
        assertTrue(req.getHeader("Content-Type").contains("multipart/form-data"));
        try (var reader = new BufferedReader(new InputStreamReader(req.getBody().inputStream()))) {
            var body = reader.lines().collect(Collectors.joining("\n"));
            // make sure extension is passed
            assertTrue(Pattern.compile("name=\"extension\"").matcher(body).find());
        }
    }

    @Test
    void testCreateFileWithoutExtensionDoesNotIncludeTrailingDot() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(201).setBody("{}"));
        fileClient.createFile(new CreateFileRequest(0, new File(getClass().getClassLoader().getResource("testNoExtension").getPath()))).blockingGet();
        var req = backend.takeRequest();
        try (var reader = new BufferedReader(new InputStreamReader(req.getBody().inputStream()))) {
            var body = reader.lines().collect(Collectors.joining("\n"));
            assertFalse(Pattern.compile("name=\"extension\"").matcher(body).find());
        }
    }

    @Test
    void testUpdateFileUsesPUT() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        fileClient.updateFile(new UpdateFileRequest(0, 0, "test")).blockingGet();
        var req = backend.takeRequest();
        assertEquals("PUT", req.getMethod());
    }

    @Test
    void testUpdateFileUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        fileClient.updateFile(new UpdateFileRequest(0, 0, "test")).blockingGet();
        var req = backend.takeRequest();
        assertEquals("/files", req.getPath());
    }

    @Test
    void testUpdateFilePassesBody() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
        backend.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        var reqBody = new UpdateFileRequest(0, 0, "test");
        fileClient.updateFile(reqBody).blockingGet();
        var req = backend.takeRequest();
        var receivedBody = new ObjectMapper().readValue(req.getBody().inputStream(), UpdateFileRequest.class);
        assertEquals(reqBody, receivedBody);
    }

    @ParameterizedTest(name = "Test that [{0}] is not accepted as a file name for update")
    @EmptySource
    @ValueSource(strings = {" "})
    void testUpdateFileRequiresFileName(String name) {
        fileClient.updateFile(new UpdateFileRequest(0, 0, name))
                .doOnError(e -> {
                    assertInstanceOf(BadFileRequestException.class, e);
                    assertEquals("Name cannot be blank.", e.getMessage());
                })
                .subscribe();
    }

    @Test
    void testUpdateFileRequiresPositiveId() {
        fileClient.updateFile(new UpdateFileRequest(-1, 0, "name"))
                .doOnError(e -> {
                    assertInstanceOf(BadFileRequestException.class, e);
                    assertEquals("Id cannot be negative.", e.getMessage());
                })
                .subscribe();
    }

}