import burp.api.montoya.MontoyaApi;
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

    WebSocketMatchReplaceRulesTableModel matchReplaceRules;

    public MyProxyWebSocketMessageHandler(
            MontoyaApi api,
            WebSocketStreamTableModel streamModel,
            JTable streamTable,
            WebSocketInterceptionRulesTableModel interceptionRules,
            WebSocketMatchReplaceRulesTableModel matchReplaceRules) {
        this.api = api;
        this.logger = api.logging();
        this.streamModel = streamModel;
        this.streamTable = streamTable;
        this.interceptionRules = interceptionRules;
        this.matchReplaceRules = matchReplaceRules;
    }

    // Strange - this seems to also catch messages being sent
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
            // This is the in the API example but seems to break WSs :/
            //interceptedTextMessage.annotations().setHighlightColor(HighlightColor.RED);
            return TextMessageReceivedAction.intercept(interceptedTextMessage);
        }

        if (shouldDropMessage(this.matchReplaceRules, new InterceptedMessageFacade(interceptedTextMessage))) {
            return TextMessageReceivedAction.drop();
        }

        interceptedTextMessage = (InterceptedTextMessage) handleMatchAndReplace(
                this.matchReplaceRules,
                new InterceptedMessageFacade(interceptedTextMessage)
        );

        return TextMessageReceivedAction.continueWith(interceptedTextMessage);
    }

    private boolean shouldDropMessage(
            WebSocketMatchReplaceRulesTableModel matchReplaceRules,
            InterceptedMessageFacade interceptedMessageFacade
    ) {
        for (int i = 0; i < matchReplaceRules.getRowCount(); i++) {
            boolean enabled = (boolean) matchReplaceRules.getValueAt(i, 0); // ENABLED

            if (!enabled) {
                continue;
            }

            WebSocketMatchReplaceRulesTableModel.MatchType matchType =
                    (WebSocketMatchReplaceRulesTableModel.MatchType) matchReplaceRules.getValueAt(i, 1);

            WebSocketMatchReplaceRulesTableModel.Direction direction =
                    (WebSocketMatchReplaceRulesTableModel.Direction) matchReplaceRules.getValueAt(i, 2);

            String strMatch = (String) matchReplaceRules.getValueAt(i, 3);

            if (matchType != WebSocketMatchReplaceRulesTableModel.MatchType.DROP) {
                continue;
            }

            if (direction == WebSocketMatchReplaceRulesTableModel.Direction.CLIENT_TO_SERVER) {
                if (!interceptedMessageFacade.direction().toString().equals("CLIENT_TO_SERVER")) continue;
            }

            if (direction == WebSocketMatchReplaceRulesTableModel.Direction.SERVER_TO_CLIENT) {
                if (!interceptedMessageFacade.direction().toString().equals("SERVER_TO_CLIENT")) continue;
            }

            // Find / Match is in Hex
            if (Utils.isHexString(strMatch)) {
                return Utils.byteArrayContains(
                        interceptedMessageFacade.binaryPayload(),
                        Utils.hexStringToByteArray(strMatch)
                );
            // Otherwise normal string
            } else {
                return interceptedMessageFacade.stringPayload().contains(strMatch);
            }
        }
        return false;
    }

    private Object handleMatchAndReplace(
            WebSocketMatchReplaceRulesTableModel matchReplaceRules,
            InterceptedMessageFacade interceptedMessageFacade
    ) {
        for (int i = 0; i < matchReplaceRules.getRowCount(); i++) {
            boolean enabled = (boolean) matchReplaceRules.getValueAt(i, 0); // ENABLED

            if (!enabled) {
                continue;
            }
            WebSocketMatchReplaceRulesTableModel.MatchType matchType =
                    (WebSocketMatchReplaceRulesTableModel.MatchType) matchReplaceRules.getValueAt(i, 1);

            WebSocketMatchReplaceRulesTableModel.Direction direction =
                    (WebSocketMatchReplaceRulesTableModel.Direction) matchReplaceRules.getValueAt(i, 2);

            String strMatch = (String) matchReplaceRules.getValueAt(i, 3);

            String strReplace = (String) matchReplaceRules.getValueAt(i, 4);

            if (matchType != WebSocketMatchReplaceRulesTableModel.MatchType.REPLACE) {
                // dropping messages needs to return a differnt function from the handler
                // handle this separately inside handleTextMessageReceived + handleBinaryMessageReceived
                continue;
            }

            if (direction == WebSocketMatchReplaceRulesTableModel.Direction.CLIENT_TO_SERVER) {
                if (!interceptedMessageFacade.direction().toString().equals("CLIENT_TO_SERVER")) continue;
            }

            if (direction == WebSocketMatchReplaceRulesTableModel.Direction.SERVER_TO_CLIENT) {
                if (!interceptedMessageFacade.direction().toString().equals("SERVER_TO_CLIENT")) continue;
            }

            // Are we match / replacing a hex pattern?
            if (Utils.isHexString(strMatch)) {
                byte[] bytesMatch = Utils.hexStringToByteArray(strMatch);
                byte[] bytesReplace = Utils.isHexString(strReplace) ? Utils.hexStringToByteArray(strReplace) : strReplace.getBytes();
                byte[] inputBytes = interceptedMessageFacade.binaryPayload();
                byte[] modified = Utils.replace(inputBytes, bytesMatch, bytesReplace);
                interceptedMessageFacade.setBytesPayload(modified);
                return interceptedMessageFacade.getInterceptedMessage();
            } else {        // it's a normal string or regex replacement
                // Do the replacement and see if there is changes
                String newStr = Utils.replace(interceptedMessageFacade.stringPayload(), strMatch, strReplace);
                if (!newStr.equals(interceptedMessageFacade.stringPayload())) {
                    interceptedMessageFacade.setStringPayload(newStr);
                    return interceptedMessageFacade.getInterceptedMessage();
                }
            }
        }
        return interceptedMessageFacade.getInterceptedMessage();
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
