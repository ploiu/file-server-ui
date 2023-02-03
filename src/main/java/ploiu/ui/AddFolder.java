package ploiu.ui;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AddFolder extends AnchorPane {

    public AddFolder(EventHandler<MouseEvent> event) {
        super();
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/AddFolder/AddFolder.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        this.setOnMouseClicked(event);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
