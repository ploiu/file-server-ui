package ploiu.ui;

import io.reactivex.rxjava3.core.Single;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import ploiu.client.FolderClient;
import ploiu.event.*;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.exception.BadFolderRequestException;
import ploiu.exception.BadFolderResponseException;
import ploiu.model.*;
import ploiu.service.FileService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static ploiu.util.DialogUtils.showErrorDialog;
import static ploiu.util.ThreadUtils.runInThread;

@SuppressWarnings("unused")
public class MainFrame extends AnchorPane {
    private final FolderClient folderClient = App.INJECTOR.getInstance(FolderClient.class);
    private final FileService fileService = App.INJECTOR.getInstance(FileService.class);
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
        throw new UnsupportedOperationException("blocking search no longer supported");
    };

    private final AsyncEventReceiver<String> asyncSearchEvents = event -> {
        fileService
                .search(event.get())
                .doOnError(e -> {
                    if (e instanceof BadFileRequestException) {
                        showErrorDialog(e.getMessage(), "Bad Search Text", null);
                    } else if (e instanceof BadFileResponseException) {
                        showErrorDialog(e.getMessage(), "Server Error", null);
                    }
                })
                .subscribe();
        return Single.just(true);
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

    private final AsyncEventReceiver<File> asyncFileUploadEvent = event -> {
        var file = event.get();
        if (!file.exists()) {
            showErrorDialog("Failed to upload file [" + file.getName() + "] because it doesn't exist. Not sure how you did that but quit!", "Failed to upload file", null);
            return Single.just(false);
        }
        if (event instanceof FileUploadEvent uploadEvent) {
            return fileService.createFile(new CreateFileRequest(uploadEvent.getFolderId(), file))
                    .doAfterSuccess(result -> {
                        if (uploadEvent.getFolderId() == currentFolder.id()) {
                            Platform.runLater(() -> loadFolder(currentFolder));
                        }
                    })
                    .map(it -> it.id() > -1);
        } else {
            return Single.error(new UnsupportedOperationException("asyncFileUploadEvent only supports FileUploadEvent"));
        }
    };

    private final AsyncEventReceiver<FileApi> asyncFileDeleteEvent = event -> {
        if (event instanceof FileDeleteEvent) {
            return fileService.deleteFile(event.get().id())
                    .doOnError(e -> showErrorDialog("Failed to delete file [" + event.get().name() + ". Error details: " + e.getMessage(), "Failed to delete file", null))
                    .andThen(Single.fromCallable(() -> {
                        Platform.runLater(() -> loadFolder(currentFolder));
                        return true;
                    }));
        } else {
            return Single.error(new UnsupportedOperationException("asyncFileDeleteEvent only supports FileDeleteEvent"));
        }
    };

    private final AsyncEventReceiver<FileApi> asyncFileUpdateEvent = event -> {
        if (event instanceof FileUpdateEvent updateEvent) {
            var file = updateEvent.get();
            var req = new UpdateFileRequest(file.id(), currentFolder.id(), file.name());
            return fileService.updateFile(req)
                    .doOnSuccess(ignored -> Platform.runLater(() -> loadFolder(currentFolder)))
                    .doOnError(e -> showErrorDialog("Failed to update file. Message is " + e.getMessage(), "Failed to update file", null))
                    .map(ignored -> true);
        } else {
            return Single.error(new UnsupportedOperationException("asyncFileUpdateEvent only supports FileUpdateEvent"));
        }
    };

    private final AsyncEventReceiver<FileApi> asyncFileSaveEvent = event -> {
        if (event instanceof FileSaveEvent saveEvent) {
            var dir = saveEvent.getDirectory();
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }
            var fileExists = Arrays.stream(dir.listFiles()).filter(File::isFile).map(File::getName).anyMatch(saveEvent.get().name()::equalsIgnoreCase);
            var loadingModal = new LoadingModal(new LoadingModalOptions(getScene().getWindow(), LoadingModalOptions.LoadingType.INDETERMINATE));
            var saveAction = fileService
                    .saveAndGetFile(saveEvent.get(), saveEvent.getDirectory())
                    .doOnError(e -> showErrorDialog("Failed to save file: " + e.getMessage(), "Failed to save file", null))
                    .doFinally(loadingModal::close);
            if (fileExists) {
                var modal = new ConfirmDialog(new ConfirmDialogOptions(getScene().getWindow(), res -> {
                    if (res.get()) {
                        loadingModal.open();
                        saveAction.subscribe();
                    }
                    return true;
                }, "That file already exists. Do you wish to overwrite?"));
            } else {
                loadingModal.open();
                saveAction.subscribe();
            }
        } else {
            return Single.error(new UnsupportedOperationException("asyncFileSaveEvent only supports FileSaveEvent"));
        }
        return Single.just(true);
    };

    private final AsyncEventReceiver<FileApi> asyncFileCrudEvents = event -> {
        if (event instanceof FileDeleteEvent) {
            return asyncFileDeleteEvent.process(event);
        } else if (event instanceof FileUpdateEvent) {
            return asyncFileUpdateEvent.process(event);
        } else if (event instanceof FileSaveEvent) {
            return asyncFileSaveEvent.process(event);
        }

        return Single.just(false);
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
            FileEntry entry = new FileEntry(fileApi, asyncFileCrudEvents);
            entry.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    runInThread(() -> {
                        try {
                            var modal = new LoadingModal(new LoadingModalOptions(getScene().getWindow(), LoadingModalOptions.LoadingType.INDETERMINATE));
                            modal.open();
                            fileService.saveAndGetFile(fileApi, null)
                                    .doFinally(modal::close)
                                    .subscribe(desktop::open, e -> showErrorDialog("Failed to open file: " + e.getMessage(), "Failed to open file", null));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            });
            filePane.getChildren().add(entry);
        }
        drawAddFile();
    }

    private void drawAddFolder() {
        var addFolder = new AddFolder(folderCrudEvents, currentFolder.id());
        this.folderPane.getChildren().add(addFolder);
    }

    private void drawAddFile() {
        var addFile = new AddFile(asyncFileUploadEvent, currentFolder.id());
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
