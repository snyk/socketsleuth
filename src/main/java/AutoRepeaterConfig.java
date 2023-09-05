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
import burp.api.montoya.websocket.Direction;

public class AutoRepeaterConfig {
    private int sourceSocketId;
    private int targetSocketId;
    private Direction direction;
    private int tabId;
    private boolean isActive;

    private WebSocketAutoRepeaterStreamTableModel streamTableModel;

    public AutoRepeaterConfig(int sourceSocketId, int targetSocketId, Direction direction, int tabId, WebSocketAutoRepeaterStreamTableModel streamTableModel) {
        this.sourceSocketId = sourceSocketId;
        this.targetSocketId = targetSocketId;
        this.direction = direction;
        this.tabId = tabId;
        this.isActive = false;
        this.streamTableModel = streamTableModel;
    }

    public int getSourceSocketId() {
        return sourceSocketId;
    }

    public void setSourceSocketId(int sourceSocketId) {
        this.sourceSocketId = sourceSocketId;
    }

    public int getTargetSocketId() {
        return targetSocketId;
    }

    public void setTargetSocketId(int targetSocketId) {
        this.targetSocketId = targetSocketId;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getTabId() {
        return tabId;
    }

    public void setTabId(int tabId) {
        this.tabId = tabId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public WebSocketAutoRepeaterStreamTableModel getStreamTableModel() {
        return streamTableModel;
    }
}
