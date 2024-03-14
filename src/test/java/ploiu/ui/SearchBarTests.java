package ploiu.ui;

import io.reactivex.rxjava3.core.Single;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
public class SearchBarTests {

    AsyncEventReceiver<String> receiver;
    SearchBar searchBar;

    @BeforeAll
    public static void before() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("java.awt.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
    }

    @Start
    void start(Stage stage) {
        receiver = Mockito.mock(AsyncEventReceiver.class);
        searchBar = new SearchBar(receiver);
        stage.setScene(new Scene(new AnchorPane(searchBar)));
        stage.show();
    }

    @Test
    @DisplayName("Test that clicking the search button calls the receiver")
    void testClickingButton(FxRobot robot) {
        when(receiver.process(any())).thenReturn(Single.never());
        robot.clickOn(searchBar, MouseButton.PRIMARY);
        robot.type(KeyCode.T, KeyCode.E, KeyCode.S, KeyCode.T);
        robot.clickOn(LabeledMatchers.hasText("Search"), MouseButton.PRIMARY);
        verify(receiver).process(argThat(it -> "test".equals(it.get())));
    }

    @Test
    @DisplayName("Test that hitting the enter button calls the receiver")
    void testHittingEnter(FxRobot robot) {
        when(receiver.process(any())).thenReturn(Single.never());
        robot.clickOn(searchBar, MouseButton.PRIMARY);
        robot.type(KeyCode.T, KeyCode.E, KeyCode.S, KeyCode.T);
        robot.type(KeyCode.ENTER);
        verify(receiver).process(argThat(it -> "test".equals(it.get())));
    }

}
