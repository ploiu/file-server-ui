package ploiu.service;

import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.stage.Window;
import lombok.RequiredArgsConstructor;
import ploiu.model.*;
import ploiu.ui.LoadingModal;
import ploiu.util.FolderApproximator;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DragNDropService {
    private final FolderService folderService;
    private final FileService fileService;

    public Completable dropFiles(Collection<File> files, FolderApi targetFolder, Window currentWindow) {
        var normalFiles = files.stream().filter(File::isFile).toList();
        var directories = files.stream().filter(File::isDirectory).toList();
        var nestedSize = directories.stream().map(FolderApproximator::convertDir).map(FolderApproximation::size).reduce(Integer::sum).orElse(0);
        var percentIncrease = 1f / (normalFiles.size() + nestedSize);
        var progressAmount = new AtomicInteger(0);
        var modal = new LoadingModal(new LoadingModalOptions(currentWindow, LoadingModalOptions.LoadingType.DETERMINATE));
        var uploads = Observable.merge(uploadFiles(normalFiles, targetFolder), uploadFolders(directories, targetFolder));
        modal.open();
        return Completable.fromObservable(
                uploads
                        .doOnNext(ignored -> modal.updateProgress(progressAmount.addAndGet(1) * percentIncrease))
                        .doFinally(modal::close)
        );
    }

    Observable<FileApi> uploadFiles(Collection<File> files, FolderApi targetFolder) {
        if (files.stream().anyMatch(File::isDirectory)) {
            return Observable.error(new UnsupportedOperationException("cannot upload a directory as a normal file"));
        }
        return Observable
                .fromIterable(files)
                .subscribeOn(Schedulers.io())
                .map(f -> new CreateFileRequest(targetFolder.id(), f))
                .flatMap(req -> fileService.createFile(req).toObservable());
    }

    Observable<ServerObject> uploadFolders(Collection<File> directories, FolderApi targetFolder) {
        if (directories.stream().anyMatch(File::isFile)) {
            return Observable.error(new UnsupportedOperationException("cannot upload a normal file as a directory"));
        }
        return Observable.fromStream(directories.stream().map(FolderApproximator::convertDir))
                .flatMap(approximation -> uploadFolders(approximation, targetFolder));
    }

    private Observable<ServerObject> uploadFolders(FolderApproximation approximation, FolderApi targetFolder) {
        return uploadFolder(approximation, targetFolder)
                .toObservable()
                .flatMap(folderApi -> {
                    // to prevent from overwhelming the server, we're pseudo batching files into groups and then making a pause in between each upload group
                    var uploadedFiles = Observable.fromIterable(approximation.childFiles())
                            .buffer(30)
                            .zipWith(Observable.interval(5, TimeUnit.SECONDS), (item, interval) -> item)
                            .flatMapIterable(f -> f)
                            .flatMap(f -> uploadFile(f, folderApi).toObservable());

                    if (approximation.childFolders().isEmpty()) {
                        return Observable.merge(Observable.just(folderApi), uploadedFiles);
                    } else {
                        return Observable.merge(approximation.childFolders()
                                .stream()
                                .map(f -> uploadFolders(f, folderApi))
                                .reduce(Observable::concatWith)
                                .get(), uploadedFiles);
                    }
                });
    }

    //Observable<Observable<FileApi>> chunkUploads(Collection<File> files, FolderApi folder) {
    //    var chunkedFiles = partition(files.iterator(), 20);
    //    var windowed = new HashSet<Observable<FileApi>>();
    //    chunkedFiles.forEachRemaining(chunk -> {
    //        for (var file : chunk) {
    //            uploadFile(file, folder)
    //        }
    //    });
    //}

    Single<FileApi> uploadFile(File file, FolderApi targetFolder) {
        var req = new CreateFileRequest(targetFolder.id(), file);
        return fileService.createFile(req);
    }

    Single<FolderApi> uploadFolder(FolderApproximation approximation, FolderApi targetFolder) {
        var req = new FolderRequest(Optional.empty(), targetFolder.id(), approximation.self().getName(), targetFolder.tags());
        return folderService.createFolder(req);
    }
}
