package ploiu.util;

import javafx.scene.image.Image;

import java.awt.Desktop;
import java.util.HashMap;
import java.util.Map;

/**
 * Created to offload the loading of file images off of the {@link ploiu.ui.FileEntry} class.
 * This should hold methods and fields to help the ui side of the application
 */
public final class UIUtils {

    // cache because creating an image takes a lot of time
    public static final Map<String, Image> MIME_IMAGE_MAPPING = new HashMap<>();
    public static final Desktop desktop = Desktop.getDesktop();

    // load all the icons into memory on application start, instead of on the fly
    public static void init() {
        MimeUtils.MIME_TYPES
                .parallelStream()
                .forEach(mimeType -> {
                    var icon = MimeUtils.getFileIconForMimeType(mimeType);
                    MIME_IMAGE_MAPPING.put(mimeType, new Image(icon, 100.25, 76.25, true, true));
                });
    }

    private UIUtils() {
    }
}
