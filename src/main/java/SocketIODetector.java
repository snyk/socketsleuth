import java.util.regex.Pattern;

public class SocketIODetector {

    public static boolean isSocketIOConnection(Object[] websocketMessages) {
        if (websocketMessages == null || websocketMessages.length == 0) {
            return false;
        }

        // This pattern looks for a Socket.IO handshake message:
        // It starts with "0", followed by a "{" character and should contain a "sid" field.
        Pattern socketIOHandshakePattern = Pattern.compile("^0\\{.*\"sid\"\\s*:\\s*\".+\".*\\}");

        for (Object message : websocketMessages) {
            if (message instanceof String) {
                if (socketIOHandshakePattern.matcher((String) message).find()) {
                    return true;
                }
            } else if (message instanceof byte[]) {
                String byteMessage = new String((byte[]) message);
                if (socketIOHandshakePattern.matcher(byteMessage).find()) {
                    return true;
                }
            }
        }

        return false;
    }
}
