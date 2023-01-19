package ploiu.ui;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;

/**
 * Custom component to play videos, with a play/pause button and a seeking slider
 */
public class VideoPlayer extends StackPane {
    @FXML
    private MediaView view;
    @FXML
    private Button playButton;
    @FXML
    private Region trackBackground;
    @FXML
    private Region trackForeground;
    @FXML
    private StackPane trackBox;
    private MediaPlayer player;

    // last time the mouse was moved in window; used for showing/hiding controls
    private long lastMouseMoveTime;

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
            view.setPreserveRatio(true);
            player.setOnReady(this::initBehaviors);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        player.stop();
        player.dispose();
    }

    private void initBehaviors() {
        resizeElements();
        trackBoxBehavior();
    }

    private void resizeElements() {
        var stage = (Stage) getScene().getWindow();
        stage.sizeToScene();
        view.fitWidthProperty().bind(stage.widthProperty());
        view.fitHeightProperty().bind(stage.heightProperty());
        ChangeListener<Number> trackBoxResizer = (observable, oldValue, newValue) -> trackBox.setPrefWidth(newValue.intValue());
        stage.widthProperty().addListener(trackBoxResizer);
    }

    private void trackBoxBehavior() {
        var scene = getScene();
        //trackBox.setVisible(false);
        var mediaLength = player.getMedia().getDuration();
        // TODO change to bottom once we figure out why certain aspect ratios hide it
        setAlignment(trackBox, Pos.TOP_CENTER);
        var hideDuration = Duration.ofSeconds(2);
        scene.setOnMouseMoved(e -> {
            trackBox.setVisible(true);
            lastMouseMoveTime = System.currentTimeMillis();
           new Thread(() -> {
               try {
                   Thread.sleep(hideDuration.toMillis());
               } catch (InterruptedException ex) {
                   throw new RuntimeException(ex);
               }
               // don't hide if the user has moved the mouse since last time
               if (System.currentTimeMillis() - lastMouseMoveTime >= hideDuration.toMillis()) {
                   trackBox.setVisible(false);
               }
           }).start();
        });
        player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            var percentDone = newValue.toSeconds() / mediaLength.toSeconds();
            trackForeground.setPrefWidth(scene.getWidth() * percentDone);
        });
    }
}
