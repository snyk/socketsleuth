import org.json.JSONException;
import org.json.JSONObject;

public class JsonRpcUtils {

    public static boolean isValidJSONRPCResponse(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            if (!jsonObject.has("jsonrpc") || !jsonObject.has("id")) {
                return false;
            }

            String jsonrpc = jsonObject.getString("jsonrpc");
            if (!"2.0".equals(jsonrpc)) {
                return false;
            }

            if (!jsonObject.has("result") && !jsonObject.has("error")) {
                return false;
            }

            if (jsonObject.has("result") && jsonObject.has("error")) {
                return false;
            }

            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public static boolean isMethodDetected(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            if (jsonObject.has("result")) {
                return true;
            }

            // TODO: Move the detection errorCodes and messages to UI so its can be configured
            if (jsonObject.has("error")) {
                JSONObject errorObject = jsonObject.getJSONObject("error");
                int errorCode = errorObject.getInt("code");
                String errorMessage = errorObject.getString("message").toLowerCase();

                if (errorCode == -32602 ||
                        errorMessage.contains("invalid param") ||
                        errorMessage.contains("incorrect param") ||
                        errorMessage.contains("bad param") ||
                        errorMessage.contains("wrong param") ||
                        errorMessage.contains("missing param")) {
                    return true;
                }

                if (errorCode == 400 || errorCode == 404 ||
                        errorCode == -32601 ||
                        errorMessage.contains("method not found") ||
                        errorMessage.contains("unknown method") ||
                        errorMessage.contains("invalid method") ||
                        errorMessage.contains("unsupported method")) {
                    return false;
                }

                return true;
            }

        } catch (JSONException e) {
            // Ignore the exception, as it's not a valid JSON-RPC response
        }

        return false;
    }

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
