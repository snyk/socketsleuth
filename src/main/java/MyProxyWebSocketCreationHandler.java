import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.websocket.ProxyWebSocketCreation;
import burp.api.montoya.proxy.websocket.ProxyWebSocketCreationHandler;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

class MyProxyWebSocketCreationHandler implements ProxyWebSocketCreationHandler {

    Logging logger;
    MontoyaApi api;
    Map<Integer, WebSocketContainer> connections;

    WebSocketConnectionTableModel tableModel;

    JTable connectionTable;

    JTable streamTable;

    WebSocketInterceptionRulesTableModel interceptionRules;

    public MyProxyWebSocketCreationHandler(
            MontoyaApi api,
            WebSocketConnectionTableModel tableModel,
            JTable connectionTable,
            JTable streamTable,
            WebSocketInterceptionRulesTableModel interceptionRules
    ) {
        this.api = api;
        this.logger = api.logging();
        this.connections = new HashMap<>();
        this.tableModel = tableModel;
        this.connectionTable = connectionTable;
        this.streamTable = streamTable;
        this.interceptionRules = interceptionRules;
    }

    @Override
    public void handleWebSocketCreation(ProxyWebSocketCreation webSocketCreation) {
        logger.logToOutput("New WS connection received");

        // Don't loose track of the selected table
        int selectedConnectionIndex = connectionTable.getSelectedRow();
        ListSelectionModel selectionModel = connectionTable.getSelectionModel();
        boolean isSelectionEmpty = selectionModel.isSelectionEmpty();

        // Store off the WebSocket so we can access it later
        WebSocketContainer container = new WebSocketContainer();
        container.setWebSocketCreation(webSocketCreation);
        container.setTableRow(new WebsocketConnectionTableRow(
                this.connections.size(),
                webSocketCreation.upgradeRequest().url(),
                webSocketCreation.upgradeRequest().httpService().port(),
                webSocketCreation.upgradeRequest().httpService().secure(),
                ""
        ));

        this.connections.put(this.connections.size(), container);
        // Get the new row from container and add to actual table model
        this.tableModel.addConnection(container.getTableRow());

        // Restore the selection if there was a previous selection
        if (!isSelectionEmpty) {
            selectionModel.setSelectionInterval(selectedConnectionIndex, selectedConnectionIndex);
        } else {
            selectionModel.clearSelection();
        }

        // Close handler? don't think we can tell :(

        // Setup handler for messages within WS stream
        webSocketCreation.proxyWebSocket().registerProxyMessageHandler(
                new MyProxyWebSocketMessageHandler(
                        this.api,
                        container.getTableRow().getStreamModel(),
                        this.streamTable,
                        this.interceptionRules
                )
        );
        logger.logToOutput("Total sockets: " + this.connections.size());
        logger.logToOutput("lets try to make a new one");
        // Lets make a new socket request because why not
    }
}