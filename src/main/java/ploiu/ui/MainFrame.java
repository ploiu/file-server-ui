package ploiu.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import ploiu.client.FileClient;
import ploiu.client.FolderClient;
import ploiu.event.EventReceiver;
import ploiu.event.FileDeleteEvent;
import ploiu.event.FileUploadEvent;
import ploiu.event.FolderEvent;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.exception.BadFolderRequestException;
import ploiu.exception.BadFolderResponseException;
import ploiu.model.CreateFileRequest;
import ploiu.model.FileApi;
import ploiu.model.FolderApi;
import ploiu.model.FolderRequest;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
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
    @FXML
    private FlowPane folderPane;
    @FXML
    private FlowPane filePane;
    @FXML
    private NavBar navigationBar;
    @FXML
    private SearchBar searchBar;
    // so we know where to add files / folders
    private FolderApi currentFolder;

    /// EVENT HANDLERS
    // search bar
    private final EventReceiver<String> searchEvents = event -> {
        handleSearch(event.get());
        return true;
    };
    // nav bar
    private final EventReceiver<FolderApi> navigateFolderEvents = event -> {
        loadFolder(event.get());
        return true;
    };
    private final EventReceiver<FolderApi> folderCrudEvents = event -> {
        var folder = event.get();
        if (event instanceof FolderEvent fe) {
            return switch (fe.getType()) {
                case UPDATE -> {
                    try {
                        // I really need to make the api return 0 instead of null for the root folder...
                        var updated = folderClient.updateFolder(new FolderRequest(Optional.of(folder.id()), folder.parentId(), folder.path()));
                        // need to redraw the current folder
                        loadFolder(currentFolder);
                        yield true;
                    } catch (BadFolderRequestException | BadFolderResponseException e) {
                        yield false;
                    }
                }
                case DELETE -> {
                    try {
                        if (folderClient.deleteFolder(folder.id())) {
                            loadFolder(currentFolder);
                            yield true;
                        }
                    } catch (BadFolderRequestException | BadFolderResponseException e) {
                        showErrorDialog(e.getMessage(), "Failed to delete folder", null);
                    }
                    yield false;
                }
                case CREATE -> {
                    var id = this.currentFolder.id();
                    folderClient.getFolder(id).ifPresent(this::loadFolder);
                    yield true;
                }
            };
        }
        return false;
    };

    // for files that don't exist yet (no file api object)
    private final EventReceiver<File> fileUploadEvent = event -> {
        var file = event.get();
        if (!file.exists()) {
            return false;
        }
        if (event instanceof FileUploadEvent uploadEvent) {
            try {
                fileClient.createFile(new CreateFileRequest(uploadEvent.getFolderId(), file));
                if (uploadEvent.getFolderId() == currentFolder.id()) {
                    loadFolder(currentFolder);
                }
                return true;
            } catch (BadFileRequestException e) {
                showErrorDialog("Failed to upload file [" + file.getName() + "] Please check server logs for details", "Failed to upload file", null);
                return false;
            }
        }
        return false;
    };

    private final EventReceiver<FileApi> fileCrudEvents = event -> {
        if (event instanceof FileDeleteEvent deleteEvent) {
            try {
                fileClient.deleteFile(event.get().id());
                loadFolder(currentFolder);
                return true;
            } catch (BadFileRequestException e) {
                showErrorDialog("Failed to delete file [" + event.get().name() + "] Please check server logs for details", "Failed to delete file", null);
                return false;
            }
        }
        return false;
    };

    public MainFrame() {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/MainFrame.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        // add event handlers to the namespace
        loader.getNamespace().put("folderEvents", navigateFolderEvents);
        loader.getNamespace().put("searchEvents", searchEvents);
        try {
            loader.load();
            loadInitialFolder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadInitialFolder() {
        var folder = folderClient.getFolder(0).orElseThrow();
        navigationBar.push(folder);
        // null folder is the root folder, so this will always exist
        loadFolder(folder);
    }

    private void loadFolder(FolderApi folder) {
        folderPane.getChildren().clear();
        filePane.getChildren().clear();
        // need to get fresh copy of the folder, as the object may be stale if other folders were added to it
        currentFolder = folderClient.getFolder(folder.id()).orElseThrow();
        var folderEntries = currentFolder.folders()
                .stream()
                .map(folderApi -> new FolderEntry(folderApi, folderCrudEvents))
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
            FileEntry entry = new FileEntry(fileApi, fileCrudEvents);
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
        drawAddFile();
    }

    private File saveAndGetFile(FileApi fileApi) throws BadFileRequestException, BadFileResponseException, IOException {
        // ensure the cache directory exists
        //noinspection ResultOfMethodCallIgnored
        new File(CACHE_DIR).mkdir();
        var cacheFile = new File(CACHE_DIR + "/" + fileApi.id() + "_" + fileApi.name());
        if (!cacheFile.exists()) {
            var contents = fileClient.getFileContents(fileApi.id());
            //noinspection ResultOfMethodCallIgnored
            cacheFile.createNewFile();
            Files.copy(new ByteArrayInputStream(contents.getBytes()), cacheFile.toPath(), REPLACE_EXISTING);
        }
        return cacheFile;
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
        var addFolder = new AddFolder(folderCrudEvents, currentFolder.id());
        this.folderPane.getChildren().add(addFolder);
    }

    private void drawAddFile() {
        var addFile = new AddFile(fileUploadEvent, currentFolder.id());
        this.filePane.getChildren().add(addFile);
    }

    //@FXML
    // TODO https://bugs.openjdk.org/browse/JDK-8275033 update to openjfx 21 when it's released
    private void onDragOver(DragEvent e) {
        var board = e.getDragboard();
        if (board.hasFiles()) {
            System.out.println(e.getTarget());
            e.acceptTransferModes(TransferMode.COPY);
            System.out.println(e.getEventType());
        }
        e.consume();
    }

    //@FXML
    // TODO https://bugs.openjdk.org/browse/JDK-8275033 update to openjfx 21 when it's released
    private void onDragDropped(DragEvent event) {
        var board = event.getDragboard();
        if (board.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
            for (File file : board.getFiles()) {

            }
        }
        event.consume();
    }
}
