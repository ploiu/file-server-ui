package ploiu.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;

@SuppressWarnings("unused")
public class ConfigModule extends AbstractModule {
    @Provides
    AuthenticationConfig authConfig() {
        return new AuthenticationConfig();
    }

    @Provides
    ServerConfig serverConfig() {
        return new ServerConfig();
    }
}
