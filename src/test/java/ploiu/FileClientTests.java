package ploiu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.FileClient;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
        fail();
    }

    @Test
    void testDeleteFileUsesDELETE() throws Exception {
        fail();
    }

    @Test
    void testDeleteFileUsesCorrectPath() throws Exception {
        fail();
    }

    @Test
    void testDeleteFilePassesAuth() throws Exception {
        fail();
    }

}
