package ploiu.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import ploiu.model.FolderApi;

import java.io.IOException;

public class FolderEntry extends AnchorPane {
    /**
     * the folder backing this UI element
     */
    @Getter
    private final FolderApi folder;
    @FXML
    private ImageView icon;
    @FXML
    private Label folderName;

    public FolderEntry(FolderApi folder) {
        this.folder = folder;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FolderEntry/FolderEntry.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            // currently api doesn't have a field for name, just full path
            var splitPath = folder.path().split("/");
            var name = splitPath[splitPath.length - 1];
            this.folderName.setText(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
