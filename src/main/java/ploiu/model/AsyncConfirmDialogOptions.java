package ploiu.model;

import javafx.stage.Window;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import ploiu.event.AsyncEventReceiver;
import ploiu.event.EventReceiver;

@Data
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public class AsyncConfirmDialogOptions {
    private final Window parentWindow = javafx.stage.Window.getWindows().stream().filter(Window::isShowing).findFirst().get();
    private final String bodyText;
    private String windowTitle = "Are you Sure?";
    private String confirmText = "Confirm";
    private String cancelText = "Cancel";

}
