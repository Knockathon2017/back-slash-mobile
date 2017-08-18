package click.tagit.data.remote.grievance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 *   * User: Anurag Singh
 *   * Date: 19/8/17
 *   * Time: 4:02 AM
 */
public class FileInfoResponse {

    @Expose
    @SerializedName("message")
    private String message;
    @Expose
    @SerializedName("status")
    private short status;
    @SerializedName("data")
    @Expose
    private List<Data> data = null;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }
}
