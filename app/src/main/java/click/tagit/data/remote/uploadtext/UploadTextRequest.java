
package click.tagit.data.remote.uploadtext;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UploadTextRequest {

    @Expose
    @SerializedName("tags")
    private String tags;
    @Expose
    @SerializedName("description")
    private String description;
    @Expose
    @SerializedName("alarm")
    private String alarm;
    @Expose
    @SerializedName("time")
    private long time;
    @Expose
    @SerializedName("category")
    private String category;
    public UploadTextRequest(String tags, String description, String alarm, long time,
            String category) {
        this.tags = tags;
        this.description = description;
        this.alarm = alarm;
        this.time = time;
        this.category = category;
    }
}
