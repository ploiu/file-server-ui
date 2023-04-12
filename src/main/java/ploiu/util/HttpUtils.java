package ploiu.util;

import ploiu.model.Multipart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpUtils {
    /**
     * creates an object containing information for a multipart/form-data request
     *
     * @param properties the properties to encode into the form
     * @return a {@link Multipart} object, containing the boundary as well as the body
     */
    public static Multipart multipart(Map<String, Object> properties) {
        var boundary = Long.toString(System.currentTimeMillis());
        var body = properties.entrySet().stream().map(entry -> buildPart(entry.getKey(), entry.getValue(), boundary)).collect(Collectors.joining("\n")) + "\n----------------------------" + boundary + "--";
        return new Multipart(boundary, body);
    }

    private static String buildPart(String name, Object value, String boundary) {
        if (value instanceof File file) {
            return buildPartFile(name, file, boundary);
        }
        return """
                ----------------------------$boundary
                Content-Disposition: form-data; name="$name"
                                
                $value
                """.strip()
                .replace("$boundary", boundary)
                .replace("$name", name)
                .replace("$value", value.toString());
    }

    private static String buildPartFile(String name, File value, String boundary) {
        var mediaType = URLConnection.guessContentTypeFromName(value.getName());
        try (var reader = new BufferedReader(new FileReader(value))) {
            var text = reader.lines().collect(Collectors.joining("\n"));
            return """
                    ----------------------------$boundary
                    Content-Disposition: form-data; name="$name"; filename="$fileName"
                    Content-Type: $mediaType
                                        
                    $fileText
                    """.strip()
                    .replace("$boundary", boundary)
                    .replace("$name", name)
                    .replace("$fileName", value.getName())
                    .replace("$mediaType", mediaType)
                    .replace("$fileText", text);
        } catch (IOException e) {
            // file can't be found...how tf did it get passed then in production code?
            throw new RuntimeException(e);
        }
    }
}
