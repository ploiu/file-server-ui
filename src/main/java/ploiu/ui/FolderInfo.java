package ploiu.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import ploiu.event.AsyncEventReceiver;
import ploiu.model.FolderApi;

import java.io.IOException;

public class FolderInfo extends AnchorPane {
    private final FolderApi folder;
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
    private Button addTagButton;
    @FXML
    private FlowPane tagList;

    public FolderInfo(FolderApi folder, AsyncEventReceiver<FolderApi> folderReceiver) {
        this.folder = folder;
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

    @FXML
    void initialize() {
        // TODO need to call folder service
        this.fileCountLabel.setText("Files: " + folder.files().size());
        this.folderCountLabel.setText("Folders: " + folder.folders().size());
        Platform.runLater(() -> {
            var window = getScene().getWindow();
            var winWidth = window.widthProperty();
            var winHeight = window.heightProperty();
            updateWidth(winWidth.doubleValue());
            this.setMinHeight(winHeight.doubleValue());
            rootPane.setMinHeight(winHeight.doubleValue());
            winWidth.addListener((obs, old, newWidth) -> {
                updateWidth(newWidth.doubleValue());
            });
            winHeight.addListener((obs, old, newHeight) -> {
                this.setMinHeight(newHeight.doubleValue());
                rootPane.setMinHeight(newHeight.doubleValue());
            });
        });
    }

    @FXML
    void addTagClicked(MouseEvent e) {

    }

    private void updateWidth(double width) {
        setMinWidth(width);
        setLayoutX(0);
        if (width < 800) {
            rootPane.setMinWidth(width);
            rootPane.setLayoutX(0);
        } else {
            rootPane.setMinWidth(width / 2);
            rootPane.setLayoutX(width / 2);
        }
    }
}
