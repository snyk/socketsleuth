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
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.websocket.ProxyWebSocketCreation;
import burp.api.montoya.proxy.websocket.ProxyWebSocketCreationHandler;
import socketsleuth.WebSocketInterceptionRulesTableModel;
import websocket.MessageProvider;

import javax.swing.*;
import java.util.Map;

class WebSocketCreationHandler implements ProxyWebSocketCreationHandler {

    private final MessageProvider socketProvider;
    Logging logger;
    MontoyaApi api;
    Map<Integer, WebSocketContainer> connections;
    WebSocketConnectionTableModel tableModel;
    JTable connectionTable;
    JTable streamTable;
    WebSocketInterceptionRulesTableModel interceptionRules;
    WebSocketMatchReplaceRulesTableModel matchReplaceRules;
    JSONRPCResponseMonitor responseMonitor;
    WebSocketAutoRepeater webSocketAutoRepeater;

    public WebSocketCreationHandler(
            MontoyaApi api,
            WebSocketConnectionTableModel tableModel,
            Map<Integer, WebSocketContainer> wsConnections,
            JTable connectionTable,
            JTable streamTable,
            WebSocketInterceptionRulesTableModel interceptionRules,
            WebSocketMatchReplaceRulesTableModel matchReplaceRules,
            JSONRPCResponseMonitor responseMonitor,
            WebSocketAutoRepeater webSocketAutoRepeater,
            MessageProvider socketProvider) {
        this.api = api;
        this.logger = api.logging();
        this.connections = wsConnections;
        this.tableModel = tableModel;
        this.connectionTable = connectionTable;
        this.streamTable = streamTable;
        this.interceptionRules = interceptionRules;
        this.matchReplaceRules = matchReplaceRules;
        this.responseMonitor = responseMonitor;
        this.webSocketAutoRepeater = webSocketAutoRepeater;
        this.socketProvider = socketProvider;
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
                true,
                webSocketCreation.upgradeRequest().httpService().secure(),
                "",
                webSocketCreation.upgradeRequest(),
                webSocketCreation.proxyWebSocket()
        ));

        // TODO: Investigate if we can get the socketId form burp instead of making our own
        int socketId = this.connections.size();
        this.connections.put(socketId, container);
        this.socketProvider.handleSocketCreated(socketId, webSocketCreation);

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
                new WebSocketMessageHandler(
                        this.api,
                        socketId,
                        container.getTableRow().getStreamModel(),
                        this.streamTable,
                        this.interceptionRules,
                        this.matchReplaceRules,
                        new SocketCloseCallback() {
                            @Override
                            public void handleConnectionClosed() {
                                container.getTableRow().setActive(false);
                                tableModel.fireTableDataChanged();
                            }
                        },
                        this.responseMonitor,
                        this.webSocketAutoRepeater,
                        this.socketProvider
                )
        );
    }
}