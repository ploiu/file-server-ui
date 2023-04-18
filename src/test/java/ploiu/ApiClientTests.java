package ploiu;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.ApiClient;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApiClientTests {
    @Mock
    ServerConfig serverConfig;
    @Spy
    HttpClient httpClient = HttpClients.createDefault();
    @Mock
    AuthenticationConfig authConfig;
    @InjectMocks
    ApiClient apiClient;

    static MockWebServer backend;

    @BeforeAll
    static void setupAll() throws IOException {
        backend = new MockWebServer();
        backend.start();
    }

    @BeforeEach
    void setupEach() {
        when(serverConfig.getBaseUrl()).thenReturn("http://localhost:" + backend.getPort());
    }

    @AfterAll
    static void teardownAll() throws IOException {
        backend.shutdown();
    }

    @Test
    void testGetApiInfoUsesGet() throws Exception {
        backend.enqueue(new MockResponse().setBody("{}"));
        apiClient.getApiInfo();
        verify(httpClient).execute(argThat(req -> req instanceof HttpGet), any(HttpClientResponseHandler.class));
    }

    @Test
    void testGetApiInfoCallsCorrectRoute() throws Exception {
        backend.enqueue(new MockResponse().setBody("{}"));
        apiClient.getApiInfo();
        verify(httpClient).execute(argThat(req -> req.getPath().equals("/api/version")), any(HttpClientResponseHandler.class));
    }

    @Test
    void testSetPasswordCallsCorrectRoute() throws Exception {
        backend.enqueue(new MockResponse().setResponseCode(201).setBody(""));
        apiClient.setPassword();
        verify(httpClient).execute(argThat(req -> req instanceof HttpPost && req.getPath().equals("/password")), any(HttpClientResponseHandler.class));
    }

    @Test
    void testSetPasswordIgnoresPasswordAlreadyReset() {
        backend.enqueue(new MockResponse().setResponseCode(400));
        var res = apiClient.setPassword();
        assertTrue(res);
    }
}
