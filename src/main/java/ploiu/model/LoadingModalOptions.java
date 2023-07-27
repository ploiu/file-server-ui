package ploiu.model;

import javafx.stage.Window;

public record LoadingModalOptions(
        String bodyText,
        Window parentWindow
) {
}
