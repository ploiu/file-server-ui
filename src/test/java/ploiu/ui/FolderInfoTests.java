package ploiu.ui;

import io.reactivex.rxjava3.core.Single;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
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
import ploiu.event.FileUpdateEvent;
import ploiu.event.FolderEvent;
import ploiu.model.FolderApi;

import java.util.HashSet;

import static javafx.scene.input.KeyCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ploiu.event.FolderEvent.Type.UPDATE;

@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
class FolderInfoTests {
    AsyncEventReceiver<FolderApi> receiver;

    FolderInfo folderInfo;

    @Start
    void start(Stage stage) {
        receiver = Mockito.mock(AsyncEventReceiver.class);
        folderInfo = new FolderInfo(new FolderApi(1, 0, "test", null, new HashSet<>(), new HashSet<>(), new HashSet<>()), receiver);
        stage.setScene(new Scene(new AnchorPane(folderInfo)));
        stage.show();
    }

    @Test
    @DisplayName("Test that renameClicked shows the rename modal")
    void testRenameShowsModal(FxRobot robot) {
        robot.clickOn(folderInfo.lookup("#renameButton"), MouseButton.PRIMARY);
        assertEquals(2, robot.listTargetWindows().size());
        var dialog = (TextInputDialog) robot.listTargetWindows().get(1).getScene().getRoot();
        assertEquals("Rename Folder", dialog.getWindowTitle());


    }

    @Test
    @DisplayName("Test that filling the rename modal attempts to rename the folder")
    void testAttemptRename(FxRobot robot) {
        when(receiver.process(any())).thenReturn(Single.never());
        robot.clickOn(folderInfo.lookup("#renameButton"), MouseButton.PRIMARY);
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
        fail();
    }

    @Test
    @DisplayName("Test that filling out the delete form with an incorrect folder name will not attempt to delete the folder")
    void testDeleteBadConfirm(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("Test that filling out the delete form with the correct folder name will post a delete event to the receiver")
    void testDeleteGoodConfirm(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("update failure shows error dialog")
    void testUpdateFailure(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("update success updates the folder info on the ui")
    void testUpdateSuccess(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("clicking on a tag attempts to remove it")
    void testRemoveTag(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("clicking on the add tag button shows a text dialog to type the tag name")
    void testAddTagShowsDialog(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("adding a tag updates the folder with the newly-added tag")
    void testAddTagUpdates(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("Test that loading the folder shows the proper folder fields")
    void testViewShowsFolderFields(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("Test that loading the folder shows the proper tags")
    void testViewShowsTags(FxRobot robot) {
        fail();
    }
}
