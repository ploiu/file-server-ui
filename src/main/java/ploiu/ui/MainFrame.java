package ploiu.ui;

import io.reactivex.rxjava3.core.Single;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;
import ploiu.client.FolderClient;
import ploiu.event.*;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.*;
import ploiu.service.DragNDropService;
import ploiu.service.FileService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static ploiu.event.FolderEvent.Type.*;
import static ploiu.util.DialogUtils.showErrorDialog;
import static ploiu.util.UIUtils.desktop;

public class MainFrame extends AnchorPane {
    private final FolderClient folderClient = App.INJECTOR.getInstance(FolderClient.class);
    private final FileService fileService = App.INJECTOR.getInstance(FileService.class);
    private final DragNDropService dragNDropService = App.INJECTOR.getInstance(DragNDropService.class);
    @FXML
    private ScrollPane scrollPane;
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
    private final ObjectProperty<FolderApi> editingFolder = new SimpleObjectProperty<>(null);
    private final ObjectProperty<FileApi> editingFile = new SimpleObjectProperty<>(null);
    // contains all the current image previews
    private final Map<Long, ObjectProperty<Image>> filePreviews = new HashMap<>();
    private FolderInfo folderInfo;
    private FileInfo fileInfo;

    /// EVENT HANDLERS
    // search bar
    @SuppressWarnings("FieldCanBeLocal")
    private final AsyncEventReceiver<String> asyncSearchEvents = event -> {
        fileService
                .search(event.get())
                .observeOn(JavaFxScheduler.platform())
                .doOnError(e -> {
                    if (e instanceof BadFileRequestException) {
                        showErrorDialog(e.getMessage(), "Bad Search Text", null);
                    } else if (e instanceof BadFileResponseException) {
                        showErrorDialog(e.getMessage(), "Server Error", null);
                    }
                })
                .doOnSuccess(files -> {
                    this.folderPane.getChildren().clear();
                    this.filePane.getChildren().clear();
                    // TODO pull previews
                    var fileEntries = files.stream().map(this::createFileEntry).toList();
                    this.filePane.getChildren().addAll(fileEntries);
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

    private final AsyncEventReceiver<FolderApi> asyncFolderUpdateEvent = event -> {
        if (event instanceof FolderEvent fe && fe.getType() == UPDATE) {
            var folder = fe.get();
            return folderClient
                    .updateFolder(new FolderRequest(Optional.of(folder.id()), folder.parentId(), folder.name(), folder.tags()))
                    .doOnSuccess(ignored -> asyncLoadFolder(currentFolder))
                    .map(ignored -> true);
        } else {
            return Single.error(new UnsupportedOperationException("Only type UPDATE is supported for updateFolderEvent"));
        }
    };

    private final AsyncEventReceiver<FolderApi> asyncFolderCreateEvent = event -> {
        if (event instanceof FolderEvent fe && fe.getType() == CREATE) {
            var req = new FolderRequest(Optional.empty(), currentFolder.id(), fe.get().name(), fe.get().tags());
            return folderClient
                    .createFolder(req)
                    .observeOn(JavaFxScheduler.platform())
                    .doFinally(() -> asyncLoadFolder(currentFolder))
                    .doOnError(e -> showErrorDialog(e.getMessage(), "Failed to create folder", null))
                    .map(ignored -> true);
        }

        return Single.error(new UnsupportedOperationException("asyncCreateFolderEvent requires FolderEvent of type CREATE"));
    };

    private final AsyncEventReceiver<FolderApi> asyncFolderDeleteEvent = event -> {
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
                case UPDATE -> asyncFolderUpdateEvent.process(fe);
                case CREATE -> asyncFolderCreateEvent.process(fe);
                case DELETE -> asyncFolderDeleteEvent.process(fe);
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
                            asyncLoadFolder(currentFolder);
                        }
                    })
                    .doOnError(e -> showErrorDialog("Failed to upload file. Please check server logs for details", "Failed to upload file", null))
                    .map(it -> it.id() > -1);
        } else {
            return Single.error(new UnsupportedOperationException("asyncFileUploadEvent only supports FileUploadEvent"));
        }
    };

    private final AsyncEventReceiver<FileObject> asyncFileDeleteEvent = event -> {
        if (event instanceof FileDeleteEvent) {
            return fileService.deleteFile(event.get().id())
                    .doOnError(e -> showErrorDialog("Failed to delete file [" + event.get().name() + ". Error details: " + e.getMessage(), "Failed to delete file", null))
                    .andThen(Single.fromCallable(() -> {
                        asyncLoadFolder(currentFolder);
                        return true;
                    }));
        } else {
            return Single.error(new UnsupportedOperationException("asyncFileDeleteEvent only supports FileDeleteEvent"));
        }
    };

    private final AsyncEventReceiver<FileObject> asyncFileUpdateEvent = event -> {
        if (event instanceof FileUpdateEvent updateEvent) {
            var file = updateEvent.get();
            UpdateFileRequest req;
            if (updateEvent.get() instanceof FileApi fileApi) {
                req = new UpdateFileRequest(fileApi.id(), fileApi.folderId() == null ? currentFolder.id() : fileApi.folderId(), fileApi.name(), fileApi.tags());
            } else {
                throw new UnsupportedOperationException("Unknown subclass of FileObject");
            }
            return fileService.updateFile(req)
                    .doOnSuccess(ignored -> asyncLoadFolder(currentFolder))
                    .doOnError(e -> showErrorDialog("Failed to update file. Message is " + e.getMessage(), "Failed to update file", null))
                    .map(ignored -> true);
        } else {
            return Single.error(new UnsupportedOperationException("asyncFileUpdateEvent only supports FileUpdateEvent"));
        }
    };

    private final AsyncEventReceiver<FileObject> asyncFileSaveEvent = event -> {
        if (event instanceof FileSaveEvent saveEvent && saveEvent.get() instanceof FileApi file) {
            var dir = saveEvent.getDirectory();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            var fileExists = Arrays.stream(dir.listFiles()).filter(File::isFile).map(File::getName).anyMatch(file.name()::equalsIgnoreCase);
            var loadingModal = new LoadingModal(new LoadingModalOptions(getScene().getWindow(), LoadingModalOptions.LoadingType.INDETERMINATE));
            var saveAction = fileService
                    .saveAndGetFile(file, saveEvent.getDirectory())
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

    private final AsyncEventReceiver<FileObject> asyncFileCrudEvents = event -> {
        return switch (event) {
            case FileDeleteEvent fileDeleteEvent -> asyncFileDeleteEvent.process(fileDeleteEvent);
            case FileUpdateEvent fileUpdateEvent -> asyncFileUpdateEvent.process(fileUpdateEvent);
            case FileSaveEvent fileSaveEvent -> asyncFileSaveEvent.process(fileSaveEvent);
            default -> Single.just(false);
        };
    };

    public MainFrame() {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/MainFrame.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        // add event handlers to the namespace
        loader.getNamespace().put("folderNavigationEvents", navigateFolderEvents);
        loader.getNamespace().put("folderCrudEvents", asyncFolderCrudEvents);
        loader.getNamespace().put("fileCrudEvents", asyncFileCrudEvents);
        loader.getNamespace().put("searchEvents", asyncSearchEvents);
        try {
            loader.load();
            loadInitialFolder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadInitialFolder() {
        this.folderPane.setPrefWidth(this.widthProperty().doubleValue());
        var defaultFolder = new FolderApi(0, -1, "root", null, List.of(), List.of(), List.of());
        navigationBar.push(defaultFolder);
        asyncLoadFolder(defaultFolder);
    }

    private void asyncLoadFolder(FolderApi folder) {
        // pull the folder
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
                .toList()
                .doOnSuccess(this.folderPane.getChildren()::addAll)
                .subscribe(ignored -> drawAddFolder());

        // handle child files
        folderReq
                .doOnNext(f -> {
                    loadFilePreviews(f);
                    filePane.getChildren().clear();
                })
                .flatMapIterable(FolderApi::files)
                .map(this::createFileEntry)
                .toList()
                .doOnSuccess(filePane.getChildren()::addAll)
                .subscribe(ignored -> drawAddFile());
    }

    private void loadFilePreviews(FolderApi folder) {
        synchronized (filePreviews) {
            filePreviews.clear();
        }
        if (folder.files().isEmpty()) {
            return;
        }
        synchronized (filePreviews) {
            for (var file : folder.files()) {
                filePreviews.put(file.id(), new SimpleObjectProperty<>(null));
            }
        }
        //noinspection ResultOfMethodCallIgnored
        fileService
                .getFilePreviewsForFolder(folder)
                .subscribe(previewMap -> {
                    synchronized (filePreviews) {
                        previewMap.forEach((id, image) -> filePreviews.get(id).setValue(image));
                    }
                });
    }

    private FileEntry createFileEntry(FileApi file) {
        ObjectProperty<Image> filePreview;
        synchronized (filePreviews) {
            filePreview = filePreviews.get(file.id());
        }
        var fileEntry = new FileEntry(file, asyncFileCrudEvents, editingFile, filePreview);
        var timesClicked = new AtomicInteger(0);
        var waitMillis = 250L;
        fileEntry.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (timesClicked.incrementAndGet() == 1) {
                    // start the timer
                    new Thread(() -> {
                        try {
                            Thread.sleep(waitMillis);
                        } catch (InterruptedException ignored) {
                        }
                        var clickCount = timesClicked.get();
                        // regardless of click count, reset the count
                        timesClicked.set(0);
                        if (clickCount == 2) {
                            // open the file
                            var modal = new LoadingModal(new LoadingModalOptions(getScene().getWindow(), LoadingModalOptions.LoadingType.INDETERMINATE));
                            modal.open();
                            fileService.saveAndGetFile(file, null)
                                    .doFinally(modal::close)
                                    .subscribe(desktop::open, e -> showErrorDialog("Failed to open file: " + e.getMessage(), "Failed to open file", null));
                        } else {
                            editingFile.set(file);
                        }
                    }).start();
                }
            }
        });
        return fileEntry;
    }

    private FolderEntry createFolderEntry(FolderApi folder) {
        var folderEntry = new FolderEntry(folder, asyncFolderCrudEvents, asyncFileCrudEvents, editingFolder);
        // when clicking any of the folder entries, clear the page and populate it with the new folder contents
        folderEntry.setOnMouseClicked(mouseEvent -> {
            // left click is used for entry, right click is used for modifying properties
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                Single.just(folder)
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

    @FXML
    private void onDragOver(DragEvent e) {
        e.acceptTransferModes(TransferMode.ANY);
        e.consume();
    }

    @FXML
    private void onDragDropped(DragEvent event) {
        var board = event.getDragboard();
        if (board.hasFiles() && !event.isConsumed()) {
            event.consume();
            event.acceptTransferModes(TransferMode.COPY);
            dragNDropService.dropFiles(board.getFiles(), currentFolder, getScene().getWindow())
                    .doOnComplete(() -> asyncLoadFolder(currentFolder))
                    .subscribe(() -> {
                    }, e -> showErrorDialog(e.getMessage(), "Failed to upload files", null));
        }
    }

    /**
     * used to update {@code currentFolder} in lambda expressions / method references
     *
     * @param folder
     */
    private void setCurrentFolder(FolderApi folder) {
        this.currentFolder = folder;
    }

    @FXML
    private void initialize() {
        widthProperty().addListener((obs, oldVal, newVal) -> folderPane.setPrefWidth(newVal.doubleValue()));
        heightProperty().addListener((obs, oldVal, newVal) -> scrollPane.setPrefHeight(newVal.doubleValue() - 50));
        editingFolder.addListener((obs, oldFolder, f) -> {
            if (f == null && folderInfo != null) {
                this.getChildren().remove(folderInfo);
                folderInfo = null;
            } else if (f != null) {
                // we don't contain detailed info about the folder unless we directly pull it
                folderClient.getFolder(f.id())
                        .observeOn(JavaFxScheduler.platform())
                        .doOnSuccess(retrieved -> {
                            this.folderInfo = new FolderInfo(retrieved, asyncFolderCrudEvents);
                            this.getChildren().add(folderInfo);
                            folderInfo.toFront();
                        })
                        .subscribe();

            }
        });
        editingFile.addListener((obs, oldFile, f) -> {
            if (f == null && fileInfo != null) {
                this.getChildren().remove(fileInfo);
                fileInfo = null;
            } else if (f != null) {
                // make sure we have updated file information
                fileService
                        .getMetadata(f.id())
                        .observeOn(JavaFxScheduler.platform())
                        .doOnSuccess(retrieved -> {
                            this.fileInfo = new FileInfo(retrieved, asyncFileCrudEvents);
                            this.getChildren().add(fileInfo);
                            fileInfo.toFront();
                        })
                        .subscribe();
            }
        });
    }

    @FXML
    void keyPressed(KeyEvent e) {
        if (e.isConsumed()) {
            return;
        }
        // hide folder + file info
        if (e.getCode() == KeyCode.ESCAPE && (editingFolder.get() != null || editingFile != null)) {
            e.consume();
            editingFolder.unbind();
            editingFolder.setValue(null);
            editingFile.unbind();
            editingFile.setValue(null);
        }
        // focus search bar
        else if (e.getCode() == KeyCode.SLASH && !e.isShiftDown() && editingFolder.get() == null) {
            e.consume();
            Platform.runLater(() -> searchBar.focus());
        }
    }
}
