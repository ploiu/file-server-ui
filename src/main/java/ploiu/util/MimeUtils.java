package ploiu.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MimeUtils {
    /*
    application         (gear)
    archive             (zipper mark)
    audio               (music note)
    cad                 (puzzle)
    code                (curly braces)
    configuration       (xml end tag)
    diagram             (ugly diagram)
    document            (clone of text just slightly modified)
    font                (F)
    image               (sun with mountain)
    material            (cube)
    model               (cube)
    object              (cube)
    presentation        (slide deck)
    rom                 (bad nes cartridge)
    savefile            (floppy disk)
    spreadsheet         (awful spreadsheet)
    text                (lines of text)
    unknown             (question mark)
    video               (play button)
     */
    public static final List<String> FILE_TYPES = List.of(
            "application",
            "archive",
            "audio",
            "cad",
            "code",
            "configuration",
            "diagram",
            "document",
            "font",
            "image",
            "material",
            "model",
            "object",
            "presentation",
            "rom",
            "savefile",
            "spreadsheet",
            "text",
            "unknown",
            "video"
    );
    public static final Map<String, String> MIME_TYPE_ICON_NAMES = new HashMap<>();
    private static final String IMAGE_DIR = "assets/img";

    static {
        FILE_TYPES.forEach(type -> MIME_TYPE_ICON_NAMES.put(type, IMAGE_DIR + "/" + type + ".png"));
    }

    public static String getFileIconForType(String fileType) {
        var loader = MimeUtils.class.getClassLoader();
        try {
            return loader.getResource(MIME_TYPE_ICON_NAMES.get(fileType.toLowerCase())).toString();
        } catch (Exception e) {
            return loader.getResource(IMAGE_DIR + "/unknown.png").toString();
        }
    }
}
