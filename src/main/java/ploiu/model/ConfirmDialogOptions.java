package ploiu.model;

import javafx.stage.Window;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import ploiu.event.EventReceiver;

@Data
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public class ConfirmDialogOptions {
    private final Window parentWindow;
    private final EventReceiver<Boolean> resultAction;
    private final String bodyText;
    private String windowTitle = "Are you Sure?";
    private String confirmText = "Confirm";
    private String cancelText = "Cancel";

}
