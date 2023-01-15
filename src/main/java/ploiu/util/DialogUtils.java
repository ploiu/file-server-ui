package ploiu.util;

import javafx.scene.control.Alert;
import org.jetbrains.annotations.Nullable;
import ploiu.action.Callback;

public final class DialogUtils {
    private DialogUtils() {}

    public static void showErrorDialog(String message, String title, @Nullable Callback callback) {
        var alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle(title);
        alert.showAndWait();
        if (callback != null) {
            callback.invoke();
        }
    }
}
