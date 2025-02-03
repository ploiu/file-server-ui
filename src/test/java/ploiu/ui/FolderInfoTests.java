package ploiu.ui;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import ploiu.event.AsyncEventReceiver;
import ploiu.event.FolderEvent;
import ploiu.model.FileApi;
import ploiu.model.FolderApi;
import ploiu.model.TagApi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javafx.scene.input.KeyCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static ploiu.event.FolderEvent.Type.UPDATE;

@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
class FolderInfoTests {
    AsyncEventReceiver<FolderApi> receiver;

    FolderInfo folderInfo;

    @BeforeAll
    static void beforeAll() {
        RxJavaPlugins.setErrorHandler(e -> {
            if (!(e instanceof UndeliverableException)) {
                throw e;
            }
        });
    }

    @Start
    void start(Stage stage) {
        receiver = Mockito.mock(AsyncEventReceiver.class);
        var tags = new HashSet<>(Set.of(new TagApi(0L, "tag1"), new TagApi(1L, "tag2"), new TagApi(2L, "tag3")));
        var subfolders = new HashSet<>(Set.of(new FolderApi(2, 1, "sub1", null, Set.of(), Set.of(), Set.of())));
        var subfiles = new HashSet<>(Set.of(new FileApi(0L, "file1", List.of(), null, null, null, null)));
        folderInfo = new FolderInfo(new FolderApi(1, 0, "test", null, subfolders, subfiles, tags), receiver);
        stage.setScene(new Scene(new AnchorPane(folderInfo)));
        stage.show();
    }

    @Test
    @DisplayName("Test that renameClicked shows the rename modal")
    void testRenameShowsModal(FxRobot robot) {
        robot.clickOn("#renameButton");
        assertEquals(2, robot.listTargetWindows().size());
        var dialog = (TextInputDialog) robot.listTargetWindows().get(1).getScene().getRoot();

        assertEquals("Rename Folder", dialog.getWindowTitle());
    }

    @Test
    @DisplayName("Test that filling the rename modal attempts to rename the folder")
    void testAttemptRename(FxRobot robot) {
        when(receiver.process(any())).thenReturn(Single.never());
        robot.clickOn("#renameButton");
        var dialog = (TextInputDialog) robot.listTargetWindows().get(1).getScene().getRoot();
        robot.clickOn(dialog.lookup("#textBox"));
        robot.type(R, E, N, A, M, E, D);
        robot.clickOn(dialog.lookup("#actionButton"));

        verify(receiver).process(argThat(folder ->
                ((FolderEvent) folder).getType() == UPDATE
                        && folder.get().name().equals("renamed"))
        );
    }

    @Test
    @DisplayName("Test that clicking the delete button opens a confirm modal")
    void testDeleteButtonClick(FxRobot robot) {
        robot.clickOn(folderInfo.lookup("#deleteButton"));
        assertEquals(2, robot.listTargetWindows().size());
        var dialog = (TextInputDialog) robot.listTargetWindows().get(1).getScene().getRoot();

        assertEquals("Confirm Delete?", dialog.getWindowTitle());
    }

    @Test
    @DisplayName("Test that filling out the delete form with an incorrect folder name will not attempt to delete the folder")
    void testDeleteBadConfirm(FxRobot robot) {
        robot.clickOn(folderInfo.lookup("#deleteButton"));
        var dialog = (TextInputDialog) robot.listTargetWindows().get(1).getScene().getRoot();
        robot.clickOn("#textBox");
        robot.type(B, A, D);
        robot.clickOn(dialog.lookup("#actionButton"));
        var errorDialog = (Stage) robot.listTargetWindows().get(1);
        assertEquals("Failed to Delete Folder", errorDialog.getTitle());

        verifyNoInteractions(receiver);
    }

    @Test
    @DisplayName("Test that filling out the delete form with the correct folder name will post a delete event to the receiver")
    void testDeleteGoodConfirm(FxRobot robot) {
        when(receiver.process(any())).thenReturn(Single.never());

        robot.clickOn("#deleteButton");
        var dialog = (TextInputDialog) robot.listTargetWindows().get(1).getScene().getRoot();
        robot.clickOn(dialog.lookup("#textBox"));
        robot.type(T, E, S, T);
        robot.clickOn(dialog.lookup("#actionButton"));

        verify(receiver).process(argThat(evt -> ((FolderEvent) evt).getType() == FolderEvent.Type.DELETE && evt.get().id() == 1));
    }

    @Test
    @DisplayName("update failure shows error dialog")
    void testUpdateFailure(FxRobot robot) {
        when(receiver.process(any())).thenReturn(Single.error(new RuntimeException("test")));

        robot.clickOn(folderInfo.lookup("#deleteButton"));
        var dialog = (TextInputDialog) robot.listTargetWindows().get(1).getScene().getRoot();
        robot.clickOn("#textBox");
        robot.type(T, E, S, T);
        robot.clickOn(dialog.lookup("#actionButton"));
        var errorDialog = (Stage) robot.listTargetWindows().get(1);

        assertEquals("Failed to Delete Folder", errorDialog.getTitle());
    }

    @Test
    @DisplayName("update success updates the folder info on the ui")
    void testUpdateSuccess(FxRobot robot) {
        when(receiver.process(any())).thenReturn(Single.just(true));

        robot.clickOn("#renameButton");
        var dialog = (TextInputDialog) robot.listTargetWindows().get(1).getScene().getRoot();
        robot.clickOn(dialog.lookup("#textBox"));
        robot.type(R, E, N, A, M, E, D);
        robot.clickOn(dialog.lookup("#actionButton"));

        verifyThat("#folderTitle", LabeledMatchers.hasText("renamed"));
    }

    @Test
    @DisplayName("clicking on a tag attempts to remove it")
    void testRemoveTag(FxRobot robot) {
        when(receiver.process(any())).thenReturn(Single.never());

        // remove one from middle of list so we know we aren't accidentally removing first or last instead of specific one
        robot.clickOn(LabeledMatchers.hasText("tag2"));

        verify(receiver).process(argThat(folder -> folder.get().tags().size() == 2 && !folder.get().tags().contains(new TagApi(1L, "tag2"))));
    }

    @Test
    @DisplayName("clicking on the add tag button shows a text dialog to type the tag name")
    void testAddTagShowsDialog(FxRobot robot) {
        robot.clickOn("#addTagButton");
        var dialog = (TextInputDialog) robot.listTargetWindows().get(1).getScene().getRoot();

        assertEquals("Enter Tag Title", dialog.getWindowTitle());
    }

    @Test
    @DisplayName("adding a tag updates the folder with the newly-added tag")
    void testAddTagUpdates(FxRobot robot) {
        when(receiver.process(any())).thenReturn(Single.never());

        robot.clickOn("#addTagButton");
        var dialog = (TextInputDialog) robot.listTargetWindows().get(1).getScene().getRoot();
        robot.clickOn(dialog.lookup("#textBox"));
        robot.type(T, E, S, T);
        robot.clickOn(dialog.lookup("#actionButton"));

        verify(receiver).process(argThat(event -> ((FolderEvent) event).getType() == UPDATE && event.get().tags().contains(new TagApi(null, "test"))));
    }

    @Test
    @DisplayName("Test that loading the folder shows the proper folder fields")
    void testViewShowsFolderFields(FxRobot robot) {
        /* data fields */
        // file count
        verifyThat("#fileCountLabel", Node::isVisible);
        verifyThat("#fileCountLabel", LabeledMatchers.hasText("Files: 1"));
        // folder count
        verifyThat("#folderCountLabel", Node::isVisible);
        verifyThat("#folderCountLabel", LabeledMatchers.hasText("Folders: 1"));
        // folder title
        verifyThat("#folderTitle", Node::isVisible);
        verifyThat("#folderTitle", LabeledMatchers.hasText("test"));

        /* interactive elements */
        // add tag button
        verifyThat("#addTagButton", Node::isVisible);
        // delete button
        verifyThat("#deleteButton", Node::isVisible);
        // rename button
        verifyThat("#renameButton", Node::isVisible);
    }

    @Test
    @DisplayName("Test that loading the folder shows the proper tags")
    void testViewShowsTags(FxRobot robot) {
        var tags = folderInfo.lookupAll(".tag-btn").stream().map(it -> ((Button) it).getText()).toList();

        assertTrue(tags.contains("tag1"));
        assertTrue(tags.contains("tag2"));
        assertTrue(tags.contains("tag3"));

    }
}
