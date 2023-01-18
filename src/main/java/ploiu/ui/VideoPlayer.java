package ploiu.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;

import java.awt.*;
import java.io.IOException;

/**
 * Custom component to play videos, with a play/pause button and a seeking slider
 */
public class VideoPlayer extends VBox {
    @FXML
    private MediaView view;
    @FXML
    private Button playButton;
    @FXML
    private Rectangle track;
    private MediaPlayer player;
    private boolean isPlaying;

    public VideoPlayer(String fileUrl) {
        try {
            var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/VideoPlayer/VideoPlayer.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
            var media = new Media(fileUrl);
            this.player = new MediaPlayer(media);
            player.setAutoPlay(true);
            view.setMediaPlayer(player);
            player.setOnReady(() -> getScene().getWindow().sizeToScene());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        player.stop();
    }
}
