package ploiu.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import ploiu.client.FolderClient;
import ploiu.model.FolderApi;

import java.io.IOException;

public class MainFrame extends AnchorPane {
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private FlowPane itemPane;

    public MainFrame() {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/MainFrame.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            loadInitialFolder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadInitialFolder() {
        var folderClient = App.INJECTOR.getInstance(FolderClient.class);
        var folder = folderClient.getFolder(null);
        // null folder is the root folder, so this will always exist
        setFolderView(folder.get());
    }

    private void setFolderView(FolderApi folder) {
        itemPane.getChildren().clear();
        folder.folders()
                .stream()
                .map(FolderEntry::new)
                .forEach(itemPane.getChildren()::add);
    }

}
