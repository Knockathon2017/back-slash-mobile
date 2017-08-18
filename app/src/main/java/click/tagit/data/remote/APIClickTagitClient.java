package click.tagit.data.remote;

import click.tagit.data.remote.uploadfile.UploadFileResponse;
import click.tagit.data.remote.uploadtext.UploadTextRequest;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 *   * User: Anurag Singh
 *   * Date: 18/8/17
 *   * Time: 19:30 PM
 */
public interface APIClickTagitClient {

    @Multipart
    @POST("uploadFile")
    Observable<UploadFileResponse> multipartUploadFile(
            @Part("tags") RequestBody tags
            , @Part("description") RequestBody description
            , @Part("alarm") RequestBody alarm
            , @Part("time") RequestBody time
            , @Part("category") RequestBody category
            , @Part MultipartBody.Part file);

    @POST("gText")
    Observable<UploadFileResponse> postGText(@Body UploadTextRequest uploadTextRequest);
}
