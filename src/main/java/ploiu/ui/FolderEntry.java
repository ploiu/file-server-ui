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
    @FXML
    private ImageView icon;

    @FXML
    private Label folderName;

    /**
     * the folder backing this UI element
     */
    @Getter
    private final FolderApi folder;

    public FolderEntry(FolderApi folder) {
        this.folder = folder;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FolderEntry/FolderEntry.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            this.folderName.setText(folder.path());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
