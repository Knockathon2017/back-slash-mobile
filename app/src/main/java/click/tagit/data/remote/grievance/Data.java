package click.tagit.data.remote.grievance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 *   * User: Anurag Singh
 *   * Date: 19/8/17
 *   * Time: 4:07 AM
 *
 */
class Data {

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
    @SerializedName("_id")
    private String _id;
    @Expose
    @SerializedName("fileName")
    private String fileName;

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
