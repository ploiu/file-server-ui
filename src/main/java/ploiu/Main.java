package ploiu;

import com.fasterxml.jackson.databind.ObjectMapper;
import ploiu.client.FolderClient;
import ploiu.config.AuthenticationConfig;
import ploiu.config.ServerConfig;

import java.net.http.HttpClient;

public class Main {
    public static void main(String[] args) throws Exception {
        var folderClient = setupFolderClient();
        var folder = folderClient.getFolder(1L).get();
        System.out.println(new ObjectMapper().writeValueAsString(folder));
    }

    private static FolderClient setupFolderClient() {
        return new FolderClient(HttpClient.newBuilder().build(), new AuthenticationConfig(), new ServerConfig());
    }
}