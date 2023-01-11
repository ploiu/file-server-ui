package ploiu.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import ploiu.model.FileApi;
import ploiu.util.MimeUtils;

import java.io.IOException;

public class FileEntry extends AnchorPane {
    @FXML
    private ImageView icon;

    @FXML
    private Label fileName;

    private final FileApi file;

    public FileEntry(FileApi file) {
        this.file = file;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FileEntry.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            this.fileName.setText(file.name());
            var image = new Image(MimeUtils.getFileIconForFileName(file.name()), 100.25, 76.25, true, true);
            icon.setImage(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
