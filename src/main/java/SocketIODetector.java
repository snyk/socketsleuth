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
