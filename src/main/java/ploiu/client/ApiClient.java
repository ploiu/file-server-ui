package ploiu.client;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import ploiu.model.ApiInfo;
import ploiu.model.CreatePasswordRequest;
import ploiu.model.StorageInfo;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiClient {

    @GET("/api/version")
    Single<ApiInfo> getApiInfo();

    @POST("/password")
    Completable setPassword(@Body CreatePasswordRequest req);

    @GET("/api/disk")
    Single<StorageInfo> getStorageInfo();
}
