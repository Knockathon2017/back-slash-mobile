
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
    @Expose
    @SerializedName("latitude")
    private String latitude;
    @Expose
    @SerializedName("longitude")
    private String longitude;
    @Expose
    @SerializedName("postalCode")
    private String postalCode;
    @Expose
    @SerializedName("locality")
    private String locality;
    @Expose
    @SerializedName("address")
    private String address;
    public UploadTextRequest(String tags, String description, String alarm, long time,
            String category, String latitude, String longitude, String postalCode,
            String locality, String address) {
        this.tags = tags;
        this.description = description;
        this.alarm = alarm;
        this.time = time;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.postalCode = postalCode;
        this.locality = locality;
        this.address = address;
    }

}
