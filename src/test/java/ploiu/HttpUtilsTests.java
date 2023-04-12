package ploiu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static ploiu.util.HttpUtils.multipart;

public class HttpUtilsTests {

    @Test
    @DisplayName("multipart generates a random boundary")
    void testMultipartBoundary() {
        var part = multipart(Map.of());
        assertFalse(part.boundary().isBlank());
        assertTrue(part.boundary().matches("^\\d+$"));
    }

    @RepeatedTest(10) // map sorting can be weird...make sure that the tests are consistent
    @DisplayName("multipart generates a body with the boundary")
    void testMultipartBody() {
        // map order only matters during tests, which is why we use TreeMap here
        var body = new TreeMap<>(Map.<String, Object>of(
                "test", "hello",
                "test2", 5
        ));
        var part = multipart(body);
        var expectedBody = """
                ----------------------------$boundary
                Content-Disposition: form-data; name="test"
                                
                hello
                ----------------------------$boundary
                Content-Disposition: form-data; name="test2"
                                
                5
                ----------------------------$boundary--
                """.replace("$boundary", part.boundary()).strip();
        assertEquals(expectedBody, part.body());
    }

    @RepeatedTest(10) // map sorting can be weird...make sure that the tests are consistent
    @DisplayName("multipart handles files properly")
    void testMultipartFile() {
        var file = new File(getClass().getClassLoader().getResource("test.txt").getPath());
        try (var reader = new BufferedReader(new FileReader(file))) {
            var fileText = reader.lines().collect(Collectors.joining("\n"));
            // map order only matters during tests, which is why we use TreeMap here
            var body = new TreeMap<>(Map.<String, Object>of(
                    "extension", "txt",
                    "file", file,
                    "folder_id", 5
            ));
            var part = multipart(body);
            var expectedBody = """
                    ----------------------------$boundary
                    Content-Disposition: form-data; name="extension"
                                        
                    txt
                    ----------------------------$boundary
                    Content-Disposition: form-data; name="file"; filename="$fileName"
                    Content-Type: text/plain
                     
                    $fileText
                    ----------------------------$boundary
                    Content-Disposition: form-data; name="folder_id"
                                        
                    5
                    ----------------------------$boundary--
                    """.replace("$boundary", part.boundary())
                    .replace("$fileText", fileText)
                    .replace("$fileName", file.getName()).trim();
            assertEquals(expectedBody, part.body());
        } catch (IOException e) {
            fail(e);
        }
    }
}
