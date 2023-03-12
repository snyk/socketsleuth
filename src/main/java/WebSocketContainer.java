import burp.api.montoya.proxy.websocket.ProxyWebSocketCreation;

public class WebSocketContainer {
    private ProxyWebSocketCreation webSocketCreation;
    private WebsocketConnectionTableRow tableModel;

    public ProxyWebSocketCreation getWebSocketCreation() {
        return webSocketCreation;
    }

    public void setWebSocketCreation(ProxyWebSocketCreation webSocketCreation) {
        this.webSocketCreation = webSocketCreation;
    }

    public WebsocketConnectionTableRow getTableRow() {
        return tableModel;
    }

    public void setTableRow(WebsocketConnectionTableRow tableModel) {
        this.tableModel = tableModel;
    }
}
