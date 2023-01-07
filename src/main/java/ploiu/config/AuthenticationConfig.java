package ploiu.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

@Slf4j
public class AuthenticationConfig {
    @Getter
    private final String username;
    @Getter
    private final String password;

    public AuthenticationConfig() {
        var props = new Properties();
        try (var inStream = getClass().getClassLoader().getResourceAsStream("app.properties")) {
            props.load(inStream);
            this.username = props.getProperty("auth.username");
            this.password = props.getProperty("auth.password");
        } catch (IOException e) {
            log.error("Failed to read properties file", e);
            throw new RuntimeException(e);
        }
    }

    public String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes());
    }
}
