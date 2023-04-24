import burp.api.montoya.MontoyaApi;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

class JSONRPCRequest {
    private int intruderTabId;
    private String messageId;
    private JSONObject request;

    public JSONRPCRequest(int intruderTabId, JSONObject request) throws Exception {
        // TODO: This should also check params is a JsonObject
        if (!JsonRpcUtils.isJsonRpcMessage(request.toString())) {
            throw new Exception("This is a problem.");
        }

        this.intruderTabId = intruderTabId;
        this.request = request;
    }

    public JSONObject getRequest() {
        return request;
    }

    public int getIntruderTabId() {
        return intruderTabId;
    }

    public String getMethodName() {
        return request.getString("method");
    }

    public void setMethodName(String methodName) {
        this.request.put("method", methodName);
    }

    public JSONObject getParams() {
        return request.getJSONObject("params");
    }

    public void setParams(Object[] params) {
        // this.params = params;
    }

    public String getMessageId() {
        Object idObject = request.get("id");
        String id;
        if (idObject instanceof Integer) {
            id = Integer.toString((Integer) idObject);
        } else if (idObject instanceof String) {
            id = (String) idObject;
        } else {
            throw new RuntimeException("Unexpected type for 'id' field");
        }

        return id;
    }

    public void setMessageId(String id) {
        this.messageId = id;
    }
}

class MethodDetectedEvent extends EventObject {
    private JSONRPCRequest request;
    private String response;

    public MethodDetectedEvent(Object source, JSONRPCRequest request, String response) {
        super(source);
        this.request = request;
        this.response = response;
    }

    public JSONRPCRequest getRequest() {
        return request;
    }

    public String getResponse() {
        return response;
    }

    public String getMethodName() {
        return this.request.getMethodName();
    }
}

interface MethodDetectedListener extends EventListener {
    void onMethodDetected(MethodDetectedEvent event);
}

public class JSONRPCResponseMonitor {
    private MontoyaApi api;
    private ConcurrentHashMap<String, JSONRPCRequest> pendingRequests;
    private ConcurrentHashMap<Integer, List<MethodDetectedListener>> listenersByTabId;

    public JSONRPCResponseMonitor(MontoyaApi api) {
        this.api = api;
        this.pendingRequests = new ConcurrentHashMap<>();
        this.listenersByTabId = new ConcurrentHashMap<>();
    }

    public void addRequest(int intruderTabId, JSONObject jsonRequest) {
        JSONRPCRequest request = null;
        try {
            api.logging().logToOutput("lets go:");
            api.logging().logToOutput("is json: " + JsonRpcUtils.isJsonRpcMessage(jsonRequest.toString()));
            api.logging().logToOutput(jsonRequest.toString());
            request = new JSONRPCRequest(intruderTabId, jsonRequest);
            api.logging().logToOutput("we managed to create! " + request.getMethodName() + " with id: " + request.getMessageId());

            pendingRequests.put(request.getMessageId(), request);
        } catch (Exception e) {
            api.logging().logToError("Invalid JSON Request provided to JSONRPC Response Monitor.");
            api.logging().logToOutput(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void processResponse(String message) {
        api.logging().logToOutput("message recieved");
        if (!JsonRpcUtils.isValidJSONRPCResponse(message)) {
            return;
        }

        api.logging().logToOutput("valid json recieved");

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

            api.logging().logToOutput("checking if its intruder resp");
            api.logging().logToOutput("id: " + id);
            // Check if it's a response to a detection message
            if (pendingRequests.containsKey(id)) {
                api.logging().logToOutput("its a intruder resp");
                JSONRPCRequest request = pendingRequests.remove(id);

                if (JsonRpcUtils.isMethodDetected(message)) {
                    api.logging().logToOutput("METHOD DETECTED: " + request.getMethodName());
                    fireEvent(request, message);
                }
            }
        } catch (Exception ex) {
            // this.api.logging().logToOutput(ex.getMessage());
        }
    }

    public void addMethodDetectedListener(int intruderTabId, MethodDetectedListener listener) {
        listenersByTabId.computeIfAbsent(intruderTabId, k -> new ArrayList<>()).add(listener);
    }

    public void removeMethodDetectedListener(int intruderTabId, MethodDetectedListener listener) {
        List<MethodDetectedListener> listeners = listenersByTabId.get(intruderTabId);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private void fireEvent(JSONRPCRequest request, String response) {
        List<MethodDetectedListener> listeners = listenersByTabId.get(request.getIntruderTabId());
        if (listeners != null) {
            MethodDetectedEvent event = new MethodDetectedEvent(this, request, response);
            for (MethodDetectedListener listener : listeners) {
                listener.onMethodDetected(event);
            }
        }
    }
}
