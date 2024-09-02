package ploiu.util;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MimeUtils {
    /*
    application (gear)
    audio       (music note, done)
    font        (F, done)
    image       (sun with mountain, done)
    message     (envelope)
    model       (cube)
    multipart   (puzzle)
    text        (lines of text, done)
    video       (play button)
    unknown     (question mark, done)
     */
    public static final List<String> MIME_TYPES = List.of("application", "audio", "font", "image", "message", "model", "multipart", "text", "video", "unknown");
    public static final Map<String, String> MIME_TYPE_ICON_NAMES = new HashMap<>();
    private static final String IMAGE_DIR = "assets/img";

    static {
        MIME_TYPES.forEach(type -> MIME_TYPE_ICON_NAMES.put(type, IMAGE_DIR + "/" + type + ".png"));
    }

    public static String getFileIconForMimeType(String mimeType) {
        var loader = MimeUtils.class.getClassLoader();
        try {
            return loader.getResource(MIME_TYPE_ICON_NAMES.get(mimeType)).toString();
        } catch (Exception e) {
            return loader.getResource(IMAGE_DIR + "/unknown.png").toString();
        }
    }

    public static String determineMimeType(String fileName) {
        var mimeType = URLConnection.guessContentTypeFromName(fileName);
        if (mimeType == null) {
            var split = fileName.toLowerCase().split("\\.");
            var extension = split[split.length - 1];
            return switch (extension.toLowerCase()) {
                case "stl" -> "model";
                default -> "unknown";
            };
        }
        return mimeType.split("/")[0];
    }
}
