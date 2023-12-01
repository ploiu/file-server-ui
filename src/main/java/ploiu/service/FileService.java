package ploiu.service;

import com.google.inject.Inject;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import ploiu.client.FileClient;
import ploiu.client.TagClient;
import ploiu.model.CreateFileRequest;
import ploiu.model.FileApi;
import ploiu.model.UpdateFileRequest;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ploiu.Constants.CACHE_DIR;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class FileService {
    private final FileClient fileClient;

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
                })
                .flatMap(fsFile -> {
                    return fileClient.getFileContents(fileApi.id())
                            .observeOn(Schedulers.io())
                            .map(contents -> {
                                //noinspection ResultOfMethodCallIgnored
                                fsFile.createNewFile();
                                Files.copy(contents, fsFile.toPath(), REPLACE_EXISTING);
                                return fsFile;
                            });
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io());

    }

    public Single<Collection<FileApi>> search(String query) {
        return fileClient.search(query);
    }

    public Single<FileApi> getMetadata(long id) {
        return fileClient.getMetadata(id);
    }

    public Completable deleteFile(long id) {
        return fileClient.deleteFile(id);
    }

    public Single<FileApi> updateFile(UpdateFileRequest request) {
        return fileClient.updateFile(request);
    }

    public Single<FileApi> createFile(CreateFileRequest request) {
        return fileClient.createFile(request);
    }

}
