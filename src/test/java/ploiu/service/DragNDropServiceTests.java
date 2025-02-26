package ploiu.service;

import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.TestHelper;
import ploiu.model.FileApi;
import ploiu.model.FolderApi;
import ploiu.model.FolderRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DragNDropServiceTests {
    static final File root = new File("src/test/resources/DragNDropService");
    static TestHelper helper = new TestHelper(root);
    static FolderApi rootApi = new FolderApi(0, 0, "root", null, List.of(), List.of(), List.of());

    @Mock
    FolderService folderService;
    @Mock
    FileService fileService;

    @InjectMocks
    DragNDropService service;

    @BeforeEach
    void setup() {
        if (root.exists()) {
            helper.deleteDirectory(root);
        }
        root.mkdirs();
    }

    @AfterAll
    static void teardown() {
        helper.deleteDirectory(root);
    }

    @Test
    void testUploadFoldersSingleFolder() throws IOException {
        var dir = helper.createDir("top");
        when(folderService.createFolder(any())).thenReturn(Single.just(new FolderApi(1, 0, "", null, List.of(), List.of(), List.of())));
        service.uploadFolders(List.of(dir), rootApi).toList().blockingGet();
        verify(folderService).createFolder(eq(new FolderRequest(Optional.empty(), 0, "top", List.of())));
        verifyNoMoreInteractions(folderService);
        verifyNoInteractions(fileService);
    }

    @Test
    void testUploadFoldersMultipleFolders() throws IOException {
        var first = helper.createDir("top");
        var second = helper.createDir("middle");
        var third = helper.createDir("bottom");
        when(folderService.createFolder(any())).thenReturn(Single.just(new FolderApi(1, 0, "", null, List.of(), List.of(), List.of())));
        service.uploadFolders(List.of(first, second, third), rootApi).toList().blockingGet();
        verify(folderService, times(3)).createFolder(argThat(arg -> arg.id().isEmpty() && arg.parentId() == 0));
        verifyNoMoreInteractions(folderService);
        verifyNoInteractions(fileService);
    }

    @Test
    void testUploadFoldersNestedFolders() throws IOException {
        var top = helper.createDir("top");
        helper.createDir("top/middle/bottom");
        when(folderService.createFolder(any())).thenReturn(Single.just(new FolderApi(1, 0, "top", null, List.of(), List.of(), List.of()))).thenReturn(Single.just(new FolderApi(2, 1, "middle", null, List.of(), List.of(), List.of()))).thenReturn(Single.just(new FolderApi(3, 2, "bottom", null, List.of(), List.of(), List.of())));
        service.uploadFolders(List.of(top), rootApi).toList().blockingGet();
        verify(folderService).createFolder(argThat(it -> it.parentId() == 0 && it.name().equals("top")));
        verify(folderService).createFolder(argThat(it -> it.parentId() == 1 && it.name().equals("middle")));
        verify(folderService).createFolder(argThat(it -> it.parentId() == 2 && it.name().equals("bottom")));
        verifyNoMoreInteractions(folderService);
        verifyNoInteractions(fileService);
    }

    @Test
    void testUploadFoldersFlatWithFiles() throws IOException {
        var dir = helper.createDir("top");
        helper.createFile("top/first.txt");
        helper.createFile("top/second.txt");
        helper.createFile("top/third.txt");
        when(folderService.createFolder(any())).thenReturn(Single.just(new FolderApi(1, 0, "", null, List.of(), List.of(), List.of())));
        when(fileService.createFile(any())).thenReturn(Single.just(new FileApi(0, "", List.of(), null, null, null, null)));
        service.uploadFolders(List.of(dir), rootApi).toList().blockingGet();
        verify(folderService).createFolder(any());
        verify(fileService).createFile(argThat(it -> it.file().getName().equals("first.txt") && it.folderId() == 1));
        verify(fileService).createFile(argThat(it -> it.file().getName().equals("second.txt") && it.folderId() == 1));
        verify(fileService).createFile(argThat(it -> it.file().getName().equals("third.txt") && it.folderId() == 1));
        verifyNoMoreInteractions(fileService);
        verifyNoMoreInteractions(folderService);
    }

    @Test
    void testUploadFoldersMultipleNestedFoldersWithFiles() throws IOException {
        /*
            top
                topFirst.txt
                middle
                    middleFirst.txt
                    middleSecond.txt
                    middleThird.txt
                    bottom
                        bottomFirst.txt
                        bottomSecond.txt
                middle2
                    middle2First.txt
                    middle2Second.txt

         */
        // top folder
        var top = helper.createDir("top");
        helper.createFile("top/topFirst.txt");
        // middle folder
        helper.createDir("top/middle");
        helper.createFile("top/middle/middleFirst.txt");
        helper.createFile("top/middle/middleSecond.txt");
        helper.createFile("top/middle/middleThird.txt");
        // second middle folder
        helper.createDir("top/middle2");
        helper.createFile("top/middle2/middle2First.txt");
        helper.createFile("top/middle2/middle2Second.txt");
        // bottom folder
        helper.createDir("top/middle/bottom");
        helper.createFile("top/middle/bottom/bottomFirst.txt");
        helper.createFile("top/middle/bottom/bottomSecond.txt");

        when(folderService.createFolder(any())).thenReturn(Single.just(new FolderApi(1, 0, "top", null, List.of(), List.of(), List.of()))).thenReturn(Single.just(new FolderApi(3, 2, "bottom", null, List.of(), List.of(), List.of())));

        when(folderService.createFolder(eq(new FolderRequest(Optional.empty(), 0, "top", List.of())))).thenReturn(Single.just(new FolderApi(1, 0, "top", null, List.of(), List.of(), List.of())));
        when(folderService.createFolder(eq(new FolderRequest(Optional.empty(), 1, "middle", List.of())))).thenReturn(Single.just(new FolderApi(2, 1, "middle", null, List.of(), List.of(), List.of())));
        when(folderService.createFolder(eq(new FolderRequest(Optional.empty(), 1, "middle2", List.of())))).thenReturn(Single.just(new FolderApi(4, 1, "middle2", null, List.of(), List.of(), List.of())));
        when(folderService.createFolder(eq(new FolderRequest(Optional.empty(), 2, "bottom", List.of())))).thenReturn(Single.just(new FolderApi(3, 2, "bottom", null, List.of(), List.of(), List.of())));

        when(fileService.createFile(any())).thenReturn(Single.just(new FileApi(0, "", List.of(), null, null, null, null)));

        service.uploadFolders(List.of(top), rootApi).toList().blockingGet();

        /// FOLDERS
        verify(folderService).createFolder(argThat(it -> it.parentId() == 0 && "top".equals(it.name())));
        verify(folderService).createFolder(argThat(it -> it.parentId() == 1 && "middle".equals(it.name())));
        verify(folderService).createFolder(argThat(it -> it.parentId() == 2 && "bottom".equals(it.name())));
        verify(folderService).createFolder(argThat(it -> it.parentId() == 1 && "middle2".equals(it.name())));
        verifyNoMoreInteractions(folderService);
        /// FILES
        // top
        verify(fileService).createFile(argThat(it -> it.folderId() == 1 && "topFirst.txt".equals(it.file().getName())));
        // middle
        verify(fileService).createFile(argThat(it -> it.folderId() == 2 && "middleFirst.txt".equals(it.file().getName())));
        verify(fileService).createFile(argThat(it -> it.folderId() == 2 && "middleSecond.txt".equals(it.file().getName())));
        verify(fileService).createFile(argThat(it -> it.folderId() == 2 && "middleThird.txt".equals(it.file().getName())));
        // middle 2
        verify(fileService).createFile(argThat(it -> it.folderId() == 4 && "middle2First.txt".equals(it.file().getName())));
        verify(fileService).createFile(argThat(it -> it.folderId() == 4 && "middle2Second.txt".equals(it.file().getName())));
        // bottom
        verify(fileService).createFile(argThat(it -> it.folderId() == 3 && "bottomFirst.txt".equals(it.file().getName())));
        verify(fileService).createFile(argThat(it -> it.folderId() == 3 && "bottomSecond.txt".equals(it.file().getName())));
        verifyNoMoreInteractions(fileService);
    }
}
