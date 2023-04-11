package ploiu.ui;

import javafx.beans.NamedArg;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import ploiu.event.Event;
import ploiu.event.EventReceiver;

import java.io.IOException;

@SuppressWarnings("unused")
public class SearchBar extends HBox {
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    private final EventReceiver<String> receiver;

    public SearchBar(@NamedArg("receiver") EventReceiver<String> receiver) {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/SearchBar/SearchBar.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.receiver = receiver;
    }

    @FXML
    private void searchButtonClicked(ActionEvent event) {
        receiver.process(new Event<>(searchButton.getText()));
    }

    @FXML
    private void searchBarKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            receiver.process(new Event<>(searchField.getText()));
        }
    }
}
