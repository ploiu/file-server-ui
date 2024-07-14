package ploiu.ui;

import io.reactivex.rxjava3.core.Single;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ploiu.event.*;
import ploiu.model.ConfirmDialogOptions;
import ploiu.model.FileApi;
import ploiu.model.FileObject;
import ploiu.model.TextInputDialogOptions;
import ploiu.util.MimeUtils;
import ploiu.util.UIUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static ploiu.util.DialogUtils.showErrorDialog;

public class FileEntry extends AnchorPane {

    @Getter
    private final FileApi file;
    private final AsyncEventReceiver<FileObject> fileReceiver;
    private final ObjectProperty<FileApi> editingFile;
    private final ObjectProperty<Image> previewImage;

    @FXML
    private ImageView icon;
    @FXML
    private Label fileName;
    @FXML
    private ContextMenu fileMenu;

    public FileEntry(FileApi file, AsyncEventReceiver<FileObject> eventHandler, ObjectProperty<FileApi> editingFile, @NotNull ObjectProperty<Image> previewImage) {
        super();
        this.editingFile = editingFile;
        this.file = file;
        this.previewImage = previewImage;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FileEntry/FileEntry.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            this.fileName.setText(file.name());
            var image = UIUtils.MIME_IMAGE_MAPPING.get(MimeUtils.determineMimeType(file.name()));
            icon.setImage(image);
            this.fileReceiver = eventHandler;
            Tooltip.install(this, new Tooltip(file.name()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void initialize() {
        previewImage.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                icon.setImage(UIUtils.MIME_IMAGE_MAPPING.get(MimeUtils.determineMimeType(file.name())));
            } else {
                icon.setImage(newValue);
            }
        });
        this.setOnContextMenuRequested(event -> fileMenu.show(this, event.getScreenX(), event.getScreenY()));
    }

    @FXML
    private void renameItemClicked(ActionEvent event) {
        EventReceiver<String> renameCallback = evt -> {
            var newName = evt.get();
            if (newName != null && !newName.isBlank()) {
                fileReceiver.process(new FileUpdateEvent(new FileApi(file.id(), newName, file.tags(), file.folderId())))
                        .onErrorResumeNext(e -> {
                            showErrorDialog(e.getMessage(), "Failed to Update File", null);
                            return Single.never();
                        })
                        .subscribe();
                return true;
            }
            return false;
        };
        new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), renameCallback, "Rename File").initialText(file.name()));
    }

    @FXML
    private void deleteItemClicked(ActionEvent ignored) {
        EventReceiver<Boolean> dialogCallback = res -> {
            if (res.get()) {
                fileReceiver.process(new FileDeleteEvent(file))
                        .onErrorResumeNext(e -> {
                            showErrorDialog(e.getMessage(), "Failed to Delete File", null);
                            return Single.never();
                        })
                        .subscribe();
                return true;
            }
            return false;
        };
        new ConfirmDialog(new ConfirmDialogOptions(getScene().getWindow(), dialogCallback, "Are you sure you want to delete this file?"));
    }

    @FXML
    private void saveAsClicked(ActionEvent ignored) {
        var chooser = new DirectoryChooser();
        // better user experience to default to the user dir, but also makes tests possible
        var homeDirectory = new File(System.getProperty("user.home"));
        chooser.setInitialDirectory(homeDirectory);
        chooser.setTitle("Save " + file.name() + "...");
        var selectedDir = chooser.showDialog(getScene().getWindow());
        if (selectedDir != null) {
            fileReceiver.process(new FileSaveEvent(file, selectedDir))
                    .subscribe();
        }
    }

    @FXML
    private void onDragDetected(MouseEvent e) {
        var board = startDragAndDrop(TransferMode.MOVE);

        board.setContent(Map.of(DataTypes.FILE, file.toJson()));
        e.consume();
    }

    @FXML
    private void infoItemClicked(ActionEvent ignored) {
        editingFile.set(file);
    }
}
