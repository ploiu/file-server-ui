package ploiu.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class ServerConfig {
    private final String baseUrl;
    private final Pattern versionMatcher;
    private final String compatibleVersion;
    private final String host;
    private final int port;

    public ServerConfig() {
        var props = new Properties();
        try (var inStream = getClass().getClassLoader().getResourceAsStream("app.properties")) {
            props.load(inStream);
            this.host = props.getProperty("server.address");
            this.port = Integer.parseInt(props.getProperty("server.port"));
            this.baseUrl = String.format("https://%s:%s", host, port);
            this.versionMatcher = generateCompatibleVersionPattern(props);
            this.compatibleVersion = props.getProperty("server.compatible.version");
        } catch (IOException e) {
            log.error("Failed to read properties file", e);
            throw new RuntimeException(e);
        }
    }

    private Pattern generateCompatibleVersionPattern(Properties props) {
        // generate pattern for each version number and join together for pattern
        var version = props.getProperty("server.compatible.version");
        // our regex builder will only work if the whole version follows the right property
        var versionMatcher = Pattern.compile("^\\d+(\\.(\\d+|x)){2}$");
        if (!versionMatcher.matcher(version).find()) {
            throw new RuntimeException("Bad compatible version " + version + ". Version must follow the format #.(#|x).(#|x) format. e.g. 1.2.x");
        }
        var versionRegex = Arrays.stream(version.split("\\."))
                .map(part -> part.replace("x", "\\d+"))
                .collect(Collectors.joining("."));
        return Pattern.compile(versionRegex);
    }
}
