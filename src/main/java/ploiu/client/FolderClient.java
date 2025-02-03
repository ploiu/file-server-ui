package ploiu.client;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import ploiu.model.FolderApi;
import ploiu.model.FolderRequest;
import retrofit2.http.*;

import java.util.Map;

public interface FolderClient {
    @GET("/folders/metadata/{id}")
    Single<FolderApi> getFolder(@Path("id") long id);

    @GET("/folders/preview/{id}")
    Single<Map<Long, byte[]>> getPreviewsForFolder(@Path("id") long id);

    @POST("/folders")
    Single<FolderApi> createFolder(@Body FolderRequest req);

    @PUT("/folders")
    Single<FolderApi> updateFolder(@Body FolderRequest req);

    @DELETE("/folders/{id}")
    Completable deleteFolder(@Path("id") long id);
}
