package ploiu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import ploiu.client.ApiClient;
import ploiu.module.ConfigModule;
import ploiu.module.HttpModule;

public class Main {
    public static void main(String[] args) throws Exception {
        var injector = Guice.createInjector(new ConfigModule(), new HttpModule());
        var client = injector.getInstance(ApiClient.class);
        System.out.println(new ObjectMapper().writeValueAsString(client.getApiInfo()));
    }

}