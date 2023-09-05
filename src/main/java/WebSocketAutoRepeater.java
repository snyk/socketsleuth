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
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.proxy.websocket.InterceptedBinaryMessage;
import burp.api.montoya.proxy.websocket.InterceptedTextMessage;
import burp.api.montoya.websocket.Direction;

import java.time.LocalDateTime;
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
            // Handle receive on source socket - we need to forward the message
            if (socketId == config.getSourceSocketId()) {
                WebSocketContainer target = this.wsConnections.get(config.getTargetSocketId());
                if (target == null) {
                    api.logging().logToOutput("target socket is null - somwething bad happend");
                    return;
                }

                // When no direction is set, its bidirectional
                if (config.getDirection() != null) {
                    if (config.getDirection().toString() == Direction.CLIENT_TO_SERVER.toString()) {
                        if (!message.direction().toString().equals("CLIENT_TO_SERVER")) continue;
                    }

                    if (config.getDirection().toString() == Direction.SERVER_TO_CLIENT.toString()) {
                        if (!message.direction().toString().equals("SERVER_TO_CLIENT")) continue;
                    }
                }

                api.logging().logToOutput("direction is: " + config.getDirection());

                WebSocketStream streamItem;
                if (message.isText()) {
                    target.getWebSocketCreation().proxyWebSocket().sendTextMessage(message.stringPayload(), message.direction());
                    streamItem = new WebSocketStream(
                            config.getStreamTableModel().getRowCount(),
                            (InterceptedTextMessage) message.getInterceptedMessage(),
                            LocalDateTime.now(),
                            "");
                } else {
                    // This is not ideal, we should be able to pass the raw byte[]
                    // TODO; handle raw byte[] to ByteArray
                    target.getWebSocketCreation().proxyWebSocket().sendBinaryMessage(ByteArray.byteArray(message.stringPayload()), message.direction());
                    streamItem = new WebSocketStream(
                            config.getStreamTableModel().getRowCount(),
                            (InterceptedBinaryMessage) message.getInterceptedMessage(),
                            LocalDateTime.now(),
                            "");
                }

                streamItem.setInjected(true);
                config.getStreamTableModel().addStream(streamItem);
            }

            // Handle receive on dst socket
            WebSocketStream streamItem;
            if (socketId == config.getTargetSocketId()) {
                if (message.isText()) {
                    streamItem = new WebSocketStream(
                            config.getStreamTableModel().getRowCount(),
                            (InterceptedTextMessage) message.getInterceptedMessage(),
                            LocalDateTime.now(),
                            "");
                } else {
                    streamItem = new WebSocketStream(
                            config.getStreamTableModel().getRowCount(),
                            (InterceptedBinaryMessage) message.getInterceptedMessage(),
                            LocalDateTime.now(),
                            "");
                }
                config.getStreamTableModel().addStream(streamItem);
            }
        }
    }
}
