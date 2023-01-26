package ploiu.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import ploiu.client.FileClient;
import ploiu.client.FolderClient;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.FileApi;
import ploiu.model.FolderApi;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ploiu.Constants.CACHE_DIR;
import static ploiu.util.DialogUtils.showErrorDialog;
import static ploiu.util.ThreadUtils.runInThread;

@SuppressWarnings("unused")
public class MainFrame extends AnchorPane {
    private final FolderClient folderClient = App.INJECTOR.getInstance(FolderClient.class);
    private final FileClient fileClient = App.INJECTOR.getInstance(FileClient.class);
    private final Desktop desktop = Desktop.getDesktop();
    // keep track of where the user has navigated
    private final List<FolderApi> folderNavigation = new ArrayList<>();
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private FlowPane itemPane;
    @FXML
    private HBox navigationBar;
    // atomic because we need to change this in a lambda click event
    private final AtomicBoolean isSearching = new AtomicBoolean(false);

    public MainFrame() {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/MainFrame.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            loadInitialFolder();
            drawNavBar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadInitialFolder() {
        var folder = folderClient.getFolder(null).get();
        folderNavigation.add(folder);
        // null folder is the root folder, so this will always exist
        setFolderView(folder);
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
                    folderNavigation.add(folderEntry.getFolder());
                    drawNavBar();
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
        loadFiles(folder.files());
    }

    private void loadFiles(Collection<FileApi> files) {
        for (FileApi fileApi : files) {
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

    @FXML
    private void searchButtonClicked(ActionEvent event) {
        handleSearch(searchButton.getText());
    }

    @FXML
    private void searchBarKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleSearch(searchField.getText());
        }
    }

    private void handleSearch(String text) {
        isSearching.set(true);
        try {
            var files = fileClient.search(text);
            this.itemPane.getChildren().clear();
            loadFiles(files);
        } catch (BadFileRequestException e) {
            showErrorDialog(e.getMessage(), "Bad Search Text", null);
        } catch (BadFileResponseException e) {
            showErrorDialog(e.getMessage(), "Server Error", null);

        }
    }

    private void drawNavBar() {
        // inefficient but can be optimized later, since this won't be invoked too often
        navigationBar.getChildren().clear();

        for (int i = 0; i < folderNavigation.size(); i++) {
            final int j = i;
            var folder = folderNavigation.get(i);
            var folderLink = new Label(folder.path());
            folderLink.getStyleClass().add("folder-link");
            navigationBar.getChildren().add(folderLink);
            var label = new Label("/");
            label.getStyleClass().add("text");
            navigationBar.setAlignment(Pos.BOTTOM_LEFT);
            navigationBar.getChildren().add(label);
            folderLink.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && (j < folderNavigation.size() - 1 || isSearching.get())) {
                    isSearching.set(false);
                    folderNavigation.subList(j + 1, folderNavigation.size()).clear();
                    drawNavBar();
                    setFolderView(folder);
                }
            });
        }
    }
}
