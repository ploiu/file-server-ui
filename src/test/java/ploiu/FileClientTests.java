package ploiu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.FileClient;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.CreateFileRequest;

import java.io.File;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class FileClientTests {
    @Mock
    private HttpClient httpClient;
    @Mock
    private ServerConfig serverConfig;
    @Mock
    private AuthenticationConfig authConfig;

    @InjectMocks
    private FileClient fileClient;

    @Mock
    private HttpResponse mockResponse;

    @Test
    void testGetMetadataThrowsExceptionIfIdIsNegative() {
        var e = assertThrows(BadFileRequestException.class, () -> fileClient.getMetadata(-1));
        assertEquals("Id cannot be negative.", e.getMessage());
    }

    @Test
    void testGetMetadataUsesGET() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{}");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.getMetadata(1);
        verify(httpClient).send(argThat(req -> req.method().equals("GET")), any());
    }

    @Test
    void testGetMetadataUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{}");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.getMetadata(1);
        verify(httpClient).send(argThat(req -> req.uri().toString().endsWith("/files/metadata/1")), any());
    }

    @Test
    void testGetMetadataPassesAuth() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{}");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.getMetadata(1);
        verify(httpClient).send(argThat(req -> req.headers().firstValue("Authorization").get().equals("Basic dGVzdDp0ZXN0")), any());
    }

    @Test
    void testGetMetadataThrowsExceptionIfFileIsNotFound() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(404);
        when(mockResponse.body()).thenReturn("{\"message\":\"The file with the passed id could not be found.\"}");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
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
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(204);
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.deleteFile(1);
        verify(httpClient).send(argThat(req -> req.method().equals("DELETE")), any());
    }

    @Test
    void testDeleteFileUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(204);
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.deleteFile(1);
        verify(httpClient).send(argThat(req -> req.uri().toString().endsWith("/files/1")), any());
    }

    @Test
    void testDeleteFilePassesAuth() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(204);
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.deleteFile(1);
        verify(httpClient).send(argThat(req -> req.headers().firstValue("Authorization").get().equals("Basic dGVzdDp0ZXN0")), any());
    }

    @Test
    void testGetFileContentsThrowsExceptionIfIdIsNegative() {
        var e = assertThrows(BadFileRequestException.class, () -> fileClient.getFileContents(-1));
        assertEquals("Id cannot be negative.", e.getMessage());
    }

    @Test
    void testGetFileContentsPassesAuth() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(200);
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.getFileContents(1);
        verify(httpClient).send(argThat(req -> req.headers().firstValue("Authorization").get().equals("Basic dGVzdDp0ZXN0")), any());
    }

    @Test
    void testGetFileContentsUsesGET() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(200);
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.getFileContents(1);
        verify(httpClient).send(argThat(req -> req.method().equals("GET")), any());
    }

    @Test
    void testGetFileContentsUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(200);
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.getFileContents(1);
        verify(httpClient).send(argThat(req -> req.uri().toString().endsWith("/files/1")), any());
    }

    @ParameterizedTest(name = "Test that [{0}] is rejected for search")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\r", "\t"})
    void testSearchContentsThrowsExceptionIfQueryIsNullOrEmpty(String query) {
        var e = assertThrows(BadFileRequestException.class, () -> fileClient.search(query));
        assertEquals("Query cannot be null or empty.", e.getMessage());
    }

    @Test
    void testSearchContentsPassesAuth() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[]");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.search("whatever");
        verify(httpClient).send(argThat(req -> req.headers().firstValue("Authorization").get().equals("Basic dGVzdDp0ZXN0")), any());
    }

    @Test
    void testSearchContentsUsesGET() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[]");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.search("whatever");
        verify(httpClient).send(argThat(req -> req.method().equals("GET")), any());
    }

    @Test
    void testSearchContentsUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("[]");
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        fileClient.search("whatever");
        verify(httpClient).send(argThat(req -> req.uri().toString().endsWith("/files/metadata?search=whatever")), any());
    }

    @Test
    void testCreateFileRequiresNonNullFile() {
        var e = assertThrows(NullPointerException.class, () -> fileClient.createFile(new CreateFileRequest(null, null)));
        assertEquals("File cannot be null.", e.getMessage());
    }

    @Test
    void testCreateFileRequiresFileToExist() {
        var e = assertThrows(BadFileRequestException.class, () -> fileClient.createFile(new CreateFileRequest(null, new File("bad file.bad file" + System.currentTimeMillis()))));
        assertEquals("The selected file does not exist.", e.getMessage());
    }

    @Test
    void testCreateFilePassesAuth() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(201);
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn("{}");
        fileClient.createFile(new CreateFileRequest(null, new File(getClass().getClassLoader().getResource("test.txt").getPath())));
        verify(httpClient).send(argThat(req -> req.headers().firstValue("Authorization").get().equals("Basic dGVzdDp0ZXN0")), any());
    }

    @Test
    void testCreateFileUsesPOST() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(201);
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn("{}");
        fileClient.createFile(new CreateFileRequest(null, new File(getClass().getClassLoader().getResource("test.txt").getPath())));
        verify(httpClient).send(argThat(req -> req.method().equals("POST")), any());
    }

    @Test
    void testCreateFileUsesCorrectPath() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(201);
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn("{}");
        fileClient.createFile(new CreateFileRequest(null, new File(getClass().getClassLoader().getResource("test.txt").getPath())));
        verify(httpClient).send(argThat(req -> req.uri().toString().endsWith("/files")), any());
    }

    @Test
    void testCreateFilePassesMultipart() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(mockResponse.statusCode()).thenReturn(201);
        when(authConfig.basicAuth()).thenReturn("Basic dGVzdDp0ZXN0");
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn("{}");
        fileClient.createFile(new CreateFileRequest(null, new File(getClass().getClassLoader().getResource("test.txt").getPath())));
        verify(httpClient).send(argThat(req -> req.headers().firstValue("Content-Type").get().contains("multipart/form-data; boundary=")), any());
    }

}
