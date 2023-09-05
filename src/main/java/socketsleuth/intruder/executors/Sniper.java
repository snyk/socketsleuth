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

package socketsleuth.intruder.executors;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.proxy.websocket.ProxyWebSocket;
import burp.api.montoya.websocket.Direction;
import burp.api.montoya.websocket.TextMessage;
import socketsleuth.intruder.WSIntruderMessageView;
import socketsleuth.intruder.payloads.models.IPayloadModel;
import socketsleuth.intruder.payloads.payloads.Utils;
import websocket.MessageProvider;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WebSocketMessage {
    private String data;
    private Direction direction;

    public WebSocketMessage(String data, Direction direction) {
        this.data = data;
        this.direction = direction;
    }
}

public class Sniper {
    private final MessageProvider socketProvider;
    private MontoyaApi api;
    private WSIntruderMessageView messageView;
    private Thread workerThread;
    private volatile boolean cancelled = false;
    private int minDelay = 100;
    private int maxDelay = 200;
    private List<WebSocketMessage> sentMessages;

    public Sniper(MontoyaApi api, WSIntruderMessageView messageView, MessageProvider socketProvider) {
        this.api = api;
        this.messageView = messageView;
        this.socketProvider = socketProvider;
        this.sentMessages = new ArrayList<WebSocketMessage>();
    }

    public int getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(int minDelay) {
        this.minDelay = minDelay;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
    }

    public boolean isRunning() {
        if (workerThread == null) {
            return false;
        }

        return workerThread.isAlive();
    }

    public void start(ProxyWebSocket proxyWebSocket,
                      int socketId, IPayloadModel<String> payloadModel,
                      String baseInput,
                      Direction selectedDirection) {
        if (workerThread != null && workerThread.isAlive()) {
            api.logging().logToOutput("Intruder action is already running. Wait before new action.");
            return;
        }

        List<String> payloadPositions = Utils.extractPayloadPositions(baseInput);
        if (payloadPositions.size() == 0) {
            JOptionPane.showMessageDialog(
                    api.userInterface().swingUtils().suiteFrame(),
                    "Please ensure at least one payload position is defined.",
                    "Invalid configuration", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        api.logging().logToOutput(
                "Starting sniper payload insertion with Min Delay: "
                        + minDelay
                        + " Max Delay: "
                        + maxDelay
        );

        Consumer<TextMessage> responseSubscriber = textMessage -> {
            messageView.getTableModel().addMessage(textMessage.payload(), textMessage.direction());
        };
        this.socketProvider.subscribeTextMessage(socketId, responseSubscriber);


        Random rand = new Random();
        workerThread = new Thread(() -> {
            api.logging().logToOutput("Sniper execution started");
            for (String payload : payloadModel) {
                String newInput = replacePlaceholders(baseInput, payload);
                proxyWebSocket.sendTextMessage(newInput, selectedDirection);
                messageView.getTableModel().addMessage(newInput, selectedDirection);
                sentMessages.add(new WebSocketMessage(newInput, selectedDirection));
                int delay = rand.nextInt(maxDelay - minDelay + 1) + minDelay;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            // Wait a while to catch responses from the final request
            try {
                api.logging().logToOutput("finished - cleaning up");
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                this.socketProvider.unsubscribeTextMessage(socketId, responseSubscriber);
                api.logging().logToOutput("clean up complete");
            }
        });

        workerThread.start();
    }

    private static String replacePlaceholders(String input, String replacement) {
        Pattern pattern = Pattern.compile("§(.*?)§");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
