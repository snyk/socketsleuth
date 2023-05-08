import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;

import java.util.HashMap;
import java.util.Map;

public class WebSocketAutoRepeater {

    MontoyaApi api;
    Map<Integer, AutoRepeaterConfig> repeaters;

    Map<Integer, WebSocketContainer> wsConnections;

    public WebSocketAutoRepeater(MontoyaApi api, Map<Integer, WebSocketContainer> wsConnections) {
        this.api = api;
        this.repeaters = new HashMap<>();
        this.wsConnections = wsConnections;
    }

    public void setRepeater(int tabId, AutoRepeaterConfig config) {
        api.logging().logToOutput("AR repeater added in tab: " + tabId);
        api.logging().logToOutput("config - sourceID: " + config.getSourceSocketId() + " dst ID: " + config.getTargetSocketId());
        repeaters.put(tabId, config);
    }

    public boolean hasRepeaterForTab(int tabId) {
        return repeaters.containsKey(tabId);
    }

    public void removeRepeaterByTabId(int tabId) {
        repeaters.remove(tabId);
    }

    public void onMessageReceived(int socketId, InterceptedMessageFacade message) {
        for (AutoRepeaterConfig config : repeaters.values()) {
            if (socketId == config.getSourceSocketId()) {
                WebSocketContainer target = this.wsConnections.get(config.getTargetSocketId());
                if (target == null) {
                    api.logging().logToOutput("target socket is null - somwething bad happend");
                    return;
                }

                api.logging().logToOutput("target is: " + target.getWebSocketCreation().upgradeRequest().url());

                // Check direction in config

                if (message.isText()) {
                    target.getWebSocketCreation().proxyWebSocket().sendTextMessage(message.stringPayload(), message.direction());
                } else {
                    // This is not ideal, we should be able to pass the raw byte[]
                    // TODO; handle raw byte[] to ByteArray
                    target.getWebSocketCreation().proxyWebSocket().sendBinaryMessage(ByteArray.byteArray(message.stringPayload()), message.direction());
                }
            }
        }
    }
}