package ploiu.ui;

import javafx.beans.NamedArg;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;
import ploiu.event.AsyncEventReceiver;
import ploiu.event.Event;
import ploiu.event.FileUpdateEvent;
import ploiu.event.FolderEvent;
import ploiu.model.FileApi;
import ploiu.model.FileApiWithFolder;
import ploiu.model.FileObject;
import ploiu.model.FolderApi;
import ploiu.service.DragNDropService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class NavBar extends HBox {
    // the "stack" of folders that represents our journey to the current folder
    private final List<FolderApi> folders = new LinkedList<>();
    private final AsyncEventReceiver<FolderApi> navigationReceiver;
    private final DragNDropService dragNDropService = App.INJECTOR.getInstance(DragNDropService.class);
    private final AsyncEventReceiver<FileObject> fileReceiver;
    private final AsyncEventReceiver<FolderApi> folderReceiver;

    public NavBar(
            @NamedArg("navigationReceiver") AsyncEventReceiver<FolderApi> navigationReceiver,
            @NamedArg("folderReceiver") AsyncEventReceiver<FolderApi> folderReceiver,
            @NamedArg("fileReceiver") AsyncEventReceiver<FileObject> fileReceiver
    ) {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/NavBar/NavBar.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.navigationReceiver = navigationReceiver;
        this.folderReceiver = folderReceiver;
        this.fileReceiver = fileReceiver;
    }

    public void push(FolderApi folder) {
        if (folders.contains(folder)) {
            throw new UnsupportedOperationException("Cannot push folder already on the stack");
        }
        folders.add(folder);
        render();
    }

    private void selectFolder(FolderApi folder) {
        // pop all the folders after the current one
        var folderIndex = folders.indexOf(folder);
        // if it's the last index, don't do anything because we're already there; index 0 means we're going to root, so always heed that
        if (folderIndex == 0 || folderIndex != folders.size() - 1) {
            navigationReceiver.process(new Event<>(folder))
                    .observeOn(JavaFxScheduler.platform())
                    .doOnSuccess(success -> {
                        if (success) {
                            var subList = new ArrayList<>(folders.subList(0, folderIndex + 1));
                            folders.clear();
                            folders.addAll(subList);
                            // tell the receiver that we've selected a folder
                            render();
                        }
                    })
                    .subscribe();
        }
    }

    private void render() {
        // remove all entries from the ui, this can be optimized but is ok for now since this will be a user-activated method
        this.getChildren().clear();
        for (int i = 0; i < folders.size(); i++) {
            var folder = folders.get(i);
            var link = new Label(folder.name());
            link.getStyleClass().add("folder-link");
            var divider = new Label("/");
            divider.getStyleClass().add("text");
            this.getChildren().add(link);
            // to fix spacing...may be able to put in css
            this.setAlignment(Pos.BOTTOM_LEFT);
            this.getChildren().add(divider);
            // link is clickable and navigates to that folder
            link.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    selectFolder(folder);
                }
            });
            // allow moving items to earlier folders
            if (i != folders.size()) {
                link.setOnDragOver(event -> {
                    event.consume();
                    event.acceptTransferModes(TransferMode.MOVE);
                    link.getStyleClass().add("selected");
                });
                link.setOnDragExited(ignored -> link.getStyleClass().removeAll(List.of("selected")));
                link.setOnDragDropped(event -> {
                    var board = event.getDragboard();
                    if (!event.isConsumed()) {
                        if (board.hasFiles()) {
                            event.consume();
                            dragNDropService.dropFiles(board.getFiles(), folder, getScene().getWindow())
                                    .subscribe();
                        } else if (board.getContent(DataTypes.FOLDER) instanceof FolderApi droppedFolder) {
                            event.consume();
                            var newApi = new FolderApi(droppedFolder.id(), folder.id(), droppedFolder.name(), droppedFolder.path(), droppedFolder.folders(), droppedFolder.files(), droppedFolder.tags());
                            folderReceiver.process(new FolderEvent(newApi, FolderEvent.Type.UPDATE))
                                    .subscribe();
                        } else if (board.getContent(DataTypes.FILE) instanceof FileApi droppedFile) {
                            event.consume();
                            var newApi = new FileApiWithFolder(droppedFile.id(), droppedFile.name(), Optional.of(folder.id()));
                            fileReceiver.process(new FileUpdateEvent(newApi))
                                    .subscribe();
                        }
                    }
                });
            }
        }
    }
}
