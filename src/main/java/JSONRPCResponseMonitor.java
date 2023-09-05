/*
 * © 2023 Snyk Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.Direction;
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
            throw new Exception("Invalid JSONRPC message");
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

class MessageEvent extends EventObject {
    private String message;
    private Direction direction;

    public MessageEvent(Object source, String message, Direction direction) {
        super(source);
        this.message = message;
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getMessage() {
        return message;
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

interface MessageSentListener extends EventListener {
    void onMessageSent(MessageEvent event);
}

interface ResponseReceivedListener extends EventListener {
    void onResponseReceived(MessageEvent event);
}

interface MethodDetectedListener extends EventListener {
    void onMethodDetected(MethodDetectedEvent event);
}

public class JSONRPCResponseMonitor {
    private MontoyaApi api;
    private ConcurrentHashMap<String, JSONRPCRequest> pendingRequests;
    private ConcurrentHashMap<Integer, List<MethodDetectedListener>> methodDetectedListenersByTabId;
    private ConcurrentHashMap<Integer, List<ResponseReceivedListener>> responseReceivedListenersByTabId;
    private ConcurrentHashMap<Integer, List<MessageSentListener>> onMessageSentListenersByTabId;

    public JSONRPCResponseMonitor(MontoyaApi api) {
        this.api = api;
        this.pendingRequests = new ConcurrentHashMap<>();
        this.methodDetectedListenersByTabId = new ConcurrentHashMap<>();
        this.responseReceivedListenersByTabId = new ConcurrentHashMap<>();
        this.onMessageSentListenersByTabId = new ConcurrentHashMap<>();
    }

    public void addRequest(int intruderTabId, JSONObject jsonRequest) {
        JSONRPCRequest request = null;
        try {
            request = new JSONRPCRequest(intruderTabId, jsonRequest);
            pendingRequests.put(request.getMessageId(), request);

            // Note: this is a bit hacky - we hijack the responseReceivedEvent even though this is the message
            // this is okay for now as the only listener is used to add sent / received messages to the table
            // TODO: make the event listeners explicit incase different actions are needed
            // TODO: ResponseMonitor will be refactored to use the SocketProvider so this will all change
            fireResponseReceivedEvent(request.getIntruderTabId(), request.getRequest().toString(), Direction.CLIENT_TO_SERVER);
        } catch (Exception e) {
            api.logging().logToError("Invalid JSON Request provided to JSONRPC Response Monitor.");
            throw new RuntimeException(e);
        }
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

            // Check if it's a response to a detection message
            if (pendingRequests.containsKey(id)) {
                JSONRPCRequest request = pendingRequests.remove(id);
                fireResponseReceivedEvent(request.getIntruderTabId(), message, Direction.SERVER_TO_CLIENT);

                if (JsonRpcUtils.isMethodDetected(message)) {
                    api.logging().logToOutput("METHOD DETECTED: " + request.getMethodName());
                    fireMethodDetectedEvent(request, message);
                }
            }
        } catch (Exception ex) {
            // this.api.logging().logToOutput(ex.getMessage());
        }
    }

    public void addMethodDetectedListener(int intruderTabId, MethodDetectedListener listener) {
        methodDetectedListenersByTabId.computeIfAbsent(intruderTabId, k -> new ArrayList<>()).add(listener);
    }

    public void addResponseReceivedListener(int intruderTabId, ResponseReceivedListener listener) {
        responseReceivedListenersByTabId.computeIfAbsent(intruderTabId, k -> new ArrayList<>()).add(listener);
    }

    public void removeMethodDetectedListener(int intruderTabId, MethodDetectedListener listener) {
        List<MethodDetectedListener> listeners = methodDetectedListenersByTabId.get(intruderTabId);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private void fireMethodDetectedEvent(JSONRPCRequest request, String response) {
        List<MethodDetectedListener> listeners = methodDetectedListenersByTabId.get(request.getIntruderTabId());
        if (listeners != null) {
            MethodDetectedEvent event = new MethodDetectedEvent(this, request, response);
            for (MethodDetectedListener listener : listeners) {
                listener.onMethodDetected(event);
            }
        }
    }

    private void fireResponseReceivedEvent(int tabId, String response, Direction direction) {
        List<ResponseReceivedListener> listeners = responseReceivedListenersByTabId.get(tabId);
        if (listeners != null) {
            MessageEvent event = new MessageEvent(this, response, direction);
            for (ResponseReceivedListener listener : listeners) {
                listener.onResponseReceived(event);
            }
        }
    }
}
