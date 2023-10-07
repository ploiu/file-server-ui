package ploiu.model;

import javafx.stage.Window;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LoadingModalOptions {
    private final Window parentWindow;
    private final LoadingType type;

    public enum LoadingType {
        DETERMINATE,
        INDETERMINATE
    }
}
