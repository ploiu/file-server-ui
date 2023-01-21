package ploiu.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import ploiu.client.FileClient;
import ploiu.client.FolderClient;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.FileApi;
import ploiu.model.FolderApi;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ploiu.Constants.CACHE_DIR;
import static ploiu.util.DialogUtils.showErrorDialog;
import static ploiu.util.ThreadUtils.runInThread;

public class MainFrame extends AnchorPane {
    private final FolderClient folderClient = App.INJECTOR.getInstance(FolderClient.class);
    private final FileClient fileClient = App.INJECTOR.getInstance(FileClient.class);
    private final Desktop desktop = Desktop.getDesktop();
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
        var folder = folderClient.getFolder(null);
        // null folder is the root folder, so this will always exist
        setFolderView(folder.get());
    }

    private void setFolderView(FolderApi folder) {
        itemPane.getChildren().clear();
        var folderEntries = folder.folders()
                .stream()
                .map(FolderEntry::new)
                .toList();
        for (FolderEntry folderEntry : folderEntries) {
            // when clicking any of the folder entries, clear the page and populate it with the new folder contents
            folderEntry.setOnMouseClicked(mouseEvent -> {
                // left click is used for entry, right click is used for modifying properties
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    itemPane.getChildren().clear();
                    var retrievedFolder = folderClient.getFolder(folderEntry.getFolder().id());
                    if (retrievedFolder.isEmpty()) {
                        showErrorDialog("That folder does not exist. Did you delete it on a different device?", "Folder not found", () -> setFolderView(folder));
                    } else {
                        setFolderView(retrievedFolder.get());
                    }
                }
            });
            this.itemPane.getChildren().add(folderEntry);
        }
        loadFolderFiles(folder);
    }

    private void loadFolderFiles(FolderApi folder) {
        for (FileApi fileApi : folder.files()) {
            FileEntry entry = new FileEntry(fileApi);
            entry.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    runInThread(() -> {
                        try {
                            var file = saveAndGetFile(fileApi);
                            desktop.open(file);
                        } catch (BadFileRequestException | BadFileResponseException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            });
            itemPane.getChildren().add(entry);
        }
    }

    private File saveAndGetFile(FileApi fileApi) throws BadFileRequestException, BadFileResponseException, IOException {
        // ensure the cache directory exists
        new File(CACHE_DIR).mkdir();
        var cacheFile = new File(CACHE_DIR + "/" + fileApi.id() + "_" + fileApi.name());
        if (!cacheFile.exists()) {
            var inStream = fileClient.getFileContents(fileApi.id());
            cacheFile.createNewFile();
            Files.copy(inStream, cacheFile.toPath(), REPLACE_EXISTING);
        }
        return cacheFile;
    }

}
