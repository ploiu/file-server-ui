package ploiu;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.FileClient;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.CreateFileRequest;
import ploiu.model.FileApi;
import ploiu.model.UpdateFileRequest;
import ploiu.service.FileService;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
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
    void testGetFile_Contents_CACHE_DIR() throws Exception {
        when(fileClient.getFileContents(anyLong()))
                .thenReturn(Single.just(new ByteArrayInputStream("test".getBytes())));
        var savedFile = fileService.getFileContents(new FileApi(0, "test.txt", List.of(), null), null)
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
    void testGetFileContents() throws Exception {
        when(fileClient.getFileContents(anyLong()))
                .thenReturn(Single.just(new ByteArrayInputStream("test".getBytes())));
        var savedFile = fileService.getFileContents(new FileApi(0, "test.txt", List.of(), null), saveDir)
                .blockingGet();
        assertEquals(saveDir, savedFile.getParentFile());
        assertEquals("test.txt", savedFile.getName());
        try (var reader = new BufferedReader(new FileReader(savedFile))) {
            var lines = reader.lines().collect(Collectors.joining("\n"));
            assertEquals("test", lines);
        }
        savedFile.delete();
    }

    @Test
    void testGetMetadataThrowsExceptionIfIdIsNegative() {
        var e = assertThrows(Exception.class, () -> fileClient.getMetadata(-1).blockingGet()).getCause();
        assertInstanceOf(BadFileRequestException.class, e);
        assertEquals("Id cannot be negative.", e.getMessage());
    }

    @Test
    void testGetMetadataThrowsExceptionIfFileIsNotFound() throws Exception {
        when(fileClient.getMetadata(anyLong())).thenReturn(Maybe.empty());
        var e = assertThrows(BadFileResponseException.class, () -> fileService.getMetadata(1).blockingGet());
        assertEquals("The file with the passed id could not be found.", e.getMessage());
    }

    @Test
    void testDeleteFileThrowsExceptionIfIdIsNegative() {
        var e = assertThrows(BadFileRequestException.class, () -> fileService.deleteFile(-1).blockingAwait()).getCause();
        assertEquals("Id cannot be negative.", e.getMessage());
    }

    @Test
    void testGetFileContentsThrowsExceptionIfIdIsNegative() {
        var e = assertThrows(BadFileRequestException.class, () -> fileService.getFileContents(-1).blockingGet()).getCause();
        assertEquals("Id cannot be negative.", e.getMessage());
    }

    @Test
    void testCreateFileRequiresNonNullFile() {
        var e = assertThrows(NullPointerException.class, () -> fileService.createFile(new CreateFileRequest(0, null)).blockingGet());
        assertEquals("File cannot be null.", e.getMessage());
    }

    @Test
    void testCreateFileRequiresFileToExist() {
        var e = assertThrows(BadFileRequestException.class, () -> fileService.createFile(new CreateFileRequest(0, new File("bad file.bad file" + System.currentTimeMillis()))).blockingGet()).getCause();
        assertEquals("The selected file does not exist.", e.getMessage());
    }

    @Test
    @DisplayName("CreateFile does not pass trailing dot to client if no file extension is passed")
    void testCreateFileWithoutExtensionNoTrailingDot() {
        fail("unimplemented");
    }

    @EmptySource
    @ParameterizedTest(name = "Test that [{0}] is not accepted as a file name for update")
    @ValueSource(strings = {" "})
    void testUpdateFileRequiresFileName(String name) {
        var e = assertThrows(Exception.class, () -> fileService.updateFile(new UpdateFileRequest(0, 0, name, List.of())).blockingGet()).getCause();
        assertInstanceOf(BadFileRequestException.class, e);
        assertEquals("Name cannot be blank.", e.getMessage());
    }

    @Test
    void testUpdateFileRequiresPositiveId() {
        var e = assertThrows(Exception.class, () -> fileService.updateFile(new UpdateFileRequest(-1, 0, "name", List.of())).blockingGet()).getCause();
        assertInstanceOf(BadFileRequestException.class, e);
        assertEquals("Id cannot be negative.", e.getMessage());
    }

}
