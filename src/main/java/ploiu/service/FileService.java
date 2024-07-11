package ploiu.service;

import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.scene.image.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import ploiu.client.FileClient;
import ploiu.client.RetrofitFileClient;
import ploiu.model.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ploiu.Constants.CACHE_DIR;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FileService {
    private final FileClient fileClient;
    private final RetrofitFileClient retrofitFileClient;

    public Single<File> saveAndGetFile(FileApi fileApi, @Nullable File directory) {
        return Single.fromCallable(() -> {
            if (directory != null) {
                //noinspection ResultOfMethodCallIgnored
                directory.mkdirs();
                return new File(directory.getAbsolutePath() + "/" + fileApi.name());
            }
            //noinspection ResultOfMethodCallIgnored
            new File(CACHE_DIR).mkdirs();
            // file name differs here because the cache dir could have a ton of files with the same name if we don't include the file id
            return new File(CACHE_DIR + "/" + fileApi.id() + "_" + fileApi.name());
        }).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).flatMap(fsFile -> {
            return fileClient.getFileContents(fileApi.id()).observeOn(Schedulers.io()).map(contents -> {
                //noinspection ResultOfMethodCallIgnored
                fsFile.createNewFile();
                Files.copy(contents, fsFile.toPath(), REPLACE_EXISTING);
                return fsFile;
            });
        });

    }

    public Single<Collection<FileApi>> search(String query) {
        return Single.just(query).map(FileSearch::fromInput).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).flatMap(fileClient::search);
    }

    public Single<FileApi> getMetadata(long id) {
        return Single.just(id).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).flatMap(fileClient::getMetadata);
    }

    public Completable deleteFile(long id) {
        return Single.just(id).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).flatMapCompletable(fileClient::deleteFile);
    }

    public Single<FileApi> updateFile(UpdateFileRequest request) {
        return Single.just(request).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).flatMap(fileClient::updateFile);
    }

    public Single<FileApi> createFile(CreateFileRequest request) {
        return Single.just(request).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).flatMap(fileClient::createFile);
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
        return Observable.just(folder)
                .observeOn(Schedulers.io())
                .map(FolderApi::id)
                .flatMapSingle(retrofitFileClient::getPreviewsForFolder)
                .map(map -> {
                    Map<Long, Image> images = new HashMap<>();
                    for (var entry : map.entrySet()) {
                        var image = new Image(new ByteArrayInputStream(entry.getValue()), 100, 100, true, true);
                        images.put(entry.getKey(), image);
                    }
                    return images;
                })
                .single(Map.of());
    }

}
