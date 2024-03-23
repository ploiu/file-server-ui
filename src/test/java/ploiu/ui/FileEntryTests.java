package ploiu.ui;

import io.reactivex.rxjava3.core.Single;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import ploiu.event.AsyncEventReceiver;
import ploiu.event.FileSaveEvent;
import ploiu.event.FileUpdateEvent;
import ploiu.model.FileApi;
import ploiu.model.FileObject;
import ploiu.model.TagApi;

import java.util.Collection;
import java.util.Set;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.MouseButton.SECONDARY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
class FileEntryTests {

    AsyncEventReceiver<FileObject> fileReceiver;
    ObjectProperty<FileApi> editingFile = new SimpleObjectProperty<>(null);
    FileEntry fileEntry;
    Collection<TagApi> tags;

    @Start
    void start(Stage stage) {
        // can't use annotation-based mocking for ui tests, because mocks aren't initialized at this point
        fileReceiver = Mockito.mock();
        tags = Set.of(new TagApi(0L, "tag1"), new TagApi(1L, "tag2"), new TagApi(2L, "tag3"));
        var file = new FileApi(0, "name.txt", tags, 0L);
        fileEntry = new FileEntry(file, fileReceiver, editingFile);
        stage.setScene(new Scene(new AnchorPane(fileEntry)));
        stage.show();
    }

    @Test
    @DisplayName("Test that fileName displays the file's name on load")
    void testFileName(FxRobot robot) {
        FxAssert.verifyThat("#fileName", LabeledMatchers.hasText("name.txt"));
    }

    @Test
    @DisplayName("Test that right clicking on the file will show the context menu")
    void testRightClickMenu(FxRobot robot) {
        robot.clickOn(fileEntry, SECONDARY);
        FxAssert.verifyThat("#fileMenu", Node::isVisible);
    }

    @Test
    @DisplayName("Test that saveAs menu item will open the save file dialog")
    void testSaveAs(FxRobot robot) {
        robot.clickOn(fileEntry, SECONDARY);
        robot.clickOn("#saveAs");
        robot.press(ESCAPE);
    }

    @Test
    @DisplayName("Test that selecting a dialog in the file chooser will attempt to save the file in that directory")
    void testSaveAsChosenDirectory(FxRobot robot) {
        when(fileReceiver.process(any())).thenReturn(Single.never());

        robot.clickOn(fileEntry, SECONDARY);
        robot.clickOn("#saveAs");
        robot.press(ENTER);

        verify(fileReceiver).process(argThat(it -> {
            if (it instanceof FileSaveEvent evt) {
                return it.get().equals(fileEntry.getFile()) && ((FileSaveEvent) it).getDirectory() != null;
            } else {
                return false;
            }
        }));
    }

    @Test
    @DisplayName("Test that renameFile will open a text dialog")
    void testRenameFileOpensDialog(FxRobot robot) {
        robot.clickOn(fileEntry, SECONDARY);
        robot.clickOn("#renameFile");

        assertEquals(2, robot.listTargetWindows().size());
        var dialog = (TextInputDialog) robot.listTargetWindows().get(1).getScene().getRoot();
        assertEquals("Rename File", dialog.getWindowTitle());
    }

    @Test
    @DisplayName("Test that confirming file rename will attempt to update file with new name")
    void testRenameFileConfirm(FxRobot robot) {
        when(fileReceiver.process(any())).thenReturn(Single.never());

        robot.clickOn(fileEntry, SECONDARY);
        robot.clickOn("#renameFile");
        robot.clickOn("#textBox");
        // clear out the file name
        robot.type(BACK_SPACE, 10);
        robot.type(R, E, N, A, M, E, D, PERIOD, T, X, T);
        robot.clickOn("#actionButton");

        verify(fileReceiver).process(argThat(it -> {
            if (it instanceof FileUpdateEvent evt) {
                return evt.get().equals(new FileApi(0L, "renamed.txt", tags, 0L));
            } else {
                return false;
            }
        }));
    }

    @Test
    @DisplayName("Test that rejecting file rename will not attempt to update the file with the new name")
    void testRenameFileReject(FxRobot robot) {
        robot.clickOn(fileEntry, SECONDARY);
        robot.clickOn("#renameFile");
        robot.clickOn("#textBox");
        // clear out the file name
        robot.type(BACK_SPACE, 10);
        robot.type(R, E, N, A, M, E, D, PERIOD, T, X, T);

        var dialog = (Stage) robot.listTargetWindows().get(1).getScene().getWindow();
        robot.interact(dialog::close);

        verifyNoInteractions(fileReceiver);
    }

    @Test
    @DisplayName("Test that rename file rejects null and blank strings")
    void testRenameRejectsNullStrings(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("Test that failing to rename file will show an error dialogue with the error message")
    void testRenameFileFail(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("Test that clicking delete file will show a confirm modal")
    void testDeleteFileClicked(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("Test that confirming deleting a file will attempt to delete the file")
    void testDeleteFileConfirm(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("Test that cancelling deleting a file will not attempt to delete the file")
    void testDeleteFileCancel(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("Test that failing to delete the file will show an error modal")
    void testDeleteFileFail(FxRobot robot) {
        fail();
    }

    @Test
    @DisplayName("clicking the info item will attempt to open the file info panel")
    void testInfoItemClicked(FxRobot robot) {
        fail();
    }
}
