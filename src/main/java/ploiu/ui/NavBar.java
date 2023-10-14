package ploiu.ui;

import javafx.beans.NamedArg;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;
import ploiu.event.AsyncEventReceiver;
import ploiu.event.Event;
import ploiu.model.FolderApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NavBar extends HBox {
    // the "stack" of folders that represents our journey to the current folder
    private final List<FolderApi> folders = new LinkedList<>();
    private final AsyncEventReceiver<FolderApi> receiver;

    public NavBar(@NamedArg("receiver") AsyncEventReceiver<FolderApi> receiver) {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/NavBar/NavBar.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.receiver = receiver;
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
            receiver.process(new Event<>(folder))
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
        for (var folder : folders) {
            var splitPath = folder.path().split("/");
            var link = new Label(splitPath[splitPath.length - 1]);
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
        }
    }
}
