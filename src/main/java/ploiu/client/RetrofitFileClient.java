package ploiu.client;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.Map;

public interface RetrofitFileClient {
    @GET("/folders/preview/{id}")
    Single<Map<Long, byte[]>> getPreviewsForFolder(@Path("id") long id);
}
