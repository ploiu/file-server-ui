package ploiu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.ApiClient;
import ploiu.config.ServerConfig;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApiClientTests {
    @Mock
    ServerConfig serverConfig;
    @Mock
    HttpClient httpClient;
    @InjectMocks
    ApiClient apiClient;

    @Mock
    HttpResponse dummyResponse;

    @Test
    void testGetApiInfoUsesGet() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        when(dummyResponse.statusCode()).thenReturn(200);
        when(dummyResponse.body()).thenReturn("{}");
        apiClient.getApiInfo();
        Mockito.verify(httpClient).send(argThat(req -> req.method().equals("GET")), any());
    }

    @Test
    void testGetApiInfoCallsCorrectRoute() throws Exception {
        when(serverConfig.getBaseUrl()).thenReturn("https://www.example.com");
        when(httpClient.send(any(), any())).thenReturn(dummyResponse);
        when(dummyResponse.statusCode()).thenReturn(200);
        when(dummyResponse.body()).thenReturn("{}");
        apiClient.getApiInfo();
        Mockito.verify(httpClient).send(argThat(req -> req.uri().toString().endsWith("/api/version")), any());
    }
}
