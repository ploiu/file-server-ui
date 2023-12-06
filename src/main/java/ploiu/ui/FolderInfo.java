package ploiu.ui;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
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
import ploiu.event.FolderEvent;
import ploiu.model.FolderApi;
import ploiu.model.TagApi;
import ploiu.model.TextInputDialogOptions;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

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
                // TODO button spacing
            }
        });
    }

    private void removeTag(TagApi tag) {
        throw new UnsupportedOperationException();
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

    @FXML
    void addTagClicked(ActionEvent ignored) {
        EventReceiver<String> confirmCallback = res -> {
            var f = folder.get();
            var tags = new HashSet<>(f.tags());
            tags.add(new TagApi(Optional.empty(), res.get()));
            var updatedFolder = new FolderApi(f.id(), f.parentId(), f.name(), f.path(), f.folders(), f.files(), tags);
            var event = new FolderEvent(updatedFolder, FolderEvent.Type.UPDATE);
            folderReceiver.process(event)
                    .observeOn(JavaFxScheduler.platform())
                    .doOnError(e -> showErrorDialog("Failed to update folder tags, error is " + e.getMessage(), "Failed to update folder", null))
                    .doOnSuccess(success -> {
                        if (success) {
                            this.folder.setValue(updatedFolder);
                        }
                    })
                    .subscribe();
            return true;
        };
        new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), confirmCallback, "Enter Tag Title"));
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
