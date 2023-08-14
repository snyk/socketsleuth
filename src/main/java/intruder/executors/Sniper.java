package intruder.executors;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.proxy.websocket.ProxyWebSocket;
import burp.api.montoya.websocket.Direction;
import intruder.payloads.models.IPayloadModel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    MontoyaApi api;
    private Thread workerThread;
    private volatile boolean cancelled = false;
    private int minDelay = 100;
    private int maxDelay = 200;
    private List<WebSocketMessage> sentMessages;

    public Sniper(MontoyaApi api) {
        this.api = api;
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

    public void start(ProxyWebSocket proxyWebSocket, IPayloadModel payloadModel, String baseInput) {
        if (workerThread != null && workerThread.isAlive()) {
            api.logging().logToOutput("Intruder action is already running. Wait before new action.");
            return;
        }

        api.logging().logToOutput(
                "Starting JSONRPC payload insertion with Min Delay: "
                        + minDelay
                        + " Max Delay: "
                        + maxDelay
                        + " and Wordlist Size: "
                        + payloadModel.getListModel().size()
        );

        Random rand = new Random();
        workerThread = new Thread(() -> {
            api.logging().logToOutput("started");
            DefaultListModel model = payloadModel.getListModel();
            int currentId = 10000;
            for (int i = 0; i < payloadModel.getListModel().size(); i++) {

                String newInput = replacePlaceholders(baseInput, model.get(i).toString());
                // TODO: add combo box to switch direction
                proxyWebSocket.sendTextMessage(newInput, Direction.CLIENT_TO_SERVER);
                sentMessages.add(new WebSocketMessage(newInput, Direction.CLIENT_TO_SERVER));
                int delay = rand.nextInt(maxDelay - minDelay + 1) + minDelay;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            api.logging().logToOutput("finished");
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
