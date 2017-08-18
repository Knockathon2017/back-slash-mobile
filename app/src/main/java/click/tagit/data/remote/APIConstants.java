package click.tagit.data.remote;

/**
 *   * User: Anurag Singh
 *   * Date: 18/8/17
 *   * Time: 20:02 PM
 *
 */
interface APIConstants {

    short READ_TIME_OUT = 1;
    short WRITE_TIME_OUT = 1;
    short CONNECTION_TIME_OUT = 3;

    String KEY_USER_AGENT_HEADER = "User-Agent";
    String VALUE_USER_AGENT_HEADER = "ClickTagit-Mobile";
    String KEY_DEVICE_ID_HEADER = "Device-Id";
    String KEY_SECURITY_TOKEN_HEADER = "S-Token";
}
