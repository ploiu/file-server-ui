package ploiu.service;

import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import javafx.stage.Window;
import lombok.RequiredArgsConstructor;
import ploiu.client.FolderClient;
import ploiu.model.CreateFileRequest;
import ploiu.model.FileApi;
import ploiu.model.FolderApi;
import ploiu.model.LoadingModalOptions;
import ploiu.ui.LoadingModal;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DragNDropService {
    private final FolderClient folderClient;
    private final FileService fileService;

    public Completable dropFiles(Collection<File> files, FolderApi targetFolder, Window currentWindow) {
        var normalFiles = files.stream().filter(File::isFile).toList();
        var directories = files.stream().filter(File::isDirectory).toList();
        // TODO account for # of folders and files recursively in directories
        var percentIncrease = 1f / normalFiles.size();
        var progressAmount = new AtomicInteger(0);
        var modal = new LoadingModal(new LoadingModalOptions(currentWindow, LoadingModalOptions.LoadingType.DETERMINATE));
        modal.open();
        return Completable.fromObservable(uploadFiles(normalFiles, targetFolder)
                .doOnNext(ignored -> modal.updateProgress(progressAmount.addAndGet(1) * percentIncrease))
                .doFinally(modal::close));
    }

    Observable<FileApi> uploadFiles(Collection<File> files, FolderApi targetFolder) {
        if (files.stream().anyMatch(File::isDirectory)) {
            return Observable.error(new UnsupportedOperationException("cannot upload a directory as a normal file"));
        }
        return Observable
                .fromIterable(files)
                .map(f -> new CreateFileRequest(targetFolder.id(), f))
                .flatMap(req -> fileService.createFile(req).toObservable());
    }

    Observable<FolderApi> uploadFolders(Collection<File> directories, FolderApi targetFolder) {
        if (directories.stream().anyMatch(File::isFile)) {
            return Observable.error(new UnsupportedOperationException("cannot upload a normal file as a directory"));
        }
        return Observable.error(new UnsupportedOperationException("Not finished upload folders"));
    }

}
