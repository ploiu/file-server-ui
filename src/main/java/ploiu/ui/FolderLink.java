package ploiu.ui;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.Getter;
import ploiu.model.FolderApi;

import java.io.IOException;

public class FolderLink extends HBox {
    @Getter
    private final FolderApi folder;

    @FXML
    private Hyperlink link;

    public FolderLink(FolderApi folder) {
        super();
        this.folder = folder;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FolderLink/FolderLink.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            link.setText(folder.path());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setOnClick(EventHandler<MouseEvent> e) {
        link.setOnMouseClicked(e);
    }
}
