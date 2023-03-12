import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.proxy.websocket.*;

import javax.swing.*;
import java.time.LocalDateTime;

class MyProxyWebSocketMessageHandler implements ProxyMessageHandler {

    MontoyaApi api;
    Logging logger;

    WebSocketStreamTableModel streamModel;

    JTable streamTable;

    WebSocketInterceptionRulesTableModel interceptionRules;

    public MyProxyWebSocketMessageHandler(
            MontoyaApi api,
            WebSocketStreamTableModel streamModel,
            JTable streamTable,
            WebSocketInterceptionRulesTableModel interceptionRules
    ) {
        this.api = api;
        this.logger = api.logging();
        // we are not setting the stream so it doens't exist later......
        this.streamModel = streamModel;
        this.streamTable = streamTable;
        this.interceptionRules = interceptionRules;
    }

    @Override
    public TextMessageReceivedAction handleTextMessageReceived(InterceptedTextMessage interceptedTextMessage) {

        int selectedRowIndex = streamTable.getSelectedRow();
        ListSelectionModel selectionModel = streamTable.getSelectionModel();
        boolean isSelectionEmpty = selectionModel.isSelectionEmpty();

        streamModel.addStream(new WebSocketStream(
                streamModel.getRowCount(),
                interceptedTextMessage,
                LocalDateTime.now(),
                ""
        ));

        // Restore the selection if there was a previous selection
        if (!isSelectionEmpty) {
            selectionModel.setSelectionInterval(selectedRowIndex, selectedRowIndex);
        } else {
            selectionModel.clearSelection();
        }

        if (shouldInterceptMessage(this.interceptionRules, interceptedTextMessage)) {
            //interceptedTextMessage.annotations().setHighlightColor(HighlightColor.RED);
            return TextMessageReceivedAction.intercept(interceptedTextMessage);
        }

        /*if (interceptedTextMessage.payload().contains("username")) {
            interceptedTextMessage.annotations().setHighlightColor(HighlightColor.RED);
        }

        if (interceptedTextMessage.direction() == CLIENT_TO_SERVER && interceptedTextMessage.payload().contains("password")) {
            return TextMessageReceivedAction.intercept(interceptedTextMessage);
        }
        */
        return TextMessageReceivedAction.continueWith(interceptedTextMessage);
    }

    private boolean shouldInterceptMessage(
            WebSocketInterceptionRulesTableModel interceptionRules,
            InterceptedTextMessage interceptedTextMessage
    ) {
        for (int i = 0; i < interceptionRules.getRowCount(); i++) {
            boolean enabled = (boolean) interceptionRules.getValueAt(i, 0); // ENABLED

            WebSocketInterceptionRulesTableModel.MatchType matchType
                    = (WebSocketInterceptionRulesTableModel.MatchType) interceptionRules.getValueAt(i, 1); // Match type

            WebSocketInterceptionRulesTableModel.Direction direction
                    = (WebSocketInterceptionRulesTableModel.Direction) interceptionRules.getValueAt(i, 2); // Direction


            String condition = (String) interceptionRules.getValueAt(i, 3); // Condition

            // ignore disabled rules
            if (enabled) {
                boolean shouldIntercept = false;

                // check direction before condition - probably a better way to test this
                if (direction == WebSocketInterceptionRulesTableModel.Direction.CLIENT_TO_SERVER) {
                    if (!interceptedTextMessage.direction().toString().equals("CLIENT_TO_SERVER")) continue;
                }

                if (direction == WebSocketInterceptionRulesTableModel.Direction.SERVER_TO_CLIENT) {
                    if (!interceptedTextMessage.direction().toString().equals("SERVER_TO_CLIENT")) continue;
                }

                api.logging().logToOutput("direction rule: " + direction + " - message direction" + interceptedTextMessage.direction().toString());

                switch (matchType) {
                    case CONTAINS: {
                        shouldIntercept = interceptedTextMessage.payload().contains(condition);
                        break;
                    }
                    case DOES_NOT_CONTAIN: {
                        shouldIntercept = !interceptedTextMessage.payload().contains(condition);
                        break;
                    }
                    case EXACT_MATCH: {
                        shouldIntercept = interceptedTextMessage.payload().equals(condition);
                       break;
                    }
                    default: {
                        api.logging().logToOutput("Unknown interceptor match type detected!");
                        break;
                    }
                }

                // Don't need to test additional rules if we already matched
                if (shouldIntercept) return true;
            }
        }

        return false;
    }

    @Override
    public TextMessageToBeSentAction handleTextMessageToBeSent(InterceptedTextMessage interceptedTextMessage) {
        return TextMessageToBeSentAction.continueWith(interceptedTextMessage);
    }

    @Override
    public BinaryMessageReceivedAction handleBinaryMessageReceived(InterceptedBinaryMessage interceptedBinaryMessage) {
        int selectedRowIndex = streamTable.getSelectedRow();
        ListSelectionModel selectionModel = streamTable.getSelectionModel();
        boolean isSelectionEmpty = selectionModel.isSelectionEmpty();

        streamModel.addStream(new WebSocketStream(
                streamModel.getRowCount(),
                interceptedBinaryMessage,
                LocalDateTime.now(),
                ""
        ));

        // Restore the selection if there was a previous selection
        if (!isSelectionEmpty) {
            selectionModel.setSelectionInterval(selectedRowIndex, selectedRowIndex);
        } else {
            selectionModel.clearSelection();
        }
        return BinaryMessageReceivedAction.continueWith(interceptedBinaryMessage);
    }

    @Override
    public BinaryMessageToBeSentAction handleBinaryMessageToBeSent(InterceptedBinaryMessage interceptedBinaryMessage) {
        return BinaryMessageToBeSentAction.continueWith(interceptedBinaryMessage);
    }
}
