package ploiu.service;

import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.scene.image.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ploiu.client.FolderClient;
import ploiu.exception.BadFolderRequestException;
import ploiu.exception.BadFolderResponseException;
import ploiu.model.FolderApi;
import ploiu.model.FolderRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ploiu.Constants.LIST_IMAGE_SIZE;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FolderService {
    private final FolderClient client;


    public Single<FolderApi> getFolder(long id) {
        return Single.just(id)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(i ->
                        i < 0
                                ? Single.error(new BadFolderRequestException("Folder id must be 0 or greater."))
                                : client.getFolder(i)
                );

    }

    public Single<FolderApi> createFolder(FolderRequest request) throws BadFolderRequestException, BadFolderResponseException {
        return Single.just(request)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(client::createFolder);
    }

    public Single<FolderApi> updateFolder(FolderRequest folder) throws BadFolderRequestException, BadFolderResponseException {
        return Single.just(folder)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(f -> {
                    if (f.id().isEmpty()) {
                        return Single.error(new BadFolderRequestException("Cannot update folder without id"));
                    }
                    if (f.id().get() == 0) {
                        return Single.error(new BadFolderRequestException("0 is the root folder id, and cannot be updated"));
                    }
                    return client.updateFolder(f);
                });
    }

    public Completable deleteFolder(long id) throws BadFolderRequestException, BadFolderResponseException {
        return Single.just(id)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(i ->
                        i < 1
                                ? Completable.error(new BadFolderRequestException("id must be greater than 0"))
                                : client.deleteFolder(i)
                );
    }

    /**
     * returns files icon images for each file in the passed folder.
     * <p>
     * this method will attempt to retrieve cached icons first, and it pairs them with the corresponding file id
     * (to prevent re-uploaded files with the same name but different contents from having the same preview).
     *
     * @param folder
     * @return
     */
    public Single<Map<Long, Image>> getFilePreviewsForFolder(FolderApi folder) {
        return Observable.just(folder).observeOn(Schedulers.io()).map(FolderApi::id).flatMapSingle(client::getPreviewsForFolder).map(map -> {
            Map<Long, Image> images = new HashMap<>();
            for (var entry : map.entrySet()) {
                var image = new Image(new ByteArrayInputStream(entry.getValue()), LIST_IMAGE_SIZE, LIST_IMAGE_SIZE, true, true);
                images.put(entry.getKey(), image);
            }
            return images;
        }).single(Map.of());
    }

    public Completable downloadFolder(FolderApi folder, @NotNull File directory) {
        if (folder.id() < 1) {
            return Completable.error(new BadFolderRequestException("Id cannot be negative, and cannot download root folder."));
        }
        return Single.fromCallable(() -> {
                    //noinspection ResultOfMethodCallIgnored
                    directory.mkdirs();
                    return new File(directory.getAbsolutePath() + "/" + folder.name() + ".tar");
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(f -> {
                    return client
                            .downloadFolder(folder.id())
                            .subscribeOn(Schedulers.io())
                            .map(resBody -> {
                                // doesn't matter if the file already exists or not (because the user would confirm if they want to overwrite it earlier), we're saving to it
                                //noinspection ResultOfMethodCallIgnored
                                f.createNewFile();
                                Files.copy(resBody.byteStream(), f.toPath(), REPLACE_EXISTING);
                                return f;
                            })
                            .flatMapCompletable(ignored -> Completable.complete());
                });
    }
}
