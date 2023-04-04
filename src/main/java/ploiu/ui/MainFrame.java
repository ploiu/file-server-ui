package ploiu.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import ploiu.client.FileClient;
import ploiu.client.FolderClient;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.exception.BadFolderRequestException;
import ploiu.exception.BadFolderResponseException;
import ploiu.model.FileApi;
import ploiu.model.FolderApi;
import ploiu.model.FolderRequest;
import ploiu.ui.event.EventReceiver;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Optional;

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
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private FlowPane folderPane;
    @FXML
    private FlowPane filePane;
    @FXML
    private NavBar navigationBar;
    // so we know where to add files / folders
    private FolderApi currentFolder;

    public MainFrame() {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/MainFrame.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        // for the nav bar
        EventReceiver<FolderApi> folderEvents = event -> {
            loadFolder(event.get());
            return true;
        };
        loader.getNamespace().put("folderEvents", folderEvents);
        try {
            loader.load();
            loadInitialFolder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadInitialFolder() {
        var folder = folderClient.getFolder(null).orElseThrow();
        navigationBar.push(folder);
        // null folder is the root folder, so this will always exist
        loadFolder(folder);
    }

    private void loadFolder(FolderApi folder) {
        folderPane.getChildren().clear();
        filePane.getChildren().clear();
        // need to get fresh copy of the folder, as the object may be stale if other folders were added to it
        currentFolder = folderClient.getFolder(folder.id() == 0 ? null : folder.id()).orElseThrow();
        var folderEntries = currentFolder.folders()
                .stream()
                .map(FolderEntry::new)
                .toList();
        for (FolderEntry folderEntry : folderEntries) {
            // when clicking any of the folder entries, clear the page and populate it with the new folder contents
            folderEntry.setOnMouseClicked(mouseEvent -> {
                // left click is used for entry, right click is used for modifying properties
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    folderPane.getChildren().clear();
                    var retrievedFolder = folderClient.getFolder(folderEntry.getFolder().id());
                    if (retrievedFolder.isEmpty()) {
                        showErrorDialog("That folder does not exist. Did you delete it on a different device?", "Folder not found", () -> loadFolder(currentFolder));
                    } else {
                        navigationBar.push(retrievedFolder.get());
                        loadFolder(retrievedFolder.get());
                    }
                }
            });
            this.folderPane.getChildren().add(folderEntry);
        }
        drawAddFolder();
        loadFiles(currentFolder.files());
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
            filePane.getChildren().add(entry);
        }
    }

    private File saveAndGetFile(FileApi fileApi) throws BadFileRequestException, BadFileResponseException, IOException {
        // ensure the cache directory exists
        //noinspection ResultOfMethodCallIgnored
        new File(CACHE_DIR).mkdir();
        var cacheFile = new File(CACHE_DIR + "/" + fileApi.id() + "_" + fileApi.name());
        if (!cacheFile.exists()) {
            var inStream = fileClient.getFileContents(fileApi.id());
            //noinspection ResultOfMethodCallIgnored
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
        try {
            var files = fileClient.search(text);
            this.folderPane.getChildren().clear();
            this.filePane.getChildren().clear();
            loadFiles(files);
        } catch (BadFileRequestException e) {
            showErrorDialog(e.getMessage(), "Bad Search Text", null);
        } catch (BadFileResponseException e) {
            showErrorDialog(e.getMessage(), "Server Error", null);

        }
    }

    private void drawAddFolder() {
        var addFolder = new AddFolder(e -> showNewFolderPane());
        this.folderPane.getChildren().add(addFolder);
    }

    private void showNewFolderPane() {
        AddFolderDialog.CreateAction callback = folderName -> {
            // translate for the api
            var folderId = currentFolder.id() == 0 ? null : currentFolder.id();
            try {
                folderClient.createFolder(new FolderRequest(Optional.empty(), Optional.ofNullable(folderId), folderName));
            } catch (BadFolderRequestException | BadFolderResponseException e) {
                showErrorDialog("Failed to create folder. Message is " + e.getMessage(), "Failed to create folder", null);
            }
            folderClient.getFolder(folderId).ifPresent(this::loadFolder);
        };
        var addFolderDialog = new AddFolderDialog(this.getScene().getWindow(), callback);
    }
}
