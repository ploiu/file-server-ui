package ploiu.client;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import ploiu.model.TagApi;
import retrofit2.http.*;

public interface TagClient {

    @GET("/tags/{id}")
    Single<TagApi> getTag(@Path("id") int id);

    @PUT("/tags")
    Single<TagApi> updateTag(@Body TagApi tag);

    @DELETE("/tags/{id}")
    Completable deleteTag(@Path("id") int id);

    @POST("/tags")
    Single<TagApi> createTag(@Body TagApi tag);
}
