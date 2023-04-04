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
import java.util.HashMap;
import java.util.Map;

public class FileEntry extends AnchorPane {
    // cache because creating an image takes a lot of time
    private static final Map<String, Image> MIME_IMAGE_MAPPING = new HashMap<>();

    // load all the icons into memory on application start, instead of on the fly
    static {
        for (var mimeType : MimeUtils.MIME_TYPES) {
            var icon = MimeUtils.getFileIconForMimeType(mimeType);
            MIME_IMAGE_MAPPING.put(mimeType, new Image(icon, 100.25, 76.25, true, true));
        }
    }

    private final FileApi file;
    @FXML
    private ImageView icon;
    @FXML
    private Label fileName;

    public FileEntry(FileApi file) {
        super();
        this.file = file;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FileEntry/FileEntry.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            this.fileName.setText(file.name());
            var image = MIME_IMAGE_MAPPING.get(MimeUtils.determineMimeType(file.name()));
            icon.setImage(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
