package ploiu.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class MainFrame {
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;

    public void initialize() {
        searchButton.setOnAction(event -> System.out.println(searchField.getText()));
    }
}
