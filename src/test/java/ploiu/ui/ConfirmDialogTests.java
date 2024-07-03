package ploiu.ui;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import ploiu.event.EventReceiver;
import ploiu.model.ConfirmDialogOptions;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class ConfirmDialogTests {
    Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        // we need this for monocle to give the stage an owner, which we use to pass to the dialog for the window ref
        stage.setScene(new Scene(new AnchorPane()));
        stage.show();
    }

    @Test
    @DisplayName("Test that windowTitle matches the options")
    void testWindowTitle(FxRobot robot) {
        var options = new ConfirmDialogOptions(stage.getOwner(), ignored -> true, "").windowTitle("test title");
        robot.interact(() -> new ConfirmDialog(options));
        assertEquals("test title", ((Stage) robot.listTargetWindows().get(1)).getTitle());

    }

    @Test
    @DisplayName("Test that bodyText is properly set")
    void testBodyText(FxRobot robot) {
        var options = new ConfirmDialogOptions(stage.getOwner(), ignored -> true, "test body");
        robot.interact(() -> new ConfirmDialog(options));

        FxAssert.verifyThat(".body-text", LabeledMatchers.hasText("test body"));
    }

    @Test
    @DisplayName("Test that confirmText matches the options")
    void testConfirmText(FxRobot robot) {
        var options = new ConfirmDialogOptions(stage.getOwner(), ignored -> true, "").confirmText("test confirm");
        robot.interact(() -> new ConfirmDialog(options));

        FxAssert.verifyThat("#confirmButton", LabeledMatchers.hasText("test confirm"));
    }

    @Test
    @DisplayName("Test that cancelText matches the options")
    void testCancelText(FxRobot robot) {
        var options = new ConfirmDialogOptions(stage.getOwner(), ignored -> true, "").cancelText("test cancel");
        robot.interact(() -> new ConfirmDialog(options));

        FxAssert.verifyThat("#cancelButton", LabeledMatchers.hasText("test cancel"));
    }

    @Test
    @DisplayName("Test that resultAction properly calls confirm")
    void testResultActionConfirm(FxRobot robot) {
        EventReceiver<Boolean> callback = it -> {
            assertTrue(it.get());
            return true;
        };
        var options = new ConfirmDialogOptions(stage.getOwner(), callback, "");
        robot.interact(() -> {
            new ConfirmDialog(options);
            robot.clickOn("#confirmButton");
        });
    }

    @Test
    @DisplayName("Test that resultAction properly calls cancel")
    void testResultActionCancel(FxRobot robot) {
        EventReceiver<Boolean> callback = it -> {
            assertFalse(it.get());
            return true;
        };
        var options = new ConfirmDialogOptions(stage.getOwner(), callback, "");
        robot.interact(() -> {
            new ConfirmDialog(options);
            robot.clickOn("#cancelButton");
        });
    }
}
