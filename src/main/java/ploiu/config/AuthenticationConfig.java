package ploiu.config;

import lombok.Getter;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

@Slf4j
@Getter
public class AuthenticationConfig {
    private final String username;
    private final String password;

    public AuthenticationConfig() {
        var props = new Properties();
        try(var inStream = new FileInputStream("app.properties")) {
            props.load(inStream);
            this.username = props.getProperty("auth.username");
            this.password = props.getProperty("auth.password");
        } catch(IOException e) {
            log.error("Failed to read properties file", e);
            throw new RuntimeException(e);
        }
    }
}
