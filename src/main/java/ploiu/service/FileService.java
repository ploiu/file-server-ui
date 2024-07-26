package ploiu.service;

import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.scene.image.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;
import ploiu.client.DeprecatedFileClient;
import ploiu.client.FileClient;
import ploiu.model.CreateFileRequest;
import ploiu.model.FileApi;
import ploiu.model.FileSearch;
import ploiu.model.UpdateFileRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Collection;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ploiu.Constants.CACHE_DIR;
import static ploiu.Constants.LIST_IMAGE_SIZE;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FileService {
    private final DeprecatedFileClient fileClient;
    private final FileClient retrofitFileClient;

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
        return Single.just(query).map(FileSearch::fromInput).observeOn(Schedulers.io()).subscribeOn(Schedulers.io())
                .flatMap(search -> retrofitFileClient.search(search.query(), search.tags()));
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

    public Maybe<Image> getFilePreview(Long id) {
        return Observable.just(id)
                .observeOn(Schedulers.io())
                .flatMap(retrofitFileClient::getFilePreview)
                .onErrorComplete()
                .map(ResponseBody::bytes)
                .map(data -> new Image(new ByteArrayInputStream(data), LIST_IMAGE_SIZE, LIST_IMAGE_SIZE, true, true))
                .singleElement();
    }

}
