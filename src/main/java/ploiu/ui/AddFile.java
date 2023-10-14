package ploiu.ui;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import ploiu.event.AsyncEventReceiver;
import ploiu.event.FileUploadEvent;
import ploiu.model.LoadingModalOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AddFile extends AnchorPane {
    private final AsyncEventReceiver<File> receiver;
    private final Long currentFolderId;

    public AddFile(AsyncEventReceiver<File> receiver, long currentFolderId) {
        super();
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/AddFile/AddFile.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        this.receiver = receiver;
        this.currentFolderId = currentFolderId;
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void openFileBrowser(MouseEvent event) {

        if (event.getButton() == MouseButton.PRIMARY) {
            var chooser = new FileChooser();
            chooser.setTitle("Upload Files");
            var selectedFiles = chooser.showOpenMultipleDialog(getScene().getWindow());
            uploadFiles(selectedFiles);
        }
    }

    private void uploadFiles(Collection<File> files) {
        var modal = new LoadingModal(new LoadingModalOptions(this.getScene().getWindow(), LoadingModalOptions.LoadingType.DETERMINATE));
        var modalProgress = new AtomicInteger(0);
        // percentage; no multiplying by 100 at the end because the max changes and we calc that later
        var progressAmount = 1f / files.size();
        // it looks like completable would work here, but then we wouldn't be able to get per-file progress reporting on the loading modal
        List<Observable<Boolean>> uploadList = new ArrayList<>();
        for (File selectedFile : files) {
            uploadList.add(receiver.process(new FileUploadEvent(selectedFile, currentFolderId)).toObservable());
        }
        modal.open();
        // we don't want to peg the server too much since it's designed for raspberry pi, so we should execute the requests sequentially
        uploadList.stream().reduce(Observable::concatWith)
                .get()
                // this is important because the observer subscribes on whatever thread it was created on...I might want to move this somewhere else
                .doFinally(modal::close)
                .subscribeOn(Schedulers.io())
                .subscribe(ignored -> modal.updateProgress(modalProgress.addAndGet(1) * progressAmount));
    }
}
