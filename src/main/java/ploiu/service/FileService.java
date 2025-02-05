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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;
import ploiu.client.FileClient;
import ploiu.exception.BadFileRequestException;
import ploiu.exception.BadFileResponseException;
import ploiu.model.CreateFileRequest;
import ploiu.model.FileApi;
import ploiu.model.UpdateFileRequest;
import ploiu.search.SearchParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ploiu.Constants.CACHE_DIR;
import static ploiu.Constants.LIST_IMAGE_SIZE;

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FileService {
    private final FileClient client;

    /**
     * saves the contents of the associated {@code fileApi to the disk and then returns its contents.
     * If the file is already on the disk, no call to the file server is made.
     *
     * @param fileApi
     * @param directory
     * @return
     */
    public Single<File> getFileContents(FileApi fileApi, @Nullable File directory) {
        if (fileApi.id() < 0) {
            return Single.error(new BadFileRequestException("Id cannot be negative."));
        }
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
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(fsFile -> {
                    return client.getFileContents(fileApi.id())
                            .observeOn(Schedulers.io())
                            .map(res -> {
                                //noinspection ResultOfMethodCallIgnored
                                fsFile.createNewFile();
                                Files.copy(res.byteStream(), fsFile.toPath(), REPLACE_EXISTING);
                                return fsFile;
                            });
                });

    }

    public Single<Collection<FileApi>> search(String input) {
        var parsed = SearchParser.parse(input);
        return client.search(parsed.text(), parsed.tags(), parsed.attributes())
                .subscribeOn(Schedulers.io());
    }

    public Single<FileApi> getMetadata(long id) {
        if (id < 0) {
            return Single.error(new BadFileRequestException("Id cannot be negative."));
        }
        return Single.just(id)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMapMaybe(client::getMetadata)
                .switchIfEmpty(Single.error(new BadFileResponseException("The file with the passed id could not be found.")));
    }

    public Completable deleteFile(long id) {
        if (id < 0) {
            return Completable.error(new BadFileRequestException("Id cannot be negative."));
        }
        return Single.just(id).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).flatMapCompletable(client::deleteFile);
    }

    public Single<FileApi> updateFile(UpdateFileRequest request) {
        if (request.id() < 0) {
            return Single.error(new BadFileRequestException("Id cannot be negative."));
        }
        if (request.name().isBlank()) {
            return Single.error(new BadFileRequestException("Name cannot be blank."));
        }
        return Single.just(request).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).flatMap(client::updateFile);
    }

    public Single<FileApi> createFile(CreateFileRequest request) {
        var file = request.file();
        Objects.requireNonNull(file, "File cannot be null.");
        if (!file.exists()) {
            return Single.error(new BadFileRequestException("The selected file does not exist."));
        }
        var splitName = file.getName().split("\\.");
        var mimeType = URLConnection.guessContentTypeFromName(file.getName());
        mimeType = mimeType == null ? "text/plain" : mimeType;
        var extension = splitName.length > 1 ? splitName[splitName.length - 1] : null;
        var force = request.force();
        var fileName = file.getName().replace("(", "leftParenthese").replace(")", "rightParenthese");
        var filePart = MultipartBody.Part.createFormData("file", fileName, RequestBody.create(file, MediaType.parse(mimeType)));
        var folderPart = MultipartBody.Part.createFormData("folderId", String.valueOf(request.folderId()));
        return client.createFile(filePart, extension != null ? MultipartBody.Part.createFormData("extension", extension) : null, folderPart)
                .subscribeOn(Schedulers.io());
    }

    public Maybe<Image> getFilePreview(Long id) {
        return Observable.just(id)
                .observeOn(Schedulers.io())
                .flatMap(client::getFilePreview)
                .onErrorComplete()
                .map(ResponseBody::bytes)
                .map(data -> new Image(new ByteArrayInputStream(data), LIST_IMAGE_SIZE, LIST_IMAGE_SIZE, true, true))
                .singleElement();
    }

}
