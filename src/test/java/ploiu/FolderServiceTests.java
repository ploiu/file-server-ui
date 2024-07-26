package ploiu;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ploiu.client.FolderClient;
import ploiu.exception.BadFolderRequestException;
import ploiu.exception.BadFolderResponseException;
import ploiu.model.FolderApi;
import ploiu.model.FolderRequest;
import ploiu.service.FolderService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderServiceTests {
    @Mock
    FolderClient client;

    @InjectMocks
    FolderService folderService;

    @Test
    void testGetFolderWithIdUsesCorrectPath() {
        when(client.getFolder(anyLong())).thenReturn(Single.just(new FolderApi(0, 0, "", "", List.of(), List.of(), List.of())));
        folderService.getFolder(1L).blockingSubscribe();
        verify(client).getFolder(eq(1L));
    }

    @Test
    void testCreateFolderUsesPost() {
        when(client.createFolder(any())).thenReturn(Single.just(new FolderApi(0, 0, "", "", List.of(), List.of(), List.of())));
        var req = new FolderRequest(Optional.empty(), 0, "test", List.of());
        folderService.createFolder(req).blockingSubscribe();
        verify(client).createFolder(argThat(req::equals));
    }

    @Test
    void testUpdateFolderRequiresFolderId() {
        var request = new FolderRequest(Optional.empty(), 0, "test", List.of());
        try {
            folderService.updateFolder(request).blockingGet();
        } catch (BadFolderRequestException | BadFolderResponseException e) {
            assertEquals("Cannot update folder without id", e.getMessage());
        }
    }

    @Test
    void testUpdateFolderRequiresNonZeroId() {
        var request = new FolderRequest(Optional.of(0L), 0, "test", List.of());
        try {
            folderService.updateFolder(request).blockingGet();
        } catch (BadFolderRequestException | BadFolderResponseException e) {
            assertEquals("0 is the root folder id, and cannot be updated", e.getMessage());
        }
    }

    @Test
    void testUpdateFolderUsesPut() {
        when(client.updateFolder(any())).thenReturn(Single.just(new FolderApi(0, 0, "", "", List.of(), List.of(), List.of())));
        var req = new FolderRequest(Optional.of(1L), 0, "test", List.of());
        folderService.updateFolder(req).blockingSubscribe();
        verify(client).updateFolder(argThat(req::equals));
    }

    @Test
    void testDeleteFolderRequiresPositiveId() {
        try {
            folderService.deleteFolder(0L).blockingAwait();
        } catch (BadFolderRequestException e) {
            assertEquals("id must be greater than 0", e.getMessage());
        }
    }

    @Test
    void testDeleteFolderUsesDelete() {
        when(client.deleteFolder(anyLong())).thenReturn(Completable.complete());
        folderService.deleteFolder(1L).blockingSubscribe();
        verify(client).deleteFolder(eq(1L));
    }
}
