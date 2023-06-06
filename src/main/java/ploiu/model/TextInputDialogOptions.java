package ploiu.model;

import javafx.stage.Window;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import ploiu.event.EventReceiver;


@Data
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
public class TextInputDialogOptions {
    private final Window parentWindow;
    private final EventReceiver<String> confirmCallback;
    private final String confirmText;
    private String windowTitle;
    private String bodyText;
    private String initialText;

    public String windowTitle() {
        return windowTitle == null ? confirmText : windowTitle;
    }

}
