package ploiu.ui;

import io.reactivex.rxjava3.core.Single;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;
import ploiu.event.AsyncEventReceiver;
import ploiu.event.EventReceiver;
import ploiu.event.folder.FolderDeleteEvent;
import ploiu.event.folder.FolderUpdateEvent;
import ploiu.model.FolderApi;
import ploiu.model.TagApi;
import ploiu.model.TextInputDialogOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import static ploiu.util.DialogUtils.showErrorDialog;

public class FolderInfo extends AnchorPane {
    private final ObjectProperty<FolderApi> folder = new SimpleObjectProperty<>(null);
    private final AsyncEventReceiver<FolderApi> folderReceiver;

    @FXML
    private VBox rootPane;
    @FXML
    private Label fileCountLabel;
    @FXML
    private Label folderCountLabel;
    @FXML
    private Button renameButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label folderTitle;
    @FXML
    private FlowPane tagList;
    @FXML
    private AnchorPane iconButtonWrapper;
    @FXML
    private VBox buttonWrapper;

    public FolderInfo(FolderApi folder, AsyncEventReceiver<FolderApi> folderReceiver) {
        this.folder.addListener(this::onFolderChanged);
        this.folder.setValue(folder);
        this.folderReceiver = folderReceiver;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FolderInfo/FolderInfo.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onFolderChanged(ObservableValue<? extends FolderApi> ignored, FolderApi ignored1, FolderApi newFolder) {
        Platform.runLater(() -> {
            tagList.getChildren().clear();
            folderTitle.setText(newFolder.name());
            var tags = newFolder.tags();
            for (var tag : tags) {
                var btn = new Button(tag.title());
                btn.getStyleClass().add("btn");
                btn.getStyleClass().add("btn-secondary");
                btn.getStyleClass().add("tag-btn");
                btn.setOnAction(action -> removeTag(tag));
                tagList.getChildren().add(btn);
            }
        });
    }

    private void removeTag(TagApi tag) {
        var f = folder.get();
        var updatedTags = new ArrayList<>(f.tags());
        updatedTags.remove(tag);
        var updatedFolder = new FolderApi(f.id(), f.parentId(), f.name(), f.path(), f.folders(), f.files(), updatedTags);
        updateFolder(updatedFolder);
    }

    @FXML
    void initialize() {
        this.fileCountLabel.setText("Files: " + folder.get().files().size());
        this.folderCountLabel.setText("Folders: " + folder.get().folders().size());
        this.folderTitle.setText(folder.get().name());
        setLayoutX(0);
        buttonSizeHandler();
        Platform.runLater(() -> {
            minWidthProperty().bind(getScene().widthProperty());
            minHeightProperty().bind(getScene().heightProperty());
            rootPane.minHeightProperty().bind(minHeightProperty());
            var winWidth = getScene().getWindow().widthProperty();
            updateWidth(winWidth.doubleValue());
            winWidth.addListener((obs, old, newWidth) -> {
                updateWidth(newWidth.doubleValue());
            });
        });
    }

    private void updateFolder(FolderApi updatedFolder) {
        var event = new FolderUpdateEvent(updatedFolder);
        folderReceiver.process(event)
                .observeOn(JavaFxScheduler.platform())
                .doOnError(e -> showErrorDialog("Failed to update folder, error is " + e.getMessage(), "Failed to update folder", null))
                .doOnSuccess(success -> {
                    if (success) {
                        this.folder.setValue(updatedFolder);
                    }
                })
                .subscribe();
    }

    @FXML
    void addTagClicked() {
        EventReceiver<String> confirmCallback = res -> {
            var f = folder.get();
            var tags = new HashSet<>(f.tags());
            tags.add(new TagApi(null, res.get(), null));
            var updatedFolder = new FolderApi(f.id(), f.parentId(), f.name(), f.path(), f.folders(), f.files(), tags);
            updateFolder(updatedFolder);
            return true;
        };
        new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), confirmCallback, "Enter Tag Title"));
    }

    @FXML
    void renameClicked() {
        EventReceiver<String> callback = e -> {
            var val = e.get();
            if (val != null && !val.isBlank()) {
                var old = folder.get();
                var newFolder = new FolderApi(old.id(), old.parentId(), val, old.path(), old.folders(), old.files(), old.tags());
                updateFolder(newFolder);
                return true;
            }
            return false;
        };
        new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), callback, "Rename Folder"));
    }

    @FXML
    void deleteClicked() {
        EventReceiver<String> dialogCallback = res -> {
            // make the user type the folder name they're deleting to confirm
            if (res.get().equals(folder.get().name())) {
                folderReceiver.process(new FolderDeleteEvent(folder.get()))
                        .onErrorResumeNext(e -> {
                            showErrorDialog(e.getMessage(), "Failed to Delete Folder", null);
                            return Single.never();
                        })
                        .doOnSuccess(ignoredRes -> folder.setValue(null))
                        .subscribe();
                return true;
            }
            showErrorDialog("[" + res.get() + "] does not match the folder name [" + folder.get().name() + "]", "Failed to Delete Folder", null);
            return false;
        };
        new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), dialogCallback, "Delete")
                .bodyText("Are you sure you want to delete? Type the name of the folder and click Confirm to delete")
                .windowTitle("Confirm Delete?"));
    }

    private void updateWidth(double width) {
        if (width < 800) {
            rootPane.setMinWidth(width);
        } else {
            rootPane.setMinWidth(width / 2 - 10);
        }
    }

    private void buttonSizeHandler() {
        iconButtonWrapper.prefWidthProperty().bind(rootPane.minWidthProperty());
        renameButton.prefWidthProperty().bind(buttonWrapper.widthProperty());
        deleteButton.prefWidthProperty().bind(buttonWrapper.widthProperty());
    }
}
