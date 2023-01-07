package ploiu.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class MainFrame {
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private FlowPane folderPane;

    public void initialize() {
        searchButton.setOnAction(event -> folderPane.getChildren().add(new Circle(10, Paint.valueOf("seagreen"))));
    }
}
