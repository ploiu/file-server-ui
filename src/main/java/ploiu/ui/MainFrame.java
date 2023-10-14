package ploiu.ui;

import io.reactivex.rxjava3.core.Single;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;
import ploiu.client.FolderClient;
import ploiu.event.*;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.*;
import ploiu.service.FileService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static ploiu.event.FolderEvent.Type.*;
import static ploiu.util.DialogUtils.showErrorDialog;

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
    @SuppressWarnings("FieldCanBeLocal")
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
                .observeOn(JavaFxScheduler.platform())
                .doOnSuccess(files -> {
                    this.folderPane.getChildren().clear();
                    this.filePane.getChildren().clear();
                    for (FileApi file : files) {
                        this.filePane.getChildren().add(createFileEntry(file));
                    }
                })
                .subscribe();
        return Single.just(true);
    };

    // nav bar
    @SuppressWarnings("FieldCanBeLocal")
    private final AsyncEventReceiver<FolderApi> navigateFolderEvents = event -> {
        asyncLoadFolder(event.get());
        return Single.just(true);
    };

    private final AsyncEventReceiver<FolderApi> asyncUpdateFolderEvent = event -> {
        if (event instanceof FolderEvent fe && fe.getType() == UPDATE) {
            var folder = fe.get();
            return folderClient
                    .updateFolder(new FolderRequest(Optional.of(folder.id()), folder.parentId(), folder.path()))
                    .doOnSuccess(ignored -> asyncLoadFolder(currentFolder))
                    .map(ignored -> true);
        } else {
            return Single.error(new UnsupportedOperationException("Only type UPDATE is supported for updateFolderEvent"));
        }
    };

    private final AsyncEventReceiver<FolderApi> asyncCreateFolderEvent = event -> {
        if (event instanceof FolderEvent fe && fe.getType() == CREATE) {
            var req = new FolderRequest(Optional.empty(), currentFolder.id(), fe.get().path());
            return folderClient
                    .createFolder(req)
                    .observeOn(JavaFxScheduler.platform())
                    .doFinally(() -> asyncLoadFolder(currentFolder))
                    .doOnError(e -> showErrorDialog(e.getMessage(), "Failed to create folder", null))
                    .map(ignored -> true);
        }

        return Single.error(new UnsupportedOperationException("asyncCreateFolderEvent requires FolderEvent of type CREATE"));
    };

    private final AsyncEventReceiver<FolderApi> asyncDeleteFolderEvent = event -> {
        if (event instanceof FolderEvent fe && fe.getType() == DELETE) {
            return folderClient
                    .deleteFolder(fe.get().id())
                    .observeOn(JavaFxScheduler.platform())
                    .doOnError(e -> showErrorDialog(e.getMessage(), "Failed to delete folder", null))
                    .doOnComplete(() -> asyncLoadFolder(currentFolder))
                    .toSingle(() -> true);
        }
        return Single.error(new UnsupportedOperationException("asyncDeleteFolderEvent requires FolderEvent of type DELETE"));
    };

    private final AsyncEventReceiver<FolderApi> asyncFolderCrudEvents = event -> {
        if (event instanceof FolderEvent fe) {
            return switch (fe.getType()) {
                case UPDATE -> asyncUpdateFolderEvent.process(fe);
                case CREATE -> asyncCreateFolderEvent.process(fe);
                case DELETE -> asyncDeleteFolderEvent.process(fe);
            };
        }
        return Single.error(new UnsupportedOperationException("FolderEvent required for asyncFolderCrudEvents"));
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
                            //Platform.runLater(() -> loadFolder(currentFolder));
                            asyncLoadFolder(currentFolder);
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
                        //Platform.runLater(() -> loadFolder(currentFolder));
                        asyncLoadFolder(currentFolder);
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
                    .doOnSuccess(ignored -> asyncLoadFolder(currentFolder))
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
        loader.getNamespace().put("searchEvents", asyncSearchEvents);
        try {
            loader.load();
            loadInitialFolder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadInitialFolder() {
        var defaultFolder = new FolderApi(0, -1, "root", List.of(), List.of());
        navigationBar.push(defaultFolder);
        asyncLoadFolder(defaultFolder);
    }

    private void asyncLoadFolder(FolderApi folder) {
        var folderReq = folderClient
                .getFolder(folder.id())
                .doOnSuccess(this::setCurrentFolder)
                .doOnError(e -> showErrorDialog(e.getMessage(), "Failed to pull folder", null))
                .observeOn(JavaFxScheduler.platform())
                .toObservable()
                .cache();

        // handle child folders
        folderReq
                .doOnNext(ignored -> folderPane.getChildren().clear())
                .flatMapIterable(FolderApi::folders)
                .map(this::createFolderEntry)
                .doOnNext(this.folderPane.getChildren()::add)
                .toList()
                .subscribeOn(JavaFxScheduler.platform())
                .subscribe(ignored -> drawAddFolder());

        // handle child files
        folderReq
                .doOnNext(ignored -> filePane.getChildren().clear())
                .flatMapIterable(FolderApi::files)
                .map(this::createFileEntry)
                .doOnNext(filePane.getChildren()::add)
                .toList()
                .subscribeOn(JavaFxScheduler.platform())
                .subscribe(ignored -> drawAddFile());

    }

    private FileEntry createFileEntry(FileApi file) {
        var fileEntry = new FileEntry(file, asyncFileCrudEvents);
        fileEntry.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                var modal = new LoadingModal(new LoadingModalOptions(getScene().getWindow(), LoadingModalOptions.LoadingType.INDETERMINATE));
                modal.open();
                fileService.saveAndGetFile(fileEntry.getFile(), null)
                        .doFinally(modal::close)
                        .subscribe(desktop::open, e -> showErrorDialog("Failed to open file: " + e.getMessage(), "Failed to open file", null));
            }
        });
        return fileEntry;
    }

    private FolderEntry createFolderEntry(FolderApi folder) {
        var folderEntry = new FolderEntry(folder, asyncFolderCrudEvents);
        // when clicking any of the folder entries, clear the page and populate it with the new folder contents
        folderEntry.setOnMouseClicked(mouseEvent -> {
            // left click is used for entry, right click is used for modifying properties
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                folderClient
                        .getFolder(folderEntry.getFolder().id())
                        .doOnError(e -> showErrorDialog("That folder does not exist. Did you delete it on a different device?", "Folder not found", () -> asyncLoadFolder(currentFolder)))
                        .subscribeOn(JavaFxScheduler.platform())
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe(it -> {
                            folderPane.getChildren().clear();
                            navigationBar.push(it);
                            asyncLoadFolder(it);
                        });
            }
        });
        return folderEntry;
    }

    private void drawAddFolder() {
        var addFolder = new AddFolder(asyncFolderCrudEvents, currentFolder.id());
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

    private void setCurrentFolder(FolderApi folder) {
        this.currentFolder = folder;
    }
}
