package ploiu.client;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import ploiu.model.FileApi;
import retrofit2.http.*;

import java.util.Collection;

public interface FileClient {

    @GET("/files/preview/{id}")
    Observable<ResponseBody> getFilePreview(@Path("id") long id);

    @GET("/files/metadata")
    Single<Collection<FileApi>> search(@Query("search") String search, @Query("tags") Collection<String> tags);

    @Multipart
    @POST("/files")
    Single<FileApi> createFile(@Part MultipartBody.Part file, @Part MultipartBody.Part extension, @Part MultipartBody.Part folderId);
}
