package ploiu.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import ploiu.model.FolderApi;

import java.io.IOException;

public class FolderEntry extends AnchorPane {
    @FXML
    private ImageView icon;

    @FXML
    private Label folderName;

    /** the folder backing this UI element */
    private FolderApi folder;

    public FolderEntry(FolderApi folder) {
        this.folder = folder;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FolderEntry.fxml"));
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
