package ploiu.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Slf4j
@Getter
public final class ServerConfig {
    private String baseUrl;

    public ServerConfig() {
        var props = new Properties();
        try(var inStream = new FileInputStream("app.properties")) {
            props.load(inStream);
            var address = props.getProperty("server.address");
            var port = props.getProperty("server.port");
            this.baseUrl = String.format("https://%s:%s", address, port);
        } catch(IOException e) {
            log.error("Failed to read properties file", e);
            throw new RuntimeException(e);
        }
    }
}
