import burp.api.montoya.MontoyaApi;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

class JSONRPCRequest {
    private String methodName;
    private Object[] params;
    private String id;

    public JSONRPCRequest(String methodName, Object[] params, String id) {
        this.methodName = methodName;
        this.params = params;
        this.id = id;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}


public class JSONRPCResponseMonitor {
    private MontoyaApi api;
    private ConcurrentHashMap<String, JSONRPCRequest> pendingRequests;

    public JSONRPCResponseMonitor(MontoyaApi api) {
        this.api = api;
        this.pendingRequests = new ConcurrentHashMap<>();
    }

    public void addRequest(String methodName, Object[] params, String id) {
        JSONRPCRequest request = new JSONRPCRequest(methodName, params, id);
        pendingRequests.put(id, request);
    }

    public void processResponse(String message) {
        if (!JsonRpcUtils.isValidJSONRPCResponse(message)) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(message);
            Object idObject = jsonObject.get("id");

            // The type of `id` could be String or Int
            String id;
            if (idObject instanceof Integer) {
                id = Integer.toString((Integer) idObject);
            } else if (idObject instanceof String) {
                id = (String) idObject;
            } else {
                throw new RuntimeException("Unexpected type for 'id' field");
            }

            // Check if its a response to a detection message
            if (pendingRequests.containsKey(id)) {
                JSONRPCRequest request = pendingRequests.remove(id);
                // Perform necessary operations with the response here
                if (JsonRpcUtils.isMethodDetected(message)) {
                    api.logging().logToOutput("METHOD DETECTED: " + request.getMethodName());
                }
            }
        } catch (Exception ex) {
            // this.api.logging().logToOutput(ex.getMessage());
        }
    }
}
