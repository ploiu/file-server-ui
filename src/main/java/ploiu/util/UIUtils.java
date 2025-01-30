package ploiu.util;

import javafx.scene.image.Image;

import java.awt.Desktop;
import java.util.HashMap;
import java.util.Map;

import static ploiu.Constants.LIST_IMAGE_SIZE;

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
                    MIME_IMAGE_MAPPING.put(mimeType, new Image(icon, LIST_IMAGE_SIZE, LIST_IMAGE_SIZE, true, true));
                });
    }

    public static String convertSizeToBytes(Long bytes) {
        var kib = 1024f;
        var mib = kib * 1024f;
        var gib = mib * 1024f;
        var tib = gib * 1024f;
        var pib = tib * 1024f;
        if (bytes < kib) {
            return bytes + " bytes";
        } else if (bytes < mib) {
            return String.format("%.1f KiB", bytes / kib);
        } else if (bytes < gib) {
            return String.format("%.1f MiB", bytes / mib);
        } else if (bytes < tib) {
            return String.format("%.1f GiB", bytes / gib);
        } else if (bytes < pib) {
            return String.format("%.1f TiB", bytes / tib);
        } else {
            // no way will there ever be 1 singular file that large
            return String.format("%.1f PiB", bytes / pib);
        }
    }

    private UIUtils() {
    }
}
