import org.json.JSONException;
import org.json.JSONObject;

public class JsonRpcUtils {

    public static boolean isJsonRpcMessage(String message) {
        try {
            // Parse the message as a JSON object
            JSONObject json = new JSONObject(message);

            // Check if the "jsonrpc" field is present and has a value of "2.0"
            if (!json.has("jsonrpc") || !"2.0".equals(json.getString("jsonrpc"))) {
                return false;
            }

            // Check if the "method" field is present
            if (!json.has("method")) {
                return false;
            }

            // Check if the "params" field is present
            if (!json.has("params")) {
                return false;
            }

            // The message is a valid JSON-RPC message
            return true;
        } catch (JSONException e) {
            // The message is not a valid JSON object
            return false;
        }
    }

}
