package ploiu.client;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import ploiu.model.FileApi;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.Collection;

public interface FileClient {

    @GET("/files/preview/{id}")
    Observable<ResponseBody> getFilePreview(@Path("id") long id);

    @GET("/files/metadata")
    Single<Collection<FileApi>> search(@Query("search") String search, @Query("tags") Collection<String> tags);
}
