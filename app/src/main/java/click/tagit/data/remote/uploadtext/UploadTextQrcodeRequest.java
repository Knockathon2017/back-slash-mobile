
package click.tagit.data.remote.uploadtext;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UploadTextQrcodeRequest {

    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("metadata")
    private String metadata;
    public UploadTextQrcodeRequest(String type, String metadata) {
        this.type = type;
        this.metadata = metadata;
    }
}
