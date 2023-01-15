package ploiu.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.MediaView;
import ploiu.client.FileClient;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.FileApi;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ploiu.Constants.CACHE_DIR;

public class FileView extends AnchorPane {
    @FXML
    private ImageView imageView;
    @FXML
    private MediaView mediaView;
    @FXML
    private Spinner<?> loader;
    @FXML
    private Label errorMessage;

    private FileApi fileApi;
    private File savedFile;
    private final FileClient client = App.INJECTOR.getInstance(FileClient.class);

    public FileView(FileApi fileApi) {
        this.fileApi = fileApi;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FileView/FileView.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            this.savedFile = saveAndGetFile(fileApi);
            initView();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (BadFileRequestException e) {
            errorMessage.setText("That file does not exist.");
        } catch (BadFileResponseException e) {
            errorMessage.setText("Failed to download file.");
        }
    }

    private void initView() {
        var fileUri = savedFile.toURI().toString();
        var mimeType = Optional.ofNullable(URLConnection.guessContentTypeFromName(savedFile.getName()))
                .map(it -> it.split("/")[0])
                .orElse("unknown");
        if ("unknown".equals(mimeType)) {
            // TODO
            throw new UnsupportedOperationException("TODO: default view when we can't render file");
        } else if ("image".equals(mimeType)) {
            loadAsImage(fileUri);
        }
    }

    private File saveAndGetFile(FileApi fileApi) throws BadFileRequestException, BadFileResponseException, IOException {
        // ensure the cache directory exists
        new File(CACHE_DIR).mkdir();
        var cacheFile = new File(CACHE_DIR + "/" + fileApi.id() + "_" + fileApi.name());
        if (!cacheFile.exists()) {
            var inStream = client.getFileContents(fileApi.id());
            cacheFile.createNewFile();
            Files.copy(inStream, cacheFile.toPath(), REPLACE_EXISTING);
        }
        return cacheFile;
    }

    private void loadAsImage(String fileUrl) {
        var image = new Image(fileUrl);
        imageView.setImage(image);
        imageView.autosize();
        setWidth(imageView.getFitWidth());
        setHeight(imageView.getFitHeight());
    }
}
