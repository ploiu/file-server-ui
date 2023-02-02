package ploiu.util;

import java.net.URLConnection;

public class MimeUtils {
    private static final String IMAGE_DIR = "assets/img";

    public static String getFileIconForFileName(String fileName) {
        var loader = MimeUtils.class.getClassLoader();
        var mimeType = URLConnection.guessContentTypeFromName(fileName);
        if (mimeType == null) {
            return loader.getResource(IMAGE_DIR + "/unknown.png").toString();
        }
        var generalType = mimeType.split("/")[0];
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
        try {
            return switch (generalType) {
                case "application":
                    yield loader.getResource(IMAGE_DIR + "/application.png").toString();
                case "audio":
                    yield loader.getResource(IMAGE_DIR + "/audio.png").toString();
                case "font":
                    yield loader.getResource(IMAGE_DIR + "/font.png").toString();
                case "image":
                    yield loader.getResource(IMAGE_DIR + "/image.png").toString();
                case "message":
                    yield loader.getResource(IMAGE_DIR + "/message.png").toString();
                case "model":
                    yield loader.getResource(IMAGE_DIR + "/model.png").toString();
                case "multipart":
                    yield loader.getResource(IMAGE_DIR + "/multipart.png").toString();
                case "text":
                    yield loader.getResource(IMAGE_DIR + "/text.png").toString();
                case "video":
                    yield loader.getResource(IMAGE_DIR + "/video.png").toString();
                default:
                    yield loader.getResource(IMAGE_DIR + "/unknown.png").toString();
            };
        } catch (Exception e) {
            return loader.getResource(IMAGE_DIR + "/unknown.png").toString();
        }
    }
}
