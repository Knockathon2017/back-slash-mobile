
package click.tagit.data.remote.uploadfile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 *   * User: Anurag Singh
 *   * Date: 17/6/17
 *   * Time: 8:15 PM
 */
public class UploadFileResponse {

    @SerializedName("status")
    @Expose
    private short status;

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }
}
