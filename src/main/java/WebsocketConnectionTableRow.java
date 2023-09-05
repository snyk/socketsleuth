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
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.proxy.websocket.ProxyWebSocket;

public class WebsocketConnectionTableRow {

    private int socketId;
    private String url;
    private int listenerPort;
    private boolean tls;
    private boolean active;
    private String comment;

    private HttpRequest upgradeRequest;

    // This may need to be a different type of websocket to support manually created...
    // handle later
    private ProxyWebSocket proxyWebSocket;

    private WebSocketStreamTableModel streamModel;

    public WebSocketStreamTableModel getStreamModel() {
        return streamModel;
    }

    public void setStreamModel(WebSocketStreamTableModel streamModel) {
        this.streamModel = streamModel;
    }

    public WebsocketConnectionTableRow(int socketId, String url, int listenerPort, boolean active, boolean tls, String comment, HttpRequest upgradeRequest, ProxyWebSocket proxyWebSocket) {
        this.socketId = socketId;
        this.url = url;
        this.listenerPort = listenerPort;
        this.tls = tls;
        this.active = active;
        this.comment = comment;
        this.upgradeRequest = upgradeRequest;
        this.streamModel = new WebSocketStreamTableModel();
        this.proxyWebSocket = proxyWebSocket;
        //this.streamModel.addStream(new WebSocketStream(socketId, "test", "fake", 4, LocalDateTime.now(), ""));
    }

    public HttpRequest getUpgradeRequest() {
        return upgradeRequest;
    }

    public int getSocketId() {
        return socketId;
    }

    public void setSocketId(int socketId) {
        this.socketId = socketId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String endpoint) {
        this.url = endpoint;
    }

    public int getListenerPort() {
        return listenerPort;
    }

    public void setListenerPort(int listenerPort) {
        this.listenerPort = listenerPort;
    }

    public boolean isTls() {
        return tls;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ProxyWebSocket getProxyWebSocket() {
        return proxyWebSocket;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
