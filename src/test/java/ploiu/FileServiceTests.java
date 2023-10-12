package ploiu;

import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.FileClient;
import ploiu.model.FileApi;
import ploiu.service.FileService;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static ploiu.Constants.CACHE_DIR;

@ExtendWith(MockitoExtension.class)
public class FileServiceTests {
    private final File saveDir = new File("file_server_ui_test_temp_dir").getAbsoluteFile();

    @Mock
    FileClient fileClient;

    @InjectMocks
    FileService fileService;

    @BeforeEach
    void setup() {
        saveDir.mkdirs();
        saveDir.deleteOnExit();
    }

    @AfterEach
    void teardown() {
        saveDir.delete();
    }

    @Test
    void testSaveAndGetFile_CACHE_DIR() throws Exception {
        when(fileClient.getFileContents(anyLong()))
                .thenReturn(Single.just(new ByteArrayInputStream("test".getBytes())));
        var savedFile = fileService.saveAndGetFile(new FileApi(0, "test.txt"), null)
                .blockingGet();
        assertEquals(new File(CACHE_DIR), savedFile.getParentFile());
        // cache dir can have a lot of files, so append the file ID to help make it unique
        assertEquals("0_test.txt", savedFile.getName());
        try (var reader = new BufferedReader(new FileReader(savedFile))) {
            var lines = reader.lines().collect(Collectors.joining("\n"));
            assertEquals("test", lines);
        }
        savedFile.delete();
    }

    @Test
    void testSaveAndGetFile() throws Exception {
        when(fileClient.getFileContents(anyLong()))
                .thenReturn(Single.just(new ByteArrayInputStream("test".getBytes())));
        var savedFile = fileService.saveAndGetFile(new FileApi(0, "test.txt"), saveDir)
                .blockingGet();
        assertEquals(saveDir, savedFile.getParentFile());
        assertEquals("test.txt", savedFile.getName());
        try (var reader = new BufferedReader(new FileReader(savedFile))) {
            var lines = reader.lines().collect(Collectors.joining("\n"));
            assertEquals("test", lines);
        }
        savedFile.delete();
    }
}
