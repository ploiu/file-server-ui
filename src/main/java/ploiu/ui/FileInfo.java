package ploiu.ui;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;
import ploiu.event.AsyncEventReceiver;
import ploiu.event.EventReceiver;
import ploiu.event.FileDeleteEvent;
import ploiu.event.FileUpdateEvent;
import ploiu.model.*;
import ploiu.util.MimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import static ploiu.util.DialogUtils.showErrorDialog;

public class FileInfo extends AnchorPane {
    private final ObjectProperty<FileApi> file = new SimpleObjectProperty<>(null);
    private final AsyncEventReceiver<FileObject> fileReceiver;
    private final String iconPathLocation;

    @FXML
    private VBox rootPane;
    @FXML
    private Button renameButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label fileTitle;
    @FXML
    private FlowPane tagList;
    @FXML
    private AnchorPane iconButtonWrapper;
    @FXML
    private HBox buttonWrapper;
    @FXML
    private ImageView fileIcon;
    @FXML
    private Button openButton;

    public FileInfo(FileApi file, AsyncEventReceiver<FileObject> fileReceiver) {
        this.file.addListener(this::onFileChanged);
        this.file.setValue(file);
        this.fileReceiver = fileReceiver;
        this.iconPathLocation = MimeUtils.MIME_TYPE_ICON_NAMES.get(MimeUtils.determineMimeType(file.name()));
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FileInfo/FileInfo.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onFileChanged(ObservableValue<? extends FileApi> ignored, FileApi ignored1, FileApi newFile) {
        if (newFile == null) {
            return;
        }
        Platform.runLater(() -> {
            tagList.getChildren().clear();
            fileTitle.setText(newFile.name());
            var tags = newFile.tags();
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
        var f = file.get();
        var updatedTags = new ArrayList<>(f.tags());
        updatedTags.remove(tag);
        var updatedFile = new FileApi(f.id(), f.name(), updatedTags, f.folderId());
        updateFile(updatedFile);
    }

    @FXML
    void initialize() {
        this.fileTitle.setText(file.get().name());
        setLayoutX(0);
        buttonSizeHandler();
        Platform.runLater(() -> {
            var image = new Image(iconPathLocation, 128, 128, true, true);
            fileIcon.setImage(image);
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

    private void updateFile(FileApi updatedFile) {
        var event = new FileUpdateEvent(updatedFile);
        fileReceiver.process(event)
                .observeOn(JavaFxScheduler.platform())
                .doOnError(e -> showErrorDialog("Failed to update file, error is " + e.getMessage(), "Failed to update file", null))
                .doOnSuccess(success -> {
                    if (success) {
                        this.file.setValue(updatedFile);
                    }
                })
                .subscribe();
    }

    @FXML
    void addTagClicked() {
        EventReceiver<String> confirmCallback = res -> {
            var f = file.get();
            var tags = new HashSet<>(f.tags());
            tags.add(new TagApi(null, res.get()));
            var updatedFile = new FileApi(f.id(), f.name(), tags, f.folderId());
            updateFile(updatedFile);
            return true;
        };
        new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), confirmCallback, "Enter Tag Title"));
    }

    @FXML
    void renameClicked() {
        EventReceiver<String> callback = e -> {
            var val = e.get();
            if (val != null && !val.isBlank()) {
                var old = file.get();
                var newFile = new FileApi(old.id(), val, old.tags(), old.folderId());
                updateFile(newFile);
                return true;
            }
            return false;
        };
        new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), callback, "Rename File"));
    }

    @FXML
    void deleteClicked() {
        EventReceiver<Boolean> callback = e -> {
            if (e.get()) {
                fileReceiver.process(new FileDeleteEvent(file.get()))
                        .doOnSuccess(ignored -> {
                            // TODO this is a different object than the editingFile object in MainFrame. Pass the value from MainFrame to here. Need to do the same with FolderInfo
                            file.setValue(null);
                        })
                        .subscribe();
            }
            return e.get();
        };
        new ConfirmDialog(new ConfirmDialogOptions(getScene().getWindow(), callback, "Are you sure you want to delete this file?"));
    }

    @FXML
    void openClicked() {
        throw new UnsupportedOperationException("unimplemented");
    }

    @FXML
    void saveClicked() {
        throw new UnsupportedOperationException("unimplemented");
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
        openButton.prefWidthProperty().bind(buttonWrapper.widthProperty());
        deleteButton.prefWidthProperty().bind(buttonWrapper.widthProperty());
    }
}
