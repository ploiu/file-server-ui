package ploiu.ui;

import io.reactivex.rxjava3.core.Single;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
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
import ploiu.ReflectionTestUtils;
import ploiu.event.AsyncEventReceiver;

import static javafx.scene.input.KeyCode.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith({ApplicationExtension.class, MockitoExtension.class})
public class SearchBarTests {

    AsyncEventReceiver<String> receiver;
    SearchBar searchBar;

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
        robot.clickOn(searchBar);
        robot.type(T, E, S, T);
        robot.clickOn(LabeledMatchers.hasText("Search"));
        verify(receiver).process(argThat(it -> "test".equals(it.get())));
    }

    @Test
    @DisplayName("Test that hitting the enter button calls the receiver")
    void testHittingEnter(FxRobot robot) {
        when(receiver.process(any())).thenReturn(Single.never());
        robot.clickOn(searchBar, MouseButton.PRIMARY);
        robot.type(T, E, S, T);
        robot.type(ENTER);
        verify(receiver).process(argThat(it -> "test".equals(it.get())));
    }

    @Test
    @DisplayName("Test that the focus method focuses the search box")
    void testFocus(FxRobot robot) {
        var textField = mock(TextField.class);
        ReflectionTestUtils.setField(searchBar, "searchField", textField);
        searchBar.focus();
        verify(textField).requestFocus();
    }

}
